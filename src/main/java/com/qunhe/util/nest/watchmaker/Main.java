package com.qunhe.util.nest.watchmaker;

import java.util.List;
import java.util.Random;

import org.uncommons.watchmaker.framework.EvolutionEngine;
import org.uncommons.watchmaker.framework.GenerationalEvolutionEngine;
import org.uncommons.watchmaker.framework.SelectionStrategy;
import org.uncommons.watchmaker.framework.selection.RankSelection;

import com.qunhe.util.nest.algorithm.Individual;

public class Main {

	public static void main(String[] args) {
		
		CandidateFactoryNest4j candidateFactory = new CandidateFactoryNest4j();
		IndividualMutation mutation = new IndividualMutation();
		
		
		// Test Candidate Factory e Individual mutation
		List<Individual> mutatedIndiv = mutation.apply(candidateFactory.generateInitialPopulation(10, new Random()), new Random());
//		System.out.println("****************************");
//		System.out.println("MUTATION:");
//		System.out.println("**************************** \n");
//		int i = 0;
//		for(Individual elem: mutatedIndiv) {
//			System.out.println("----------------------------");
//			System.out.println("INDIVIDUO: " + (i++));
//			System.out.println("---------------------------- \n");
//			elem.showNestPaths();
//		}
	
		
		IndividualFitness fitnessEvaluator = new IndividualFitness();
		
		SelectionStrategy<? super Individual> selectionStrategy = new RankSelection();
		Random rng = new Random();
		
		EvolutionEngine<Individual> engine = new GenerationalEvolutionEngine<>(candidateFactory, mutation, fitnessEvaluator, selectionStrategy, rng);
		

	}

}
