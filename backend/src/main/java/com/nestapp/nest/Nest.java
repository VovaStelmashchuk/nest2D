package com.nestapp.nest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nestapp.nest.algorithm.GeneticAlgorithm;
import com.nestapp.nest.algorithm.Individual;
import com.nestapp.nest.config.Config;
import com.nestapp.nest.data.*;
import com.nestapp.nest.nfp.NfpPair;
import com.nestapp.nest.nfp.NfpUtils;
import com.nestapp.nest.util.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.nestapp.nest.util.IOUtils.log;

/**
 * @author yisa
 */
public class Nest {

    private Config config;
    private int loopCount;
    private GeneticAlgorithm GA = null;
    private Map<String, List<NestPath>> nfpCache;
    private static Gson gson = new GsonBuilder().create();
    private int launchcount = 0;


    /**
     * Create a new Nest object
     *
     * @param config parameter settings
     * @param count  The number of iterations to calculate
     */
    public Nest(Config config, int count) {
        this.config = config;
        this.loopCount = count;
        nfpCache = new HashMap<>();
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
        /**
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
        launchcount = 0;
        Result best = null;
        List<List<Placement>> appliedPlacement = null;

        for (int i = 0; i < loopCount; i++) {
            Result result = launchWorkers(tree, binPolygon, config);
            if (best != null) log(best.fitness);
            // if result gets value less than fitness, it will be the new values of best fitness
            if (i == 0 || best.fitness > result.fitness) {
                best = result;
                appliedPlacement = applyPlacement(best, tree);
                notifyObserver(appliedPlacement);
            }
            notifyObserver(result);
        }
        return appliedPlacement;
    }

    // observable /observe pattern
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

    public double computeUseRate(Result best, List<NestPath> tree) {
        // Log new result
        double sumarea = 0;
        double totalarea = Config.BIN_HEIGHT * best.fitness;
        for (List<PathPlacement> element : best.placements) {
            // totalarea += Math.abs(GeometryUtil.polygonArea(binPolygon));
            for (PathPlacement element2 : element) {
                sumarea += Math.abs(GeometryUtil.polygonArea(tree.get(element2.id)));
            }
        }
        return (sumarea / totalarea) * 100;
    }

    /**
     * Applicazione dell'algoritmo di nesting
     *
     * @param tree       lista di tutti i poligoni da disporre
     * @param binPolygon superficie principale su cui disporre i poligoni
     * @param config     confiugurazione standard
     * @return bestResult
     */
    public Result launchWorkers(List<NestPath> tree, NestPath binPolygon, Config config) {
        launchcount++;
        if (Config.IS_DEBUG) {
            IOUtils.log("launchWorkers(): launching worker " + launchcount);
        }
        // GA is null by default
        if (GA == null) {
            List<NestPath> adam = generateAdamForGeneticAlgorithm(tree);
            GA = new GeneticAlgorithm(adam, binPolygon, config);
        }

        Individual individual = null;
        for (Individual element : GA.population) {
            if (element.getFitness() < 0) {
                individual = element;
                break;
            }
        }

        /*---------------------GENERATION OF CHILDERN---------------------*/
        // dalla seconda iterazione di loopcount nel metodo startNest --> launchcount >= 2
        if (launchcount > 1 && individual == null) {
            GA.generation();
            individual = GA.population.get(1);
        }

        //Above is GA. Now we got a set of candidates
        List<NestPath> placelist = individual.getPlacement();
        List<Integer> rotations = individual.getRotation();

        for (int i = 0; i < placelist.size(); i++) {
            placelist.get(i).setRotation(rotations.get(i));
        }

        /*-------------------------------------CREATE NFP CACHE-------------------------------------*/
        List<NfpPair> nfpPairs = NfpUtils.INSTANCE.createNfpPairs(placelist, binPolygon, rotations);

        System.out.print("launch count: " + launchcount + ": ");
        System.out.print("nfp pairs size " + nfpPairs.size());

        // The first time nfpCache is empty, nfpCache stores Nfp ( List<NestPath> ) formed by two polygons corresponding to nfpKey

        for (NfpPair nfpPair : nfpPairs) {
            System.out.println("Generating nfp a:" + nfpPair.getA().getBid() + ",b:" + nfpPair.getB().getBid());

            ParallelData data = NfpUtil.nfpGenerator(nfpPair);
            if (data == null) {
                System.out.println("Null nfp a: " + nfpPair.getA().getBid() + ",b:" + nfpPair.getB().getBid());
            }

            String tkey = gson.toJson(data.getKey());
            nfpCache.put(tkey, data.value);
        }
        /*---------------------FITNESS COMPUTATION---------------------*/

        IOUtils.debug("Launching placement worker...");
        // Places parts according to the sequence specified by the individual
        Placementworker worker = new Placementworker(binPolygon, config, nfpCache); // --------> uses Placementworker to set fitness value

        List<NestPath> placeListSlice = new ArrayList<>();

        for (int i = 0; i < placelist.size(); i++) {
            placeListSlice.add(new NestPath(placelist.get(i)));
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

}
