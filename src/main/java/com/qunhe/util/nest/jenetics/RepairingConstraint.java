package com.qunhe.util.nest.jenetics;

import java.util.ArrayList;
import java.util.List;

import com.qunhe.util.nest.data.NestPath;
import com.qunhe.util.nest.data.Placement;
import com.qunhe.util.nest.data.Segment;
import com.qunhe.util.nest.util.CommonUtil;
import com.qunhe.util.nest.util.GeometryUtil;

import io.jenetics.DoubleChromosome;
import io.jenetics.DoubleGene;
import io.jenetics.Genotype;
import io.jenetics.Phenotype;
import io.jenetics.engine.Constraint;

public class RepairingConstraint implements Constraint<DoubleGene, Double>{
	
	static List<NestPath> list;
	static int nGenes = Model_Factory.nGenes;
	static double binW = Model_Factory.binWidth;
	static double binH = Model_Factory.binHeight;
	
	public RepairingConstraint(List<NestPath>l) {
		list=l;
	}
	
	/**
	 *Return true if the Phenotype passed is valid, false otherwise
	 */
	@Override
	public boolean test(Phenotype<DoubleGene, Double> pt) {
		
		double[] l = new double[pt.genotype().length()];
		for(int i=0; i < pt.genotype().length(); i++)
        {
        l[i]=pt.genotype().get(i).gene().allele();     
        }
		return isValid(l);		
	}
	
	
	/**
	 * @param arr array representing the list of chromosomes
	 * @return true if the array passed is valid, false otherwise
	 */
	static boolean isValid (double[] arr)
	{

		ArrayList<NestPath> polys = CommonUtil.cloneArrayListNestpath(list);
		
		for(int i=0; i < arr.length; i+= nGenes)
        {

        	NestPath p = polys.get(i/nGenes);        	
        	double x = arr[i];
        	double y = arr[i+1];
        	//double rotation = arr[i+2];
        	double rotation=0.0;//TODO
        	int intRotation = (int) Math.round(rotation);        	
        	Segment s = new Segment(x,y);
        	//p= GeometryUtil.rotatePolygon2Polygon(p, intRotation);  
        	
//        	double xx = p.getMinX();
//			double yy = p.getMinY();
//			p.Zerolize();
//			p= GeometryUtil.rotatePolygon2Polygon(p, intRotation);
//
//			//p.translate(Math.abs(p.getMinX()), Math.abs(p.getMinY()));			
//			p.translate(xx, yy); 
        	Placement pl = new Placement(p.getBid(),s,intRotation);
        	
        	p.translate(pl.translate.x, pl.translate.y);
        	p.setRotation(intRotation);
        	
        	if(p.getMinX()<0 || p.getMinY()<0 || p.getMaxX() > binW || p.getMaxY() > binH) return false;
        	
        }
		
		
		for (int i=0; i< polys.size(); i++){
			NestPath p1 = polys.get(i);
            for (int j=0; j< polys.size(); j++){
                if(i!=j) 
                {                   	
                	NestPath p2 = polys.get(j);
                	if(GeometryUtil.intersect(p1, p2)) return false;                	
//					OPTIMAL SOLUTION BUT TOOO SLOW!
//                	double overlapArea = Fitness_Model.overlapDouble(p1.toPolygon2D(), p2.toPolygon2D());
//                	if(overlapArea<1) return false;                	
                }
                
            }
        }
        	
        	return true;		
	}

	/**
	 *	Try to repair the Phenotype(in this case by creating new random ones until one valid is created)
	 */
	@Override
	public Phenotype<DoubleGene, Double> repair(final Phenotype<DoubleGene, Double> pt, long generation) {
	
        Phenotype<DoubleGene, Double> res;
        int x=0;
		do {
		List<DoubleChromosome> arr = new ArrayList<DoubleChromosome>();
        for(int i=0; i<list.size()*nGenes; i+=nGenes)
        {
            arr.add(DoubleChromosome.of(-binW/2, binW/2));
            arr.add(DoubleChromosome.of(-binH/2,binH/2));
            //arr.add(DoubleChromosome.of(0,360));
        }
       
        final Genotype<DoubleGene> gt = Genotype.of(arr);
        
        	res=Phenotype.of(gt,generation);
        	x++;
        }while(!test(res));
    	//System.out.println("iterazioni sprecate:" + x);///TODO: evitare generazione di soluzioni non valide

		return res;
		
		
	}
	
//	static double[] repair (final double[] x)
//	{
//		
//		//repair
//		return x;
//		
//	}
	
	

}
