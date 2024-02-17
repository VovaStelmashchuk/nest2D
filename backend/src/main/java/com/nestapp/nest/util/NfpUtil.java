package com.nestapp.nest.util;

import com.nestapp.nest.data.NestPath;
import com.nestapp.nest.nfp.NfpPair;

import java.util.List;

/**
 * @author yisa
 */
public class NfpUtil {

    public static List<NestPath> nfpGenerator(NfpPair pair) {
        boolean searchEdges = false;

        NestPath A = GeometryUtil.rotatePolygon2Polygon(pair.getA(), pair.key.getArotation());
        NestPath B = GeometryUtil.rotatePolygon2Polygon(pair.getB(), pair.key.getBrotation());

        List<NestPath> nfp;
        if (pair.key.isInside()) {
            if (GeometryUtil.isRectangle(A, 0.001)) {
                nfp = GeometryUtil.noFitPolygonRectangle(A, B);
            } else {
                nfp = GeometryUtil.noFitPolygon(A, B, true, searchEdges);
            }
            if (nfp != null && !nfp.isEmpty()) {
                for (NestPath element : nfp) {
                    if (GeometryUtil.polygonArea(element) > 0) {
                        element.reverse();
                    }
                }
            }
        } else {
            nfp = GeometryUtil.minkowskiDifference(A, B);
            // sanity check
            if (nfp.isEmpty()) {
                return null;
            }
            //TODO This test is causing null result
            for (int i = 0; i < nfp.size(); i++) {
                // TODO why here: normally the area of nfp should be greater than A
                if (Math.abs(GeometryUtil.polygonArea(nfp.get(i))) < Math.abs(GeometryUtil.polygonArea(A))) {
                    nfp.remove(i);

                    return null;
                }
            }
            if (nfp.isEmpty()) {
                return null;
            }

            for (int i = 0; i < nfp.size(); i++) {
                if (GeometryUtil.polygonArea(nfp.get(i)) > 0) {
                    nfp.get(i).reverse();
                }

                if (i > 0) {
                    if (GeometryUtil.pointInPolygon(nfp.get(i).get(0), nfp.get(0))) {
                        if (GeometryUtil.polygonArea(nfp.get(i)) < 0) {
                            nfp.get(i).reverse();
                        }
                    }
                }
            }

        }
        return nfp;
    }
}
