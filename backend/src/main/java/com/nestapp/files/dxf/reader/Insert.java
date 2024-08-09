package com.nestapp.files.dxf.reader;

public class Insert extends Entity implements AutoPop {
    private String blockHandle, blockName;
    private double ix, iy, xScale = 1.0, yScale = 1.0, zScale = 1.0, rotation;

    Insert(String type) {
        super(type);
    }

    @Override
    public void addParam(int gCode, String value) {
        switch (gCode) {
            case 2:                                     // Name of Block to insert
                blockName = value;
                break;
            case 5:                                     // Handle of Block to insert
                blockHandle = value;
                break;
            case 10:                                    // Insertion X
                ix = Double.parseDouble(value);
                break;
            case 20:                                    // Insertion Y
                iy = Double.parseDouble(value);
                break;
            case 41:                                    // X scaling
                xScale = Double.parseDouble(value);
                break;
            case 42:                                    // Y scaling
                yScale = Double.parseDouble(value);
                break;
            case 43:                                    // Z Scaling (affects x coord and rotation)
                zScale = Double.parseDouble(value);
                break;
            case 50:                                    // Rotation angle (degrees)
                rotation = Double.parseDouble(value);
                break;
        }
    }
}
