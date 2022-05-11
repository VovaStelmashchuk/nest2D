package com.qunhe.util.nest.jenetics_with_NFP;

import java.util.concurrent.locks.ReentrantLock;

import com.qunhe.util.nest.data.NestPath;

import io.jenetics.EnumGene;
import io.jenetics.Gene;
import io.jenetics.Phenotype;
import io.jenetics.engine.EvolutionResult;

public class Updater<G extends Gene<?,G>> {
	
	Phenotype<G, Double> tmpBest=null;
	ReentrantLock tmpBestLock;
	
	
	public Updater()
	{
		tmpBestLock = new ReentrantLock(true);
	}
	
	/**
	 * Function to be executed at every generation
	 * If the result is the best up to now show a message
	 * @param result result of current evaluation
	 */
	void update(final EvolutionResult<G, Double> result)
	{
		System.out.println(result.generation() + " generation: ");
		tmpBestLock.lock();
		if(tmpBest == null || tmpBest.compareTo(result.bestPhenotype())>0)
		{			
			tmpBest =result.bestPhenotype();			
			System.out.println("Found better fitness: " + tmpBest.fitness());
		}
		else
		{
			System.out.println("Better fitness is still: " + tmpBest.fitness());
		}
		tmpBestLock.unlock();
	}
	
	

}
