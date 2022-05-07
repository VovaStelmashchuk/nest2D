package com.qunhe.util.nest.jenetics;

import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.lang.model.type.IntersectionType;

import org.apache.batik.ext.awt.geom.Polygon2D;
import org.apache.xmlgraphics.image.loader.util.Penalty;

import com.qunhe.util.nest.data.Bound;
import com.qunhe.util.nest.data.NestPath;
import com.qunhe.util.nest.data.Segment;
import com.qunhe.util.nest.util.CommonUtil;
import com.qunhe.util.nest.util.GeometryUtil;

import io.jenetics.Genotype;

public class Fitness_Model {
	
	public List<NestPath> list;
	Double binWidth,binHeight;
	
	/**
	 * @param l		List of polygons
	 * @param binW	Width of bin polygon
	 * @param binH	Height of bin polygon
	 */
	public Fitness_Model(List<NestPath> l, double binW, double binH) {
		
		this.list = l;
		this.binHeight=binH;
		this.binWidth=binW;
		
	}
	
	
	/**
	 * @param p1	1st polygon
	 * @param p2	2nd	polygon
	 * @return		value of the area of the rectangle that contains the polygon where p1 and p2 overlap
	 */
	public static double overlapDouble(Polygon2D p1, Polygon2D p2)
    {		
		Area area = new Area(p1);
		area.intersect(new Area(p2));
		return Math.abs(area.getBounds().getWidth()*area.getBounds().getHeight());		
    }
	
	/**
	 * @param p1	1st NestPath
	 * @param p2	2nd	NestPath
	 * @return		true if p1 and p2 overlap USES GeometryUtil.intersect THAT SEEMS TO NOT WORK CORRECTLY ALL THE TIME
	 */
	public static boolean overlapBool(NestPath p1, NestPath p2)
    {		
		return  GeometryUtil.intersect(p1, p2);
    }
	
	
	/**
	 * @param model
	 * @return double value of the fitness, in this case the area that contains all the polygons divided by the sum of the areas of all the polygons
	 */
	public double scalarFitness(final Genotype model)
    {		
		double maxX=0;
		double maxY=0;
		double penalty=0;
		double totArea=0;
		ArrayList<NestPath> polys = CommonUtil.cloneArrayListNestpath(list);
		Model_Factory.convert(model, polys);
		
		for(int i=0; i<polys.size();i++)
		{
			NestPath p = polys.get(i);
			totArea+=Math.abs(GeometryUtil.polygonArea(p));
//			if(p.getMaxX()>binWidth) penalty+=p.getMaxX()-binWidth;
//			if(p.getMaxY()>binHeight) penalty+=p.getMaxY()-binHeight;
//			if(p.getMinX()<0) penalty+=-p.getMinX()*Math.abs(p.area);
//			if(p.getMinY()<0) penalty+=-p.getMinY()*Math.abs(p.area);
//
//			penalty+= (p.getMaxX()*4+ p.getMaxY()*2);
			
			
			List<Segment> ls = polys.get(i).getSegments();
			
			for(int j=0; j<ls.size();j++)
			{
				if (ls.get(j).getX()>maxX) maxX = ls.get(j).getX();
				if (ls.get(j).getY()>maxY) maxY = ls.get(j).getY();
			}			
		}		
		
//		penalty += maxX*list.size()*2;
//		penalty += maxY*list.size();
		//penalty+= maxX*maxY;
		

        //double area = rectBounds.getWidth() * 2 + rectBounds.getHeight();		
       // ArrayList<NestPath> new_list = (ArrayList<NestPath>) Model_Factory.convert(model,list);
        
		
		///TODO set overlapping solutions as invalid with constraints
		///TODO 
		penalty = (maxX*maxY)/totArea;
//        for (int i=0; i< polys.size(); i++){
//            for (int j=0; j< polys.size(); j++){
//                if(i!=j) 
//                {
//                	double add= overlapDouble(polys.get(i).toPolygon2D(), polys.get(j).toPolygon2D());
//                    penalty += add;
//                    if(add>0) penalty+=list.size()*100;
//                    
////                	NestPath p1 = polys.get(i);
////                	NestPath p2 = polys.get(j);
////
////                	if(GeometryUtil.intersect(p1, p2)) penalty+=50;
//                }
//                
//            }
//        }
        
        if(penalty<=0)
        {
        	return 0;
        }
        return penalty;
    }

    public Function<Genotype, Double> getFitness()
    {
        return this::scalarFitness;
    }

}
