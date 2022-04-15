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
	
	public Fitness_Model(List<NestPath> l, double binW, double binH) {
		
		this.list = l;
		this.binHeight=binH;
		this.binWidth=binW;
		
	}
	
	
	private static double overlapDouble(Polygon2D p1, Polygon2D p2)
    {		
		Area area = new Area(p1);
		area.intersect(new Area(p2));
		return area.getBounds().getWidth()*area.getBounds().getHeight();		
    }
	
	private static boolean overlapBool(NestPath p1, NestPath p2)
    {		
		return  GeometryUtil.intersect(p1, p2);
    }
	
	
	public double scalarFitness(final Genotype model)
    {
		
		double maxX=0;
		double maxY=0;
		double penalty=0;
		
		ArrayList<NestPath> polys = CommonUtil.cloneArrayListNestpath(list);
		Model_Factory.convert(model, polys);
		
		for(int i=0; i<polys.size();i++)
		{
			NestPath p = polys.get(i);
			if(p.getMaxX()>binWidth) penalty+=p.getMaxX()-binWidth;
			if(p.getMaxY()>binHeight) penalty+=p.getMaxY()-binHeight;
			if(p.getMinX()<0) penalty+=-p.getMinX()*Math.abs(p.area);
			if(p.getMinY()<0) penalty+=-p.getMinY()*Math.abs(p.area);

			penalty+= (p.getMinX()*8+ p.getMinY())/4;
			
			
			List<Segment> ls = polys.get(i).getSegments();
			
			for(int j=0; j<ls.size();j++)
			{
				if (ls.get(j).getX()>maxX) maxX = ls.get(j).getX();
				if (ls.get(j).getY()>maxY) maxY = ls.get(j).getY();
			}			
		}		
		
		penalty += maxX*20;
		penalty += 10*maxY;

		

        //double area = rectBounds.getWidth() * 2 + rectBounds.getHeight();
		
		
       // ArrayList<NestPath> new_list = (ArrayList<NestPath>) Model_Factory.convert(model,list);
        
        for (int i=0; i< polys.size(); i++){
            for (int j=0; j< polys.size(); j++){
                if(i!=j) 
                {
                	double add= overlapDouble(polys.get(i).toPolygon2D(), polys.get(j).toPolygon2D());
                    penalty += add;
                    if(add>0) penalty+=100;
                    
//                	NestPath p1 = polys.get(i);
//                	NestPath p2 = polys.get(j);
//
//                	if(GeometryUtil.intersect(p1, p2)) penalty+=50;
                }
                
            }
        }
        
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
