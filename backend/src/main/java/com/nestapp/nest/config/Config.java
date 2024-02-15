package com.nestapp.nest.config;

public class Config {
    public static int CLIIPER_SCALE = 10000;
    public static double CURVE_TOLERANCE = 0.02;
    public double SPACING;
    public static double BOUND_SPACING;

    public Config() {
        CLIIPER_SCALE = 10000;
        CURVE_TOLERANCE = 0.3;
        SPACING = 5;
    }
}
