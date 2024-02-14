package com.nestapp.nest.config;

/**
 * @author yisa
 */
public class Config {
    public static int CLIIPER_SCALE = 10000;
    public static double CURVE_TOLERANCE = 0.02;
    public static double BIN_WIDTH;
    public static double BIN_HEIGHT;
    public double SPACING;
    public int POPULATION_SIZE;
    public int MUTATION_RATE;
    public boolean USE_HOLE;
    public static boolean IS_DEBUG;
    public static double BOUND_SPACING;
    public int MAX_SEC_DURATION;
    public int MAX_STEADY_FITNESS;
    public int NUMBER_OF_ROTATIONS;
    public int N_THREAD;


    public Config() {
        CLIIPER_SCALE = 10000;
        CURVE_TOLERANCE = 0.3;
        SPACING = 5;
        POPULATION_SIZE = 10;
        MUTATION_RATE = 10;
        USE_HOLE = false;
        NUMBER_OF_ROTATIONS = 4;
        MAX_SEC_DURATION = 60;
        MAX_STEADY_FITNESS = 20;
        N_THREAD = 1;
    }
}
