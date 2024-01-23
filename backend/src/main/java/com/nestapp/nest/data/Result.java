package com.nestapp.nest.data;

import java.util.List;

/**
 * @author yisa
 */
public class Result implements Comparable<Result>{
    public List<List<PathPlacement>> placements;
    public double fitness;
    
    public List<NestPath> paths;
    public double area;

    public Result(List<List<PathPlacement>> placements, double fitness, List<NestPath> paths, double area) {
        this.placements = placements;
        this.fitness = fitness;
        this.paths = paths;
        this.area = area;
    }

	@Override
	public int compareTo(Result o) {
		return Double.valueOf(fitness).compareTo(o.fitness);
	}
}
