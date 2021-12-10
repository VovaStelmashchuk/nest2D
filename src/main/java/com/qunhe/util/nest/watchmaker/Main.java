package com.qunhe.util.nest.watchmaker;

import java.util.Random;

import org.uncommons.watchmaker.framework.EvolutionEngine;
import org.uncommons.watchmaker.framework.GenerationalEvolutionEngine;
import org.uncommons.watchmaker.framework.SelectionStrategy;
import org.uncommons.watchmaker.framework.selection.RankSelection;

import com.qunhe.util.nest.algorithm.Individual;

public class Main {

	public static void main(String[] args) {
		
		CandidateFactoryNest4j candidateFactory = new CandidateFactoryNest4j();
		
		// Test CandidateFactory
		//candidateFactory.generateInitialPopulation(10, new Random());
		//candidateFactory.generateRandomCandidate(new Random());
	
		
		IndividualMutation evolutionaryOperator = new IndividualMutation();

		evolutionaryOperator.apply(candidateFactory.generateInitialPopulation(10, new Random()), new Random());
				
		IndividualFitness fitnessEvaluator = new IndividualFitness();
		
		SelectionStrategy<? super Individual> selectionStrategy = new RankSelection();
		Random rng = new Random();
		
		EvolutionEngine<Individual> engine = new GenerationalEvolutionEngine<>(candidateFactory, evolutionaryOperator, fitnessEvaluator, selectionStrategy, rng);
		

	}

}
