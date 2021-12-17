package com.qunhe.util.nest.watchmaker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.apache.batik.dom.GenericAttrNS;
import org.uncommons.watchmaker.framework.CandidateFactory;

import com.qunhe.util.nest.algorithm.Individual;
import com.qunhe.util.nest.data.NestPath;
import com.qunhe.util.nest.data.Segment;
import com.qunhe.util.nest.util.GeometryUtil;

class CandidateFactoryNest4j implements CandidateFactory<Individual> {

	private static final int MIN_NUM_CANDIDATE = 8;
	private static final int MAX_NUM_CANDIDATE = 20;
	private static final int MIN_COORD_BOUND = 0;
	private static final int MAX_COORD_BOUND = 1000;
	private static final int MIN_DISTANCE = 4;
	private static final int MAX_DISTANCE = 20;

	NestPath nestPath;
	
	public CandidateFactoryNest4j() {
		System.out.println("GENERATE INITIAL POPULATION\n");
		//generateInitialPopulation(10, new Random());	// populationsize is useless
	}

	
	@Override
	public List<Individual> generateInitialPopulation(int populationSize, Random rng) {
		// popolazione iniziale (Individual) di poligoni (NestPath)
		List<Individual> individiualsList = new ArrayList<Individual>();
		Individual individual = new Individual();
		NestPath np = new NestPath();
		int numPoly = rng.nextInt(casualNum(MIN_NUM_CANDIDATE, MAX_NUM_CANDIDATE)) + 8;
		
		for (int i = 0; i < numPoly; i++) {
			np = generatePoly(i + 1);
			individual.getPlacement().add(np);
		}
		
		System.out.println("Sono stati generati " + numPoly + " poligoni\n");
//		individual.showNestPaths();
		
		individiualsList.add(individual);
		return individiualsList;
	}

	
	@Override
	public List<Individual> generateInitialPopulation(int populationSize, Collection<Individual> seedCandidates, Random rng) {
		// TODO Auto-generated method stub
		return null;
	}

	
	@Override
	public Individual generateRandomCandidate(Random rng) {
		// TODO Auto-generated method stub
		return null;
	}

	
	/**
	 * Random polygon generation
	 */
	private NestPath generatePoly(int id) {
		NestPath poly = new NestPath();
		
		// generazione numero di poligoni (NestPath) nello stesso indivuduo (Individual)
		Random r = new Random();
		int switchRng = r.nextInt(casualNum(1, 5));	// range 0-4
		int angolo = (int) (Math.random() * (73 - 0 + 1) + 0);	// 360°/72 = 5°, rotazione minima di 5°, range 0-72 (se 0 -> non viene ruotato)
		
		switch (switchRng) {
		case 0:		// triangolo
//			System.out.println("TRIANGOLO");
			// 3 coordinate random
			for(int i=0 ; i<3 ; i++) {
				poly.add(new Segment(casualNum(MIN_COORD_BOUND, MAX_COORD_BOUND), casualNum(MIN_COORD_BOUND, MAX_COORD_BOUND)));	
			}
			break;
		case 1:		// rettangolo
//			System.out.println("RETTANGOLO");
			int p1 = casualNum(MIN_COORD_BOUND, MAX_COORD_BOUND);
			int p2 = casualNum(MIN_COORD_BOUND, MAX_COORD_BOUND);
			int p3 = casualNum(MIN_DISTANCE, MAX_DISTANCE);
			int p4 = casualNum(MIN_DISTANCE, MAX_DISTANCE);
			// base
			poly.add(new Segment(p1, p2));
			poly.add(new Segment(p1+p3, p2));
			// altezza
			poly.add(new Segment(p1, p2+p4));
			poly.add(new Segment(p1+p3, p2+p4));
			break;
		default:	// poligono random
//			System.out.println("POLIGONO DI "+ (switchRng+2) + " LATI");
			for(int i=0 ; i<switchRng+2 ; i++) {
				poly.add(new Segment(casualNum(MIN_COORD_BOUND, MAX_COORD_BOUND), casualNum(MIN_COORD_BOUND, MAX_COORD_BOUND)));	
			}
			break;
		}
		
		poly.setId(id);
		poly.setRotation(angolo);
		GeometryUtil.rotatePolygon2Polygon(poly, angolo);	// rotazione
		
//		System.out.println(poly.toString());
		return poly;
	}

	/**
	 * Random number within bounds
	 * @param min minimo valore del range (compreso)
	 * @param max massimo valore del range (escluso)
	 */
	private int casualNum(int min, int max) {
		return (int) (Math.random() * (max - min + 1) + min);
	}
	
	public NestPath getRandomNestPath(int id) {
		return generatePoly(id);
	}
	
}







