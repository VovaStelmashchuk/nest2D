package com.nestapp.nest.util;

import com.nestapp.nest.config.Config;
import com.nestapp.nest.data.NestPath;
import com.nestapp.nest.data.Segment;
import com.nestapp.nest.util.coor.ClipperCoor;
import com.nestapp.nest.util.coor.NestCoor;
import de.lighti.clipper.*;
import de.lighti.clipper.Point.LongPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yisa
 */
public class CommonUtil {

    /**
     * Coordinate conversion
     */
    static ClipperCoor toClipperCoor(double x, double y) {
        return new ClipperCoor((long) (x * Config.CLIIPER_SCALE), (long) (y * Config.CLIIPER_SCALE));
    }

    /**
     * Coordinate conversion
     */
    private static NestCoor toNestCoor(long x, long y) {
        return new NestCoor(((double) x / Config.CLIIPER_SCALE), ((double) y / Config.CLIIPER_SCALE));
    }

    private static NestPath clipperToNestPath(Path polygon) {
        NestPath normal = new NestPath();
        for (LongPoint element : polygon) {
            NestCoor nestCoor = toNestCoor(element.getX(), element.getY());
            normal.add(new Segment(nestCoor.getX(), nestCoor.getY()));
        }
        return normal;
    }

    public static void offsetTree(List<NestPath> t, double offset) {
        for (NestPath element : t) {
            List<NestPath> offsetPaths = polygonOffset(element, offset);
            if (offsetPaths.size() == 1) {
                element.clear();
                NestPath from = offsetPaths.get(0);

                for (Segment s : from.getSegments()) {
                    element.add(s);
                }
            }
            if (!element.getChildren().isEmpty()) {
                offsetTree(element.getChildren(), -offset);
            }
        }
    }

    public static List<NestPath> polygonOffset(NestPath polygon, double offset) {
        List<NestPath> result = new ArrayList<>();
        if (offset == 0 || GeometryUtil.almostEqual(offset, 0)) {
            return result;
        }
        Path p = new Path();
        for (Segment s : polygon.getSegments()) {
            ClipperCoor cc = CommonUtil.toClipperCoor(s.getX(), s.getY());
            p.add(new Point.LongPoint(cc.getX(), cc.getY()));
        }

        int miterLimit = 2;
        ClipperOffset co = new ClipperOffset(miterLimit, Config.CURVE_TOLERANCE * Config.CLIIPER_SCALE);
        co.addPath(p, Clipper.JoinType.ROUND, Clipper.EndType.CLOSED_POLYGON);

        Paths newpaths = new Paths();
        co.execute(newpaths, offset * Config.CLIIPER_SCALE);

        // The length here is 1, which is what we want
        for (Path newpath : newpaths) {
            result.add(CommonUtil.clipperToNestPath(newpath));
        }

        if (offset > 0) {
            NestPath from = result.get(0);
            if (GeometryUtil.polygonArea(from) > 0) {
                from.reverse();
            }
            from.add(from.get(0));
            from.getSegments().remove(0);
        }


        return result;
    }
}
