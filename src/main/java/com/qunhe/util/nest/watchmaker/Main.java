package com.qunhe.util.nest.watchmaker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.uncommons.watchmaker.framework.EvolutionEngine;
import org.uncommons.watchmaker.framework.EvolutionObserver;
import org.uncommons.watchmaker.framework.GenerationalEvolutionEngine;
import org.uncommons.watchmaker.framework.PopulationData;
import org.uncommons.watchmaker.framework.SelectionStrategy;
import org.uncommons.watchmaker.framework.selection.RankSelection;
import org.uncommons.watchmaker.framework.termination.GenerationCount;

import org.uncommons.watchmaker.*;

import com.qunhe.util.nest.Nest;
import com.qunhe.util.nest.algorithm.Individual;
import com.qunhe.util.nest.config.Config;
import com.qunhe.util.nest.data.NestPath;
import com.qunhe.util.nest.data.Result;
import com.qunhe.util.nest.data.Segment;
import com.qunhe.util.nest.util.Placementworker;

public class Main {

	public static void main(String[] args) {
		//
		CandidateFactoryNest4j candidateFactory = new CandidateFactoryNest4j();
		IndividualMutation mutation = new IndividualMutation();
		
		// TEST CandidateFactory & IndividualMutation
		List<Individual> mutatedIndiv = mutation.apply(candidateFactory.generateInitialPopulation(10, new Random()), new Random());
//		testCandFact(mutatedIndiv);

		
		IndividualFitness fitnessEvaluator = new IndividualFitness();	
		/* TODO correggere, fitness sempre = 0 -> 
		 * 
		 * in output da sempre "GENERATE INITIAL POPULATION", mi sa che devo correggere qualcosa in CandidateFactory
		 * */
		
		// TEST IndividualFitness
		testFitness(mutatedIndiv, fitnessEvaluator);
		// Per avere un rate in % della fitness posso sfruttare Nest.computUseRate
		
		SelectionStrategy<? super Individual> selectionStrategy = new RankSelection();
		
		

		// TARGET
//		private static final int NUMBER_GENERATIONS = 20;
//		// Target = numero di generazioni
//		GenerationCount stop = new GenerationCount(NUMBER_GENERATIONS);	//->> NON SERVEEEE, è SOLO LA FITNESS CHE DEVO FARE
		
		
		EvolutionEngine<Individual> engine = new GenerationalEvolutionEngine<>(candidateFactory, mutation, fitnessEvaluator, selectionStrategy, new Random());
		
//		engine.addEvolutionObserver(new EvolutionObserver() {
//			@Override
//			public void populationUpdate(PopulationData data) {
//				// qui potresti mandare 
//				System.out.println("**** " + data.getBestCandidateFitness());
//			}			
//		});
//		
//		engine.addEvolutionObserver(new org.uncommons.watchmaker.swing.evolutionmonitor.EvolutionMonitor<Individual>());
		engine.addEvolutionObserver(new EvolutionObserver() {
			@Override
			public void populationUpdate(PopulationData data) {
				// qui potresi mandare 
				System.out.println("**** " + data.getBestCandidateFitness());
			}			
		});
		
		engine.addEvolutionObserver(new org.uncommons.watchmaker.swing.evolutionmonitor.EvolutionMonitor<Individual>());



		//engine.evolve(10, 1, new GenerationCount(10));
			
	}
	
	
	private static void testCandFact(List<Individual> list) {
		System.out.println("****************************");
		System.out.println("MUTATION:");
		System.out.println("**************************** \n");
		int i = 0;
		
		for(Individual elem: list) {
			System.out.println("----------------------------");
			System.out.println("INDIVIDUO: " + (i++));
			System.out.println("---------------------------- \n");
			elem.showNestPaths();
		}
	}
	
	
	private static void testFitness(List<Individual> individui, IndividualFitness judger) {
		System.out.println("****************************");
		System.out.println("TEST FITNESSSSSSS");
		System.out.println("****************************");
		
		for(Individual elem: individui) {
			judger.getFitness(elem, individui);
		}

		//engine.evolve(10, 1, new GenerationCount(10));

	}

}





















