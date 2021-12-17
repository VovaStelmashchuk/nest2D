package com.qunhe.util.nest.watchmaker;

import java.util.List;

import org.uncommons.watchmaker.framework.FitnessEvaluator;
import org.uncommons.watchmaker.framework.termination.GenerationCount;

import com.qunhe.util.nest.algorithm.Individual;

public class IndividualFitness implements FitnessEvaluator<Individual> {
	private static final int NUMBER_GENERATIONS = 20;
	
	@Override
	public double getFitness(Individual arg0, List<? extends Individual> arg1) {
		// Target = numero di generazioni
		GenerationCount stop = new GenerationCount(NUMBER_GENERATIONS);
		
		// TODO sfrutto nest per selezionare gli individui -> metodo computUseRate per la fitness
		return 0;
	}

	@Override
	public boolean isNatural() {
		return true;
	}
}