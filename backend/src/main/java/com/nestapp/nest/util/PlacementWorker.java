package com.nestapp.nest.util;

import com.nestapp.nest.config.Config;
import com.nestapp.nest.data.Bound;
import com.nestapp.nest.data.NestPath;
import com.nestapp.nest.data.PathPlacement;
import com.nestapp.nest.data.Segment;
import com.nestapp.nest.nfp.NfpCacheReader;
import com.nestapp.nest.nfp.NfpCacheRepository;
import com.nestapp.nest.nfp.NfpKey;
import com.nestapp.nest.util.coor.ClipperCoor;
import de.lighti.clipper.*;
import de.lighti.clipper.Point.LongPoint;

import java.util.ArrayList;
import java.util.List;


public class PlacementWorker {

    private final NfpCacheRepository nfpCache;

    private final NfpCacheReader nfpCacheReader;

    public PlacementWorker(
        NfpCacheRepository nfpCache,
        NfpCacheReader nfpCacheReader
    ) {
        this.nfpCache = nfpCache;
        this.nfpCacheReader = nfpCacheReader;
    }

    /**
     * According to the plate list and the rotation angle list, calculate the position of the plate on the
     * bottom plate through nfp, and return the fitness of this population
     */
    public List<PathPlacement> placePaths(NestPath binPolygon, List<NestPath> paths) {
        // rotazione dei NestPaths passati (paths)
        List<NestPath> rotated = new ArrayList<>();
        for (int i = 0; i < paths.size(); i++) {
            NestPath r = GeometryUtil.rotatePolygon2Polygon(paths.get(i), paths.get(i).getRotation());
            r.setRotation(paths.get(i).getRotation());
            rotated.add(r);
        }
        paths = rotated;

        //used with multiple bins

        List<NestPath> placed = new ArrayList<>();        // polygons (NestPath) to place
        List<PathPlacement> placements = new ArrayList<>();    // coordinates

        // Loops over all the polygons (paths)
        for (int i = 0; i < paths.size(); i++) {
            NestPath path = paths.get(i);
            List<NfpKey> keysToCache = new ArrayList<>();
            //inner NFP	***************************************************************

            final NfpKey binKey = new NfpKey(binPolygon.getBid(), path.getBid(), true, 0, path.getRotation());

            keysToCache.add(binKey);

            for (NestPath element : placed) {
                NfpKey key = new NfpKey(element.getBid(), path.getBid(), false, element.getRotation(), path.getRotation());
                keysToCache.add(key);
            }

            nfpCache.prepareCacheForKeys(keysToCache);

            List<NestPath> binNfp = nfpCacheReader.get(binKey);

            PathPlacement position = null;
            if (placed.size() == 0) {
                // first placement , put it on the left
                for (NestPath element : binNfp) {
                    for (int k = 0; k < element.size(); k++) {
                        if (position == null || element.get(k).x - path.get(0).x < position.x) {
                            position = new PathPlacement(
                                element.get(k).x - path.get(0).x,
                                element.get(k).y - path.get(0).y,
                                path.getBid(),
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
                NfpKey key = new NfpKey(
                    placed.get(j).getBid(),
                    path.getBid(),
                    false,
                    placed.get(j).getRotation(),
                    path.getRotation()
                );
                List<NestPath> nfp = nfpCacheReader.get(key);

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

            if (finalNfp.isEmpty()) {
                continue;
            }

            List<NestPath> f = new ArrayList<>();
            for (Path element : finalNfp) {
                f.add(toNestCoordinates(element));
            }

            double minarea = Double.MAX_VALUE;

            for (NestPath element : f) {
                if (Math.abs(GeometryUtil.polygonArea(element)) < 2) {
                    continue;
                }
                for (int k = 0; k < element.size(); k++) {
                    NestPath allpoints = new NestPath();
                    for (int m = 0; m < placed.size(); m++) {
                        for (int n = 0; n < placed.get(m).size(); n++) {
                            allpoints.add(new Segment(placed.get(m).get(n).x + placements.get(m).x,
                                placed.get(m).get(n).y + placements.get(m).y));
                        }
                    }
                    PathPlacement shifvector = new PathPlacement(
                        element.get(k).x - path.get(0).x,
                        element.get(k).y - path.get(0).y,
                        path.getBid(),
                        path.getRotation()
                    );
                    for (int m = 0; m < path.size(); m++) {
                        allpoints.add(new Segment(path.get(m).x + shifvector.x, path.get(m).y + shifvector.y));
                    }
                    Bound rectBounds = GeometryUtil.getPolygonBounds(allpoints);

                    double area = rectBounds.width * rectBounds.height;

                    if (area < minarea || GeometryUtil.almostEqual(minarea, area)) {
                        minarea = area;
                        position = shifvector;
                    }
                }
            }
            if (position != null) {
                placed.add(path);
                placements.add(position);
            }
        }

        if (!placements.isEmpty() && placed.size() == paths.size()) {
            return placements;
        } else {
            return null;
        }
    }


    /**
     * coordinate conversion required to interact with the clipper library
     */
    public static Path scaleUp2ClipperCoordinates(NestPath polygon) {
        Path p = new Path();
        for (Segment s : polygon.getSegments()) {
            ClipperCoor cc = CommonUtil.toClipperCoor(s.x, s.y);
            p.add(new Point.LongPoint(cc.getX(), cc.getY()));
        }
        return p;
    }

    public static NestPath toNestCoordinates(Path polygon) {
        NestPath clone = new NestPath();
        for (LongPoint element : polygon) {
            Segment s = new Segment((double) element.getX() / Config.CLIIPER_SCALE, (double) element.getY() / Config.CLIIPER_SCALE);
            clone.add(s);
        }
        return clone;
    }
}
