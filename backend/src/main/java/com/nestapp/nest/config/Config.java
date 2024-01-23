package com.nestapp.nest.config;

/**
 * @author yisa
 */
public class Config {
    public static int CLIIPER_SCALE = 10000;
    public static double CURVE_TOLERANCE = 0.02;
    public static double BIN_WIDTH;
    public static double BIN_HEIGHT;
    public static int NB_ITERATIONS;
    public double SPACING;
    public int POPULATION_SIZE;
    public int MUTATION_RATE;
    public boolean CONCAVE;
    public boolean USE_HOLE;
    // Contest purpose
    public static boolean IS_DEBUG;
    public static double BOUND_SPACING;
    public static boolean ASSUME_NO_INNER_PARTS = false;
    public static boolean ASSUME_ALL_PARTS_PLACABLE;
    public static int LIMIT = 0;
    public static String OUTPUT_DIR = "output/";
    public static String NFP_CACHE_PATH;

    /**
     * @author Alberto Gambarara
     */
    public int MAX_SEC_DURATION;
    public int MAX_STEADY_FITNESS;
    public int NUMBER_OF_ROTATIONS;
    public String OUTPUT_FILENAME;
    public int N_THREAD;


    public Config() {
        CLIIPER_SCALE = 10000;
        CURVE_TOLERANCE = 0.3;
        SPACING = 5;
        POPULATION_SIZE = 10;
        MUTATION_RATE = 10;
        CONCAVE = false;
        USE_HOLE = false;

        /**
         * @author Alberto Gambarara
         */
        OUTPUT_FILENAME = "res.html";
        NUMBER_OF_ROTATIONS = 4;
        MAX_SEC_DURATION = 60;
        MAX_STEADY_FITNESS = 20;
        N_THREAD = 1;


    }

    public boolean isCONCAVE() {
        return CONCAVE;
    }

    public boolean isUSE_HOLE() {
        return USE_HOLE;
    }
}
