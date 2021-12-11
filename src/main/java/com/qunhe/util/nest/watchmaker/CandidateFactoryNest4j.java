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

class CandidateFactoryNest4j implements CandidateFactory<Individual> {

	NestPath nestPath;
	CandidateFactoryNest4j(){
		System.out.println("GENERATE INITIAL POPULATION");
		// crea un nest path		
		nestPath = generatePoly();		
	}
	
	
	@Override
	public List<Individual> generateInitialPopulation(int populationSize, Random rng) {
		List<Individual> individiualsList = new ArrayList<Individual>();
		// Verranno creati più individui randomicamente
		for(int i=0 ; i<populationSize ; i++) {
			// NestPaths random generation
			Individual individual = new Individual();
			for(int z=0 ; z<rng.nextInt(casualNum(5, 15)) ; z++) {
				// crea una variante del nestPath un po' differente ad esempio ruotando
				// TODO
				individual.getPlacement().add(nestPath);
				individiualsList.add(individual);
			}
		}
		return individiualsList;
	}

	@Override
	public List<Individual> generateInitialPopulation(int populationSize, Collection<Individual> seedCandidates, Random rng) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Individual generateRandomCandidate(Random rng) {
		
		Individual individual = new Individual();
		
		// ogni individuo è composto da piu NestPath (poligoni), quindi creo randomicamente una serie di poligoni (tra i 5 e i 15)
		for(int i=0 ; i<rng.nextInt(casualNum(5, 15)) ; i++) {
			System.out.println("GENERATE RANDOM CANDIDATE");
			// TODO ruota o latrre variazioni
			individual.getPlacement().add(nestPath);
		}
		System.out.println(individual.toString());

		return individual;
	}
	
	/**
	 *Random number within bounds
	 */
	private int casualNum(int min, int max) {
		return (int)(Math.random()*(max-min+1)+min);
	}
	
	/**
	 *Random polygon generation 
	 */
	private NestPath generatePoly() {
		
		NestPath poly = new NestPath();
		
		// coordinate bounds
		int minCoord = 0;
		int maxCoord = 800;
		
		// generazione numero di poligoni 
		Random r = new Random();
		int switchRng = r.nextInt(casualNum(1, 3));	// esce anche 0
		
		switch (switchRng) {
		case 0:		// triangolo
			System.out.println("TRIANGOLO");
			// 3 coordinate random
			for(int i=0 ; i<3 ; i++) {
				poly.add(new Segment(casualNum(minCoord, maxCoord), casualNum(minCoord, maxCoord)));	
			}
			break;
		case 1:		// rettangolo orizzontale, verrà poi eventualmente ruotato
			System.out.println("RETTANGOLO");
			int p1 = casualNum(minCoord, maxCoord);
			int p2 = casualNum(minCoord, maxCoord);
			int p3 = casualNum(4, 20);
			int p4 = casualNum(4, 20);
			// base
			poly.add(new Segment(p1, p2));
			poly.add(new Segment(p1+p3, p2));
			// altezza
			poly.add(new Segment(p1, p2+p4));
			poly.add(new Segment(p1+p3, p2+p4));
			break;
		default:	// polygono random
			System.out.println("POLIGONO DI "+ switchRng + " LATI \n");
			for(int i=0 ; i<switchRng ; i++) {
				poly.add(new Segment(casualNum(minCoord, maxCoord), casualNum(minCoord, maxCoord)));	
			}
			break;
		}
		poly.toString();
		return poly;
	}

	
}







