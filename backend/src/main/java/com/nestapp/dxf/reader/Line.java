package com.nestapp.dxf.reader;

public class Line extends Entity implements AutoPop {
    public double xStart, yStart, xEnd, yEnd;

    Line(String type) {
        super(type);
    }

    @Override
    void addParm(int gCode, String value) {
        switch (gCode) {
            case 10:                              // Line Point X1
                xStart = Double.parseDouble(value);
                break;
            case 20:                              // Line Point Y2
                yStart = Double.parseDouble(value);
                break;
            case 11:                              // Line Point X2
                xEnd = Double.parseDouble(value);
                break;
            case 21:                              // Line Point Y2
                yEnd = Double.parseDouble(value);
                break;
        }
    }

    @Override
    public String toString() {
        return "Line{" +
            "xStart=" + xStart +
            ", yStart=" + yStart +
            ", xEnd=" + xEnd +
            ", yEnd=" + yEnd +
            '}';
    }
}
