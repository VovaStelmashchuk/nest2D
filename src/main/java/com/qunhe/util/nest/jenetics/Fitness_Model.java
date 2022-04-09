package com.qunhe.util.nest.jenetics;

import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.apache.batik.ext.awt.geom.Polygon2D;

import com.qunhe.util.nest.data.NestPath;

import io.jenetics.Genotype;

public class Fitness_Model {
	
	public List<NestPath> list;
	
	public Fitness_Model(List<NestPath> l) {
		
		this.list = l;
		
	}
	
	
	private static double overlap(Polygon2D p1, Polygon2D p2)
    {
		
		Area area = new Area(p1);
		area.intersect(new Area(p2));
		return area.getBounds().getWidth()*area.getBounds().getHeight();
		
    }
	
	
	
	public double scalarFitness(final Genotype model)
    {
		
        ArrayList<NestPath> new_list = (ArrayList<NestPath>) Model_Factory.convert(model,list);
        double penalty = 0;
        for (int i=0; i< list.size(); i++){
            for (int j=0; j< list.size(); j++){
                if(i!=j)
                    penalty += overlap(new_list.get(i).toPolygon2D(), new_list.get(j).toPolygon2D());
                
            }
        }
        return penalty;
    }

    public Function<Genotype, Double> getFitness()
    {
        return this::scalarFitness;
    }

}
