package com.qunhe.util.nest.data;

import de.lighti.clipper.Paths;

/**
 * @author yisa
 */
//before Vector, renamed to set apart from Java.util.Vector
public class PathPlacement {
    public double x;
    public double y;
    public int id;
    public int rotation;
    
    public Paths nfp;

    public PathPlacement(double x, double y, int id, int rotation) {
        this.x = x;
        this.y = y;
        this.id = id;
        this.rotation = rotation;
        this.nfp = new Paths();
    }

    public PathPlacement(double x, double y, int id, int rotation, Paths nfp) {
        this.x = x;
        this.y = y;
        this.id = id;
        this.rotation = rotation;
        this.nfp = nfp;
    }

    public PathPlacement() {
        nfp = new Paths();
    }

    @Override
    public String toString() {
        return  "x = "+ x+" , y = "+y ;
    }
}
