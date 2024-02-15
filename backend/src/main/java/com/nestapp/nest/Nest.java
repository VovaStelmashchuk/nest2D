package com.nestapp.nest;

import com.nestapp.nest.config.Config;
import com.nestapp.nest.data.*;
import com.nestapp.nest.nfp.NfpCacheRepository;
import com.nestapp.nest.nfp.NfpKey;
import com.nestapp.nest.nfp.NfpPair;
import com.nestapp.nest.nfp.NfpUtils;
import com.nestapp.nest.util.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;


public class Nest {

    private final Config config;
    private final NfpCacheRepository nfpCache = new NfpCacheRepository();


    /**
     * @param config parameter settings
     */
    public Nest(
        Config config
    ) {
        this.config = config;
    }

    /**
     * Start the Nest calculation
     *
     * @return the placements' list of all NestPath
     */
    public List<List<Placement>> startNest(NestPath plateNestPath, List<NestPath> parts) {
        List<NestPath> tree = NewCommonUtils.INSTANCE.copyNestPathsAndSetIds(parts);

        CommonUtil.offsetTree(tree, 0.5 * config.SPACING);
        plateNestPath.config = config;
        for (NestPath nestPath : parts) {
            nestPath.config = config;
        }

        NestPath binPolygon = new NestPath(plateNestPath);    //conversione di un eventuale binPath self intersecting in un poligono semplice
        // Bound binBound = GeometryUtil.getPolygonBounds(binPolygon);
        if (Config.BOUND_SPACING > 0) {
            List<NestPath> offsetBin = CommonUtil.polygonOffset(binPolygon, -Config.BOUND_SPACING);
            if (offsetBin.size() == 1) {
                binPolygon = offsetBin.get(0);
            }
        }
        binPolygon.setId(-1);
        // A part may become not positionable after a rotation. TODO this can also be removed if we know that all parts are legal
        List<Integer> integers = checkIfCanBePlaced(binPolygon, tree);
        List<NestPath> safeTree = new ArrayList<>();
        for (Integer i : integers) {
            safeTree.add(tree.get(i));
        }
        tree = safeTree;

        /*VENGONO SETTATE LE COORDINATE MAX E MIN DEL SINGOLO BINPATH PER POI POTERLO TRASLARE NELL'ORIGINE*/
        double xbinmax = binPolygon.get(0).x;    // get.(0) = prende il primo segmento dei 4 (coordinate del primo vertice), se si assume che la superficie sia rettangolare
        double xbinmin = binPolygon.get(0).x;
        double ybinmax = binPolygon.get(0).y;
        double ybinmin = binPolygon.get(0).y;
        // Find min max
        for (int i = 1; i < binPolygon.size(); i++) {
            if (binPolygon.get(i).x > xbinmax) {
                xbinmax = binPolygon.get(i).x;
            } else if (binPolygon.get(i).x < xbinmin) {
                xbinmin = binPolygon.get(i).x;
            }

            if (binPolygon.get(i).y > ybinmax) {
                ybinmax = binPolygon.get(i).y;
            } else if (binPolygon.get(i).y < ybinmin) {
                ybinmin = binPolygon.get(i).y;
            }
        }

        /*VIENE TRASLATO IL POLIGONO BINPATH NELL'ORIGINE*/
        for (int i = 0; i < binPolygon.size(); i++) {
            binPolygon.get(i).x -= xbinmin;
            binPolygon.get(i).y -= ybinmin;
        }

        //double binPolygonWidth = xbinmax - xbinmin;
        //double binPolygonHeight = ybinmax - ybinmin;

        /*VIENE ROVESCIATO IL POLIGONO BINPATH NELL'ORIGINE PER AVERE L'ORIGINE NON IN ALTO A SX MA IN BASSO A SX*/
        if (GeometryUtil.polygonArea(binPolygon) > 0) {
            binPolygon.reverse();
        }
        /*
         * Make sure it's counterclockwise (rotazione antioraria) TODO why?
         * Need for NFP algorithm
         */
        for (NestPath element : tree) {
            Segment start = element.get(0);
            Segment end = element.get(element.size() - 1);
            if (start == end || GeometryUtil.almostEqual(start.x, end.x) && GeometryUtil.almostEqual(start.y, end.y)) {
                element.pop();
            }
            if (GeometryUtil.polygonArea(element) > 0) {
                element.reverse();
            }
        }


        /*-------------------------START NESTING---------------------------------------*/
        Result result = launchWorkers(tree, binPolygon);
        List<List<Placement>> appliedPlacement = applyPlacement(result, tree);

        return appliedPlacement;
    }

    /**
     * Applicazione dell'algoritmo di nesting
     *
     * @param tree       lista di tutti i poligoni da disporre
     * @param binPolygon superficie principale su cui disporre i poligoni
     * @return bestResult
     */
    public Result launchWorkers(List<NestPath> tree, NestPath binPolygon) {
        List<NestPath> adam = generateAdamForGeneticAlgorithm(tree);

        nfpCache.setNestPaths(adam);
        nfpCache.setBinPolygon(binPolygon);

        /*---------------------FITNESS COMPUTATION---------------------*/
        // Places parts according to the sequence specified by the individual
        Placementworker worker = new Placementworker(binPolygon, nfpCache); // --------> uses Placementworker to set fitness value

        List<NestPath> placeListSlice = new ArrayList<>();

        for (int i = 0; i < adam.size(); i++) {
            placeListSlice.add(new NestPath(adam.get(i)));
        }

        Result result = worker.placePaths(placeListSlice);

        System.out.print("launchWorkers(): current best fitness = " + result.fitness);
        return result;
    }

    @NotNull
    private List<NestPath> generateAdamForGeneticAlgorithm(List<NestPath> tree) {
        List<NestPath> adam = new ArrayList<>();
        for (NestPath nestPath : tree) {
            NestPath clone = new NestPath(nestPath);
            adam.add(clone);
        }
        for (NestPath nestPath : adam) {
            nestPath.area = GeometryUtil.polygonArea(nestPath);
            nestPath.setRotation(90);
        }
        Collections.sort(adam);
        return adam;
    }

    /**
     * Bind translate and rotate to the corresponding board by id and bid
     *
     * @param best current fitness best value
     * @param tree all NestPaths
     * @return applyPlacement    all the translations/rotations that have been done on the best current Individual
     */
    public static List<List<Placement>> applyPlacement(Result best, List<NestPath> tree) {
        List<List<Placement>> applyPlacement = new ArrayList<>();
        for (int i = 0; i < best.placements.size(); i++) {
            List<Placement> binTranslate = new ArrayList<>();
            for (int j = 0; j < best.placements.get(i).size(); j++) {
                PathPlacement v = best.placements.get(i).get(j);
                NestPath nestPath = tree.get(v.id);
                Placement placement = new Placement(nestPath.getBid(), new Segment(v.x, v.y), v.rotation);
                binTranslate.add(placement);
            }
            applyPlacement.add(binTranslate);
        }
        return applyPlacement;
    }


    /**
     * * Every time a new population is generated by mutation or mating in the genetic algorithm,
     * the result that the plate and the rotation angle do not match may occur, and it needs to be re-checked and adapted.
     *
     * @param binPolygon
     * @param tree
     * @return
     */
    public static List<Integer> checkIfCanBePlaced(NestPath binPolygon, List<NestPath> tree) {
        List<Integer> CanBePlacdPolygonIndex = new ArrayList<>();
        Bound binBound = GeometryUtil.getPolygonBounds(binPolygon);
        for (int i = 0; i < tree.size(); i++) {
            NestPath nestPath = tree.get(i);
            if (nestPath.getRotation() == 0) {
                Bound bound = GeometryUtil.getPolygonBounds(nestPath);
                if (bound.width < binBound.width && bound.height < binBound.height) {
                    CanBePlacdPolygonIndex.add(i);
                    continue;
                }
            } else {
                for (int j = 0; j < nestPath.getRotation(); j++) {
                    Bound rotatedBound = GeometryUtil.rotatePolygon(nestPath, (360 / nestPath.getRotation()) * j);
                    if (rotatedBound.width < binBound.width && rotatedBound.height < binBound.height) {
                        CanBePlacdPolygonIndex.add(i);
                        break;
                    }
                }
            }
        }
        return CanBePlacdPolygonIndex;
    }

    public interface ListPlacementObserver {
        void populationUpdate(List<List<Placement>> appliedPlacement);
    }

    public interface ResultObserver {
        void muationStepDone(Result result);
    }

    public List<ListPlacementObserver> observers = new ArrayList<>();

    public List<ResultObserver> resultobservers = new ArrayList<>();

    public void notifyObserver(List<List<Placement>> appliedPlacement) {
        for (ListPlacementObserver lo : observers) {
            lo.populationUpdate(appliedPlacement);
        }
    }

    public void notifyObserver(Result result) {
        for (ResultObserver lo : resultobservers) {
            lo.muationStepDone(result);
        }
    }
}
