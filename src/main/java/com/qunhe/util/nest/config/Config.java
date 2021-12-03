package com.qunhe.util.nest.config;

/**
 * @author yisa
 */
public class Config {
    public  static int CLIIPER_SCALE = 10000;
    public  static double CURVE_TOLERANCE  = 0.02;
    public static double BIN_WIDTH;
    public static double BIN_HEIGHT;
    public static int NB_ITERATIONS;
    public  double SPACING ;
    public  int POPULATION_SIZE;
    public  int MUTATION_RATE ;
    public  boolean CONCAVE ;
    public   boolean USE_HOLE ;
    // Contest purpose
    public static boolean IS_DEBUG;
    public static double BOUND_SPACING;
    public static boolean ASSUME_NO_INNER_PARTS;
    public static boolean ASSUME_ALL_PARTS_PLACABLE;
    public static int LIMIT=0;
    public static String OUTPUT_DIR ="output/";
    public static String NFP_CACHE_PATH;


    public Config() {
        CLIIPER_SCALE = 10000;
        CURVE_TOLERANCE = 0.3;
        SPACING = 5;
        POPULATION_SIZE = 10;
        MUTATION_RATE = 10;
        CONCAVE = false;
        USE_HOLE = false;
    }

    public boolean isCONCAVE() {
        return CONCAVE;
    }

    public boolean isUSE_HOLE() {
        return USE_HOLE;
    }
}
