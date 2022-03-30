package com.qunhe.util.nest.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.qunhe.util.nest.config.Config;
import com.qunhe.util.nest.data.Bound;
import com.qunhe.util.nest.data.NestPath;
import com.qunhe.util.nest.data.NfpKey;
import com.qunhe.util.nest.data.Result;
import com.qunhe.util.nest.data.Segment;
import com.qunhe.util.nest.data.Vector;
import com.qunhe.util.nest.util.coor.ClipperCoor;

import de.lighti.clipper.Clipper;
import de.lighti.clipper.DefaultClipper;
import de.lighti.clipper.Path;
import de.lighti.clipper.Paths;
import de.lighti.clipper.Point;
import de.lighti.clipper.Point.LongPoint;


/**
 * @author yisa
 */
public class Placementworker {
    private static Gson gson = new GsonBuilder().create();
    
    public NestPath binPolygon;
    private Config config;
    
    public Map<String, List<NestPath>> nfpCache;

    /**
     * @param binPolygon
     * @param config
     * @param nfpCache   nfpList
     */
    public Placementworker(NestPath binPolygon, Config config, Map<String, List<NestPath>> nfpCache) {
        this.binPolygon = binPolygon;
        this.config = config;
        this.nfpCache = nfpCache;
    }

    /**
     * According to the plate list and the rotation angle list, calculate the position of the plate on the
     * bottom plate through nfp, and return the fitness of this population
     *
     * @param paths
     * @return
     */
    public Result placePaths(List<NestPath> paths) {
    	
    	// rotazione dei NestPaths passati (paths)
        List<NestPath> rotated = new ArrayList<>();
        for (int i = 0; i < paths.size(); i++) {
            NestPath r = GeometryUtil.rotatePolygon2Polygon(paths.get(i), paths.get(i).getRotation());
            r.setRotation(paths.get(i).getRotation());
            r.setPossibleRotations(paths.get(i).getPossibleRotations());
            r.setSource(paths.get(i).getSource());
            r.setId(paths.get(i).getId());
            rotated.add(r);
        }
        paths = rotated;	
        
        
        List<List<Vector>> allplacements = new ArrayList<>();
        // Now the fitness is defined as the width of material used.
        double fitness = 0;
        double binarea = Math.abs(GeometryUtil.polygonArea(this.binPolygon));
        String key = null;
        List<NestPath> nfp = null;

        // Cicla tutti i NestPath passati
        while (paths.size() > 0) {

            List<NestPath> placed = new ArrayList<>();		// poligoni (NestPath) da piazzare
            List<Vector> placements = new ArrayList<>();	// coordinate

            //fitness += 1;
            double minwidth = Double.MAX_VALUE;				// valore che verrà assegnato alla fitness
            
            // cicla tutti i poligoni (paths)
            for (int i = 0; i < paths.size(); i++) {
                NestPath path = paths.get(i);

                
                //inner NFP	***************************************************************
                key = gson.toJson(new NfpKey(-1, path.getId(), true, 0, path.getRotation()));
                if (!nfpCache.containsKey(key)) {
                    continue;
                }
                List<NestPath> binNfp = nfpCache.get(key);
                // ensure exists
                boolean error = false;
                for (NestPath element : placed) {
                    key = gson.toJson(new NfpKey(element.getId(), path.getId(), false, element.getRotation(), path.getRotation()));
                    if (nfpCache.containsKey(key)) nfp = nfpCache.get(key);
                    else {
                        error = true;
                        break;
                    }
                }
                if (error) {
                    continue;
                }//***************************************************************

                
                Vector position = null;
                if (placed.size() == 0) {
                    // first placement , put it on the left
                   for (NestPath element : binNfp) {
                        for (int k = 0; k < element.size(); k++) {
                            if (position == null || element.get(k).x - path.get(0).x < position.x) {
                                position = new Vector(
                                        element.get(k).x - path.get(0).x,
                                        element.get(k).y - path.get(0).y,
                                        path.getId(),
                                        path.getRotation()
                                );
                            }
                        }
                    }
                    placements.add(position);
                    placed.add(path);
                    continue;
                }

                Paths clipperBinNfp = new Paths();

                for (NestPath binNfpj : binNfp) {
                    clipperBinNfp.add(scaleUp2ClipperCoordinates(binNfpj));
                }
                DefaultClipper clipper = new DefaultClipper();
                Paths combinedNfp = new Paths();

                for (int j = 0; j < placed.size(); j++) {
                    key = gson.toJson(new NfpKey(placed.get(j).getId(), path.getId(), false, placed.get(j).getRotation(), path.getRotation()));
                    nfp = nfpCache.get(key);
                    if (nfp == null) {
                        continue;
                    }

                    for (NestPath element : nfp) {
                        Path clone = scaleUp2ClipperCoordinates(element);
                        for (LongPoint element2 : clone) {
                            long clx = element2.getX();
                            long cly = element2.getY();
                            element2.setX(clx + (long) (placements.get(j).x * Config.CLIIPER_SCALE));
                            element2.setY(cly + (long) (placements.get(j).y * Config.CLIIPER_SCALE));
                        }
                        // TODO why clean again here
                        clone = clone.cleanPolygon(0.0001 * Config.CLIIPER_SCALE);
                        double area = Math.abs(clone.area());
                        if (clone.size() > 2 && area > 0.1 * Config.CLIIPER_SCALE * Config.CLIIPER_SCALE) {
                            clipper.addPath(clone, Clipper.PolyType.SUBJECT, true);
                        }
                    }
                }
                if (!clipper.execute(Clipper.ClipType.UNION, combinedNfp, Clipper.PolyFillType.NON_ZERO, Clipper.PolyFillType.NON_ZERO)) {
                    continue;
                }

                //difference with bin polygon
                Paths finalNfp = new Paths();
                clipper = new DefaultClipper();

                clipper.addPaths(combinedNfp, Clipper.PolyType.CLIP, true);
                clipper.addPaths(clipperBinNfp, Clipper.PolyType.SUBJECT, true);
                if (!clipper.execute(Clipper.ClipType.DIFFERENCE, finalNfp, Clipper.PolyFillType.NON_ZERO, Clipper.PolyFillType.NON_ZERO)) {
                    continue;
                }

                finalNfp = finalNfp.cleanPolygons(0.0001 * Config.CLIIPER_SCALE);
                for (int j = 0; j < finalNfp.size(); j++) {
                    double area = Math.abs(finalNfp.get(j).area());
                    if (finalNfp.get(j).size() < 3 || area < 0.1 * Config.CLIIPER_SCALE * Config.CLIIPER_SCALE) {
                        finalNfp.remove(j);
                        j--;
                    }
                }

                if (finalNfp == null || finalNfp.size() == 0) {
                    continue;
                }

                List<NestPath> f = new ArrayList<>();
                for (Path element : finalNfp) {
                    f.add(toNestCoordinates(element));
                }

                List<NestPath> finalNfpf = f;
                double minarea = Double.MIN_VALUE;
                double minX = Double.MAX_VALUE;
                NestPath nf = null;
                double area = Double.MIN_VALUE;
                Vector shifvector = null;
                for (NestPath element : finalNfpf) {
                    nf = element;
                    if (Math.abs(GeometryUtil.polygonArea(nf)) < 2) {	
                        continue;
                    }
                    for (int k = 0; k < nf.size(); k++) {
                        NestPath allpoints = new NestPath();
                        for (int m = 0; m < placed.size(); m++) {
                            for (int n = 0; n < placed.get(m).size(); n++) {
                                allpoints.add(new Segment(placed.get(m).get(n).x + placements.get(m).x,
                                        placed.get(m).get(n).y + placements.get(m).y));
                            }
                        }
                        shifvector = new Vector(
                                nf.get(k).x - path.get(0).x,
                                nf.get(k).y - path.get(0).y,
                                path.getId(),
                                path.getRotation(),
                                combinedNfp
                        );
                        for (int m = 0; m < path.size(); m++) {
                            allpoints.add(new Segment(path.get(m).x + shifvector.x, path.get(m).y + shifvector.y));
                        }
                        Bound rectBounds = GeometryUtil.getPolygonBounds(allpoints);

                        area = rectBounds.getWidth() * 2 + rectBounds.getHeight();
                        if (minarea == Double.MIN_VALUE
                                || area < minarea
                                || (GeometryUtil.almostEqual(minarea, area)
                                && (minX == Double.MIN_VALUE || shifvector.x < minX))) {
                            minarea = area;
                            minwidth = rectBounds.getWidth();
                            position = shifvector;
                            minX = shifvector.x;
                        }
                    }
                }
                if (position != null) {

                    placed.add(path);				// viene aggiunto il poligono 
                    placements.add(position);
                }
            }
            if (minwidth != Double.MAX_VALUE) {
                //fitness += minwidth ;/// binarea;
            	fitness = minwidth;
            }


            for (int i = 0; i < placed.size(); i++) {
                int index = paths.indexOf(placed.get(i));
                if (index >= 0) {
                    paths.remove(index);
                }
            }

            if (placements != null && placements.size() > 0) {
                // Add a new material
                allplacements.add(placements);
            } else {
                break; // something went wrong
            }

        }// End of while(paths.size>0)
        // there were paths that couldn't be placed
        //fitness += Config.BIN_WIDTH * paths.size();		removed because	path.size() is always 0
        return new Result(allplacements, fitness, paths, binarea);
    }


    /**
     * coordinate conversion required to interact with the clipper library
     *
     * @param polygon
     * @return
     */
    static Path scaleUp2ClipperCoordinates(NestPath polygon) {
        Path p = new Path();
        for (Segment s : polygon.getSegments()) {
            ClipperCoor cc = CommonUtil.toClipperCoor(s.x, s.y);
            p.add(new Point.LongPoint(cc.getX(), cc.getY()));
        }
        return p;
    }

    static NestPath toNestCoordinates(Path polygon) {
        NestPath clone = new NestPath();
        for (LongPoint element : polygon) {
            Segment s = new Segment((double) element.getX() / Config.CLIIPER_SCALE, (double) element.getY() / Config.CLIIPER_SCALE);
            clone.add(s);
        }
        return clone;
    }

}
