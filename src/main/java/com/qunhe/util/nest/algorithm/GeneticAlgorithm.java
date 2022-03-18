package com.qunhe.util.nest.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.qunhe.util.nest.config.Config;
import com.qunhe.util.nest.data.Bound;
import com.qunhe.util.nest.data.NestPath;
import com.qunhe.util.nest.util.GeometryUtil;


/**
 * @author yisa
 */
public class GeneticAlgorithm {

    public List<NestPath> adam;	// Individual
    public NestPath bin;
    public Bound binBounds;
    public List<Integer> angles ;
    public List<Individual> population;
    Config config;

    public GeneticAlgorithm(List<NestPath> adam, NestPath bin, Config config) {
        this.adam = adam;
        this.bin = bin;
        this.config = config;
        this.binBounds = GeometryUtil.getPolygonBounds(bin);	// estremi della superficie (binPath) su cui si dispongono i poligoni
        population = new ArrayList<>();
        init();
    }

    
    private void init(){
        angles = new ArrayList<>();
        for(int i = 0 ; i< adam.size(); i ++) {
            int angle = randomAngle(adam.get(i));	// assegnazione di un angolo di rotazione casuale
            angles.add(angle);						// nel mio caso verr� quindi aggiunto solo un angolo
        }
        population.add(new Individual(adam , angles));
        while(population.size() < config.POPULATION_SIZE) {		// config.POPULATION_SIZE = 10 by default
            Individual mutant = mutate(population.get(0));
            population.add(mutant);
        }
    }

    private Individual mutate(Individual individual) {
        Individual clone = new Individual(individual);			// crea un clone (che verr� aggiunto a population) che muta e ruota gli individui originali
        for(int i = 0 ; i< clone.placement.size() ; i ++){		// itera tutti i NestPath contenuti nell'individuo "clone"
            double random = Math.random();
            if( random < 0.01 * config.MUTATION_RATE ){			// config.MUTATION_RATE = 10 by default
                int j = i + 1;
                if( j < clone.placement.size() ){
                    Collections.swap(clone.getPlacement(), i, j);	// Lo swap funge come ListOrderMutation di WatchMaker.framework.operators
                }
            }
            random = Math.random();
            if(random < 0.01 * config.MUTATION_RATE ){
                clone.getRotation().set(i, randomAngle(clone.placement.get(i)));
            }
        }
        checkAndUpdate(clone);
        return clone;
    }
    
    public void checkAndUpdate(Individual individual){
        for(int i = 0; i<individual.placement.size(); i++){
            int angle = individual.getRotation().get(i);
            NestPath nestPath = individual.getPlacement().get(i);
            Bound rotateBound = GeometryUtil.rotatePolygon(nestPath,angle);		// assegna nuovi Bound ad ogni NestPath, di conseguenza lo ruota
            if(rotateBound.width < binBounds.width && rotateBound.height < binBounds.height){	// se larghezza e altezza del poligono ruotato sono minori della posizione originale
                continue;
            }
            else{
                int safeAngle = randomAngle(nestPath);
                individual.getRotation().set(i , safeAngle);
            }
        }
    }

    // metodo per la generazione di figli
    public void generation(){
        List<Individual> newpopulation = new ArrayList<>();
        Collections.sort(population);

        newpopulation.add(population.get(0));
        while(newpopulation.size() < config.POPULATION_SIZE) {
            Individual male = randomWeightedIndividual(null);
            Individual female = randomWeightedIndividual(male);
            List<Individual> children = mate(male,female);
            newpopulation.add(mutate(children.get(0)));
            if(newpopulation.size() < population.size() ){
                newpopulation.add(mutate(children.get(1)));
            }
        }
        population = newpopulation;
    }

    private List<Individual> mate(Individual male , Individual female){
        List<Individual> children = new ArrayList<>();

        long cutpoint = Math.round(Math.min(Math.max(Math.random(), 0.1), 0.9)*(male.placement.size()-1));	// range circa 1-21

        List<NestPath> gene1 = new ArrayList<>();
        List<Integer> rot1 = new ArrayList<>();
        List<NestPath> gene2 = new ArrayList<>();
        List<Integer> rot2 = new ArrayList<>();

        for(int i = 0; i <cutpoint;i ++){
            gene1.add(new NestPath(male.placement.get(i)));
            rot1.add(male.getRotation().get(i));
            gene2.add(new NestPath(female.placement.get(i)));
            rot2.add(female.getRotation().get(i));
        }

        for(int i = 0 ; i<female.placement.size() ;i ++){
            if(!contains(gene1,female.placement.get(i).getId())){
                gene1.add(female.placement.get(i));
                rot1.add(female.rotation.get(i));
            }
        }

        for(int  i= 0 ; i<male.placement.size() ; i ++){
            if(! contains(gene2 , male.placement.get(i).getId())){
                gene2.add(male.placement.get(i));
                rot2.add(male.rotation.get(i));
            }
        }
        Individual individual1 = new Individual(gene1,rot1);
        Individual individual2 = new Individual(gene2,rot2);

        checkAndUpdate(individual1);checkAndUpdate(individual2);


        children.add(individual1); children.add(individual2);
        return children;
    }


    private boolean contains(List<NestPath> gene , int id ){
        for (NestPath element : gene) {
            if(element.getId() == id ){
                return true;
            }
        }
        return false;
    }

    private Individual randomWeightedIndividual(Individual exclude) {
    	// generazione di pop (clone di population)
        List<Individual> pop = new ArrayList<>();
        for(int i = 0 ; i < population.size(); i ++){
            Individual individual = population.get(i);
            Individual clone = new Individual(individual);
            pop.add(clone);
        }
        
        if(exclude != null){
            int index = pop.indexOf(exclude);
            if(index >= 0) {
                pop.remove(index);
            }
        }
        double rand = Math.random();
        double lower = 0;
        double weight = 1/pop.size();
        double upper = weight;

        for(int i=0 ; i<pop.size() ; i++) {
            if(rand > lower && rand < upper) {
                return pop.get(i);
            }
            lower = upper;
            upper += 2 * weight * ( (pop.size() - i ) / pop.size());
        }
        return pop.get(0);
    }


    /**
     * Return an angle for a polygon
     * @param part
     * @return
     */
    private int randomAngleOld(NestPath part){
        List<Integer> angleList = new ArrayList<>();
        int rotate = Math.max(1,part.getRotation());
        if(rotate == 0 ){
            angleList.add(0);
        }
        else{
            for(int i = 0 ; i< rotate; i ++){
                angleList.add((360/rotate) * i );
            }
        }
        Collections.shuffle(angleList);
        for (Integer element : angleList) {
            Bound rotatedPart = GeometryUtil.rotatePolygon(part , element);
            if(rotatedPart.getWidth() < binBounds.getWidth() && rotatedPart.getHeight() < binBounds.getHeight() ){
                return element;
            }
        }
        /**
         * æ²¡æœ‰æ‰¾åˆ°å�ˆæ³•çš„è§’åº¦
         */
        return -1;
    }

    private  int randomAngle(NestPath part){
        int[]poss = part.getPossibleRotations();
        if(poss==null || poss.length<2){
            return part.getRotation();
        }
        return poss[(int)(Math.random()*poss.length)];
    }


        public List<NestPath> getAdam() {
        return adam;
    }

    public void setAdam(List<NestPath> adam) {
        this.adam = adam;
    }

    public NestPath getBin() {
        return bin;
    }

    public void setBin(NestPath bin) {
        this.bin = bin;
    }
 
}
