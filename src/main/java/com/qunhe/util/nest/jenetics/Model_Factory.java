package com.qunhe.util.nest.jenetics;

import java.util.ArrayList;
import java.util.List;

import com.qunhe.util.nest.Nest;
import com.qunhe.util.nest.algorithm.GeneticAlgorithm;
import com.qunhe.util.nest.data.Bound;
import com.qunhe.util.nest.data.NestPath;
import com.qunhe.util.nest.data.Placement;
import com.qunhe.util.nest.data.Segment;
import com.qunhe.util.nest.util.GeometryUtil;

import io.jenetics.DoubleChromosome;
import io.jenetics.DoubleGene;
import io.jenetics.Genotype;
import io.jenetics.IntegerChromosome;
import io.jenetics.util.Factory;

public class Model_Factory {
	
	public static int nGenes =3;
	public static double binWidth;
	public static double binHeight;
	
	public static Factory<Genotype<DoubleGene>> of (List<NestPath> list, double binW, double binH)
    {
		binHeight=binH;
		binWidth=binW;
        List<DoubleChromosome> arr = new ArrayList<DoubleChromosome>();
        for(int i=0; i<list.size()*nGenes; i+=nGenes)
        {
            arr.add(DoubleChromosome.of(-binW/2, binW/2)); ///TODO
            arr.add(DoubleChromosome.of(-binH/2,binH/2));
            arr.add(DoubleChromosome.of(0,360));	//TODO GeneticAlgorithm.randomangle(nestpath)
            //GeneticAlgorithm.randomAngle(null)
        }
        return Genotype.of(arr);
    }
		
	
	/**
	 * @param solution
	 * @param list		list of NestPath MODIFIED by the method
	 * @return			appliedplacement
	 */
	public static List<List<Placement>> convert(Genotype<DoubleGene> solution, List<NestPath> list)
    {
       
		List<List<Placement>> res = new ArrayList<List<Placement>>();
		List<Placement> places = new ArrayList<Placement>();
        for(int i=0; i < solution.length(); i+= nGenes)
        {

        	NestPath p = list.get(i/nGenes);
        	
        	double x = (double)solution.get(i).gene().allele();
        	double y = (double)solution.get(i+1).gene().allele();
        	double rotation = (double)solution.get(i+2).gene().allele();
        	rotation=0;
        	int intRotation = (int) Math.round(rotation);
        	
        	Segment s = new Segment(x,y);
        	
        	//p= GeometryUtil.rotatePolygon2Polygon(p, intRotation);    	
        	
        	
        	//Bound rotateBound = GeometryUtil.rotatePolygon(p,intRotation);		// assegna nuovi Bound ad ogni NestPath, di conseguenza lo ruota
//        	if(rotateBound.width < binWidth && rotateBound.height < binHeight){	// se larghezza e altezza del poligono ruotato sono minori della posizione originale
//        		continue;
//        	}
//        	else{
//        		int safeAngle = GeneticAlgorithm.randomAngle(p);
//        		intRotation = safeAngle;
//        	}
        	
 
//			double xx = p.getMinX();
//			double yy = p.getMinY();
//			p.Zerolize();
//			p= GeometryUtil.rotatePolygon2Polygon(p, intRotation);
//
//			p.translate(Math.abs(p.getMinX()), Math.abs(p.getMinY()));			
//			p.translate(xx, yy);
//        	
        	
        	
        	Placement pl = new Placement(p.getBid(),s,intRotation);
        	
        	p.translate(pl.translate.x, pl.translate.y);
        	p.setRotation(intRotation);
        	
        	places.add(pl);
        	
        	//list.get(i/2).translate((double)solution.get(i).gene().allele(), (double)solution.get(i+1).gene().allele());
        	//list.get(i).setRotation(list.get(i).getPossibleRotations()[(int) Math.round(solution.get(i+2).gene().allele())]);
        	
        }
        
        
        ///Push to origin
//        double minX=Double.MAX_VALUE;
//        double minY=Double.MAX_VALUE;
//        for(int i =0; i<list.size();i++)
//        {
//        	if(list.get(i).getMinX()<minX) minX=list.get(i).getMinX();
//        	if(list.get(i).getMinY()<minY) minY=list.get(i).getMinY();
//        }
//        	
//        if(minX > 0 || minY > 0)
//        {
//        	int i=0;
//        	for(NestPath p : list)
//        	{
//        		p.translate(-minX, -minY);
//        		places.get(i).translate.x -=minX;
//        		places.get(i).translate.y -=minY;
//        		i++;
//        	}        	
//        }
        
        
        res.add(places);
        return res;
    }
	

}
