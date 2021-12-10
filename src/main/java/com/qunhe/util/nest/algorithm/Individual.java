package com.qunhe.util.nest.algorithm;

import java.util.ArrayList;
import java.util.List;

import com.qunhe.util.nest.data.NestPath;

/**
 * @author yisa
 */
public class Individual  implements Comparable<Individual> {
    List<NestPath> placement;
    List<Integer> rotation;
    private double fitness;

  
    Individual(Individual individual) {
    	fitness = individual.fitness;
        placement = new ArrayList<>();
        rotation = new ArrayList<>();
        for(int i = 0 ; i<individual.placement.size() ; i++){
            NestPath cloneNestPath = new NestPath(individual.placement.get(i));
            placement.add(cloneNestPath);
        }
        for(int i = 0 ; i<individual.rotation.size() ; i++){
            int rotationAngle = individual.getRotation().get(i);
            rotation.add(rotationAngle);
        }
    }


    public Individual() {
    	fitness = -1;
        placement = new ArrayList<>();
        rotation = new ArrayList<>();
    }

    Individual(List<NestPath> placement, List<Integer> rotation) {
        setFitness(-1) ;
        this.placement = placement;
        this.rotation = rotation;
    }

    private int size(){
        return placement.size();
    }

    public List<NestPath> getPlacement() {
        return placement;
    }

    public void setPlacement(List<NestPath> placement) {
        this.placement = placement;
    }

    public List<Integer> getRotation() {
        return rotation;
    }

    public void setRotation(List<Integer> rotation) {
        this.rotation = rotation;
    }


    @Override
    public int compareTo(Individual o) {
        if(fitness > o.fitness){
            return 1;
        }
        else if(fitness == o.fitness){
            return 0 ;
        }
        return -1;
    }


    @Override
    public boolean equals(Object obj) {
        Individual individual = (Individual) obj;
        if(placement.size() != individual.size()){
            return false;
        }
        for(int i = 0; i <placement.size(); i ++){
            if(!placement.get(i).equals(individual.getPlacement().get(i))){
                return false;
            }
        }
        if(rotation.size() != individual.getRotation().size() ){
            return false;
        }
        for(int i = 0 ; i< rotation.size() ;i ++){
            if(rotation.get(i) != individual.getRotation().get(i)){
                return false;
            }
        }
        return true;
    }


    @Override
    public String toString() {
        String res = "";
        int count = 0 ;
        for (NestPath element : placement) {
            res += "NestPath "+ count +"\n";
            count++;
            res += element.toString() +"\n";
        }
        res+= "rotation \n";
        for(int r : rotation){
            res += r+" ";
        }
        res+="\n";

        return res;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }
}
