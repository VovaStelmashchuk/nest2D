package com.qunhe.util.nest.watchmaker;

import java.util.List;

import org.uncommons.watchmaker.framework.FitnessEvaluator;

import com.qunhe.util.nest.algorithm.Individual;

public class IndividualFitness implements FitnessEvaluator<Individual> {

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
}