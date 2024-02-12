package com.nestapp.nest.util;

import java.util.ArrayList;
import java.util.List;

import com.nestapp.nest.config.Config;
import com.nestapp.nest.data.NestPath;
import com.nestapp.nest.data.NfpPair;
import com.nestapp.nest.data.Bound;
import com.nestapp.nest.data.ParallelData;

/**
 * @author yisa
 */
public class NfpUtil {

    /**
     * Get a pair of polygons and generate nfp
     * @param pair
     * @param config
     * @return
     */
    public static ParallelData nfpGenerator(NfpPair pair , Config config){
        boolean searchEdges = config.isCONCAVE();
        boolean useHoles = config.isUSE_HOLE();

        NestPath A = GeometryUtil.rotatePolygon2Polygon(pair.getA() , pair.getKey().getArotation());
        NestPath B = GeometryUtil.rotatePolygon2Polygon(pair.getB() , pair.getKey().getBrotation());

        List<NestPath> nfp ;
        if(pair.getKey().isInside()){
            if(GeometryUtil.isRectangle(A,0.001)){
                nfp = GeometryUtil.noFitPolygonRectangle(A,B);
                if(nfp == null ){

                }
            }
            else{
                nfp = GeometryUtil.noFitPolygon(A,B,true,searchEdges);
            }
            if(nfp != null && nfp.size() > 0){
                for (NestPath element : nfp) {
                    if(GeometryUtil.polygonArea(element) > 0 ){
                        element.reverse();
                    }
                }
            }
            else{
                //Warning on null inner NFP
            }
        }
        else{
            int count = 0 ;
            if(searchEdges){

                // NFP Generator TODO  double scale contorl
                nfp = GeometryUtil.noFitPolygon(A,B,false ,searchEdges);
                if(nfp  == null ){

                }
            }
            else{

                nfp = GeometryUtil.minkowskiDifference(A,B);
            }
            // sanity check
            if( nfp == null || nfp.size() == 0 ){

                return null;
            }
            //TODO This test is causing null result
            for(int i = 0; i<nfp.size() ; i++){
                if(!searchEdges || i == 0){
                    // TODO why here: normally the area of nfp should be greater than A
                    if(Math.abs(GeometryUtil.polygonArea(nfp.get(i))) < Math.abs(GeometryUtil.polygonArea(A))){
                        nfp.remove(i);

                        return null;
                    }
                }
            }
            if(nfp.size() == 0 ){

                return null;
            }

            for(int i=0; i<nfp.size(); i++){
                if(GeometryUtil.polygonArea(nfp.get(i)) > 0){
                    nfp.get(i).reverse();
                }

                if(i > 0){
                    if(GeometryUtil.pointInPolygon(nfp.get(i).get(0), nfp.get(0))){
                        if(GeometryUtil.polygonArea(nfp.get(i)) < 0){
                            nfp.get(i).reverse();
                        }
                    }
                }
            }

        }
        if(nfp ==null ){

        }
        return new ParallelData(pair.getKey() , nfp);
    }
}
