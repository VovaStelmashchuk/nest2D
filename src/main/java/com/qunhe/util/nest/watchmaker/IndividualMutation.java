package com.qunhe.util.nest.watchmaker;

import java.util.List;
import java.util.Random;

import org.uncommons.watchmaker.framework.EvolutionaryOperator;
import com.qunhe.util.nest.algorithm.Individual;
import com.qunhe.util.nest.util.GeometryUtil;

// fa ruotazioni a caso
public class IndividualMutation implements EvolutionaryOperator<Individual>{

	// Mutation = un individuo alla volta
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
	
}
