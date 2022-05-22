package com.qunhe.util.nest.jenetics_with_NFP;

import java.util.List;

import com.qunhe.util.nest.config.Config;
import com.qunhe.util.nest.data.NestPath;
import com.qunhe.util.nest.data.Segment;
import com.qunhe.util.nest.util.CommonUtil;
import com.qunhe.util.nest.util.GeometryUtil;

public class Util {
	
	/**
	 * translate bin to the origin and ensure it can be used for nesting
	 * @param bin Nespath to clean
	 * @return Nestpath cleaned
	 */
	public static NestPath cleanBin(NestPath bin)
	{
		
		NestPath binPolygon = CommonUtil.cleanNestPath(bin);
	    if(Config.BOUND_SPACING > 0 ){
	        List<NestPath> offsetBin = CommonUtil.polygonOffset(binPolygon , - Config.BOUND_SPACING);
	        if(offsetBin.size() == 1 )
	            binPolygon = offsetBin.get(0);
	    }
		
		binPolygon.setId(-1);
	    double xbinmax = binPolygon.get(0).x;
	    double xbinmin = binPolygon.get(0).x;
	    double ybinmax = binPolygon.get(0).y;
	    double ybinmin = binPolygon.get(0).y;
	    // Find min max
	    for(int i = 1 ; i<binPolygon.size(); i++){
	        if(binPolygon.get(i).x > xbinmax ){
	            xbinmax = binPolygon.get(i).x;
	        }
	        else if (binPolygon.get(i).x < xbinmin ){
	            xbinmin = binPolygon.get(i) .x;
	        }

	        if(binPolygon.get(i).y > ybinmax ){
	            ybinmax = binPolygon.get(i).y;
	        }
	        else if (binPolygon.get(i). y <ybinmin ){
	            ybinmin = binPolygon.get(i).y;
	        }
	    }

	    /*binpath is translated to origin*/
	    for(int i=0; i<binPolygon.size(); i++){				
	        binPolygon.get(i).x -= xbinmin;
	        binPolygon.get(i).y -= ybinmin;
	    }
	    
	    if(GeometryUtil.polygonArea(binPolygon) > 0 )
	        binPolygon.reverse();
	    
	    return binPolygon;
	}
	
	/**
	 * Ensure every polygon can be nested
	 * @param tree List<NestPath> to be clean
	 */
	public static void cleanTree(List<NestPath> tree)
	{
		for (NestPath element : tree) {
	        Segment start = element.get(0);
	        Segment end = element.get(element.size()-1);
	        if(start == end || GeometryUtil.almostEqual(start.x , end.x) && GeometryUtil.almostEqual(start.y , end.y)){
	            element.pop();
	        }
	        if(GeometryUtil.polygonArea(element) > 0 )
	            element.reverse();
	    }
		
	}
	
	/**
	 * Return a rectangle NestPath starting in origin with width and height specified
	 * @author Alberto Gambarara
	 * @param width
	 * @param height
	 * @return NestPath
	 */
	public static NestPath createRectPolygon(double width, double height)
	{
		NestPath np = new NestPath();
		np.add(0, 0);
		np.add(width, 0);
		np.add(width, height);
		np.add(0, height);
		return np;
		
	}

}
