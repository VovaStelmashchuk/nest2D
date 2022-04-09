package com.qunhe.util.nest.jenetics;

import java.util.ArrayList;
import java.util.List;

import com.qunhe.util.nest.data.NestPath;

import io.jenetics.DoubleChromosome;
import io.jenetics.DoubleGene;
import io.jenetics.Genotype;
import io.jenetics.IntegerChromosome;
import io.jenetics.util.Factory;

public class Model_Factory {
	
	
	public static Factory<Genotype<DoubleGene>> of (List<NestPath> list, double binW, double binH)
    {
        List<DoubleChromosome> arr = new ArrayList<DoubleChromosome>();
        List<IntegerChromosome> arr2 = new ArrayList<IntegerChromosome>();
        int currPolygon = 0;
        for(int i=0; i<list.size()*3; i+=3)
        {
            arr.add(DoubleChromosome.of(-binW, binW));
            arr.add(DoubleChromosome.of(-binH,binH));
            arr.add(DoubleChromosome.of(0,360.0));
            currPolygon ++;
        }
        return Genotype.of(arr);
    }
		
	
	public static List<NestPath> convert(Genotype<DoubleGene> solution, List<NestPath> list)
    {
       
        for(int i=0; i < solution.length(); i+= 4)
        {
        	list.get(i).translate((double)solution.get(i).gene().allele(), (double)solution.get(i+1).gene().allele());
        	//list.get(i).setRotation(list.get(i).getPossibleRotations()[(int) Math.round(solution.get(i+2).gene().allele())]);
        	
        }
        return list;
    }
	

}
