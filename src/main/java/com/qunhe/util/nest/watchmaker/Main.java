package com.qunhe.util.nest.watchmaker;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.uncommons.maths.random.Probability;
import org.uncommons.watchmaker.framework.EvolutionEngine;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;
import org.uncommons.watchmaker.framework.FitnessEvaluator;
import org.uncommons.watchmaker.framework.GenerationalEvolutionEngine;
import org.uncommons.watchmaker.framework.SelectionStrategy;
import org.uncommons.watchmaker.framework.selection.RankSelection;

import com.qunhe.util.nest.Nest;
import com.qunhe.util.nest.algorithm.Individual;
import com.qunhe.util.nest.config.Config;
import com.qunhe.util.nest.data.NestPath;
import com.qunhe.util.nest.data.Placement;
import com.qunhe.util.nest.data.Segment;
import com.qunhe.util.nest.util.GeometryUtil;
import com.qunhe.util.nest.util.SvgUtil;

public class Main {

	public static void main(String[] args) {
		
		CandidateFactoryNest4j candidateFactory = new CandidateFactoryNest4j();
		
		// Test CandidateFactory
		//candidateFactory.generateInitialPopulation(10, new Random());
		//candidateFactory.generateRandomCandidate(new Random());
	
		
		EvolutionaryOperator<Individual> evolutionaryOperator = new EvolutionaryOperator<Individual>() {

			@Override
			public List<Individual> apply(List<Individual> indivList, Random random) {
				int angolo = (int)(Math.random()*(73-0+1)+0);	// 360°/72 = 5°, rotazione di 5°
				int i=0;
				for(Individual elem : indivList) {
					GeometryUtil.rotatePolygon2Polygon(elem.getPlacement().get(i), angolo);
					i++;
				}
				return null;
			}
		};
		evolutionaryOperator.apply(candidateFactory.generateInitialPopulation(10, new Random()), new Random());
		
		
		FitnessEvaluator<? super Individual> fitnessEvaluator = new FitnessEvaluator<Individual>() {

			@Override
			public double getFitness(Individual arg0, List<? extends Individual> arg1) {
				// Number of generation
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public boolean isNatural() {
				// Whether fitness scores are natural or non-natural. 
				// If fitness is natural, the condition will be satisfied if any individual has a fitness that is greater than or equal to the target fitness. 
				// If fitness is non-natural, the condition will be satisified in any individual has a fitness that is less than or equal to the target fitness.
				// TODO Auto-generated method stub
				return false;
			}
		};
		
		SelectionStrategy<? super Individual> selectionStrategy = new RankSelection();
		Random rng = new Random();
		
		EvolutionEngine<Individual> engine = new GenerationalEvolutionEngine<>(candidateFactory, evolutionaryOperator, fitnessEvaluator, selectionStrategy, rng);
		

	}

}
