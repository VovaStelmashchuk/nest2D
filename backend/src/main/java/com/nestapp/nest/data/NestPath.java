package com.nestapp.nest.data;

import com.nestapp.nest.config.Config;
import org.apache.batik.ext.awt.geom.Polygon2D;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yisa
 */

public class NestPath implements Comparable<NestPath> {
    private final List<Segment> segments;
    public double offsetX;
    public double offsetY;
    private int id;
    private int rotation;    // angolo rotazione
    public Config config;
    public double area;

    // assgnied incrementally or cloned
    private int bid;

    static private int bid_counter = 1;

    public NestPath() {
        this(new Config());
    }

    public NestPath(Config config) {
        offsetX = 0;
        offsetY = 0;
        segments = new ArrayList<>();
        area = 0;
        this.config = config;
        //
        bid = bid_counter++;
    }

    public NestPath(NestPath srcNestPath) {
        segments = new ArrayList<>();
        for (Segment segment : srcNestPath.getSegments()) {
            segments.add(new Segment(segment));
        }

        this.id = srcNestPath.id;
        this.rotation = srcNestPath.rotation;
        this.offsetX = srcNestPath.offsetX;
        this.offsetY = srcNestPath.offsetY;
        this.bid = srcNestPath.bid;
        this.area = srcNestPath.area;
    }


    public void add(double x, double y) {
        this.add(new Segment(x, y));
    }

    @Override
    public boolean equals(Object obj) {
        NestPath nestPath = (NestPath) obj;
        if (segments.size() != nestPath.size()) {
            return false;
        }
        for (int i = 0; i < segments.size(); i++) {
            if (!segments.get(i).equals(nestPath.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Discard the last segment
     */
    public void pop() {
        segments.remove(segments.size() - 1);
    }

    public void reverse() {
        List<Segment> rever = new ArrayList<>();
        for (int i = segments.size() - 1; i >= 0; i--) {
            rever.add(segments.get(i));
        }
        segments.clear();
        segments.addAll(rever);
    }

    public Segment get(int i) {
        return segments.get(i);
    }

    @Override
    public String toString() {
        String res = "";
        res += "id = " + id + " , rotation = " + rotation + "\n";
        int count = 0;
        for (Segment s : segments) {
            res += "Segment " + count + "\n";
            count++;
            res += s.toString() + "\n";
        }
        return res;
    }

    public int getRotation() {
        return rotation;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public void clear() {
        segments.clear();
    }

    public int size() {
        return segments.size();
    }

    public void add(Segment s) {
        segments.add(s);
    }

    public List<Segment> getSegments() {
        return segments;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(double offsetX) {
        this.offsetX = offsetX;
    }

    public double getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(double offsetY) {
        this.offsetY = offsetY;
    }

    @Override
    public int compareTo(NestPath o) {
        double area0 = this.area;
        double area1 = o.area;
        if (area0 > area1) {
            return 1;
        } else if (area0 == area1) {
            return 0;
        }
        return -1;
    }

    public int getBid() {
        return bid;
    }

    public void setBid(int bid) {
        this.bid = bid;
    }

}
