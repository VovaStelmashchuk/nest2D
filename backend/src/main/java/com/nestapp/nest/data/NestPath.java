package com.nestapp.nest.data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class NestPath implements Comparable<NestPath> {
    private final List<Segment> segments;
    public double offsetX;
    public double offsetY;
    private int rotation;
    public double area;
    private String bid;
    static private final AtomicInteger bid_counter = new AtomicInteger(1);

    public NestPath() {
        this("");
    }

    public NestPath(String id) {
        offsetX = 0;
        offsetY = 0;
        segments = new ArrayList<>();
        area = 0;
        if (id == null || id.isEmpty()) {
            bid = String.valueOf(bid_counter.incrementAndGet());
        } else {
            bid = id;
        }
    }

    public NestPath(NestPath srcNestPath) {
        segments = new ArrayList<>();
        for (Segment segment : srcNestPath.getSegments()) {
            segments.add(new Segment(segment));
        }

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
        res += "bid = " + bid + " , rotation = " + rotation + "\n";
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

    public String getBid() {
        return bid;
    }

    public void setBid(String bid) {
        this.bid = bid;
    }

}
