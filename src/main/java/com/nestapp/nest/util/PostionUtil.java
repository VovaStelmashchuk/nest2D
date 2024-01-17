package com.nestapp.nest.util;

import java.util.List;

import com.nestapp.nest.data.NestPath;

/**
 * Receive a set of NestPath, and transform its coordinates to the two-dimensional coordinate plane
 */
public class PostionUtil {

    public static List<NestPath> positionTranslate4Path(double x, double y, List<NestPath> paths) {
        for (NestPath path : paths) {
            path.translate(x, y);
            y = path.getMaxY() + 10;
        }
        return paths;
    }
}
