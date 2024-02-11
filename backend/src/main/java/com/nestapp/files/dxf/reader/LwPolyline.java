package com.nestapp.files.dxf.reader;

import java.util.ArrayList;
import java.util.List;

public class LwPolyline extends Entity implements AutoPop {
    public List<LSegment> segments = new ArrayList<>();
    LSegment cSeg;
    private double xCp, yCp;
    private boolean hasXcp, hasYcp;
    private boolean close;

    public class LSegment {
        public double dx;
        public double dy;
        private double bulge;

        LSegment(double dx, double dy) {
            this.dx = dx;
            this.dy = dy;
        }
    }

    LwPolyline(String type) {
        super(type);
    }

    @Override
    void addParm(int gCode, String value) {
        switch (gCode) {
            case 10:                                      // Control Point X
                xCp = Double.parseDouble(value);
                hasXcp = true;
                break;
            case 20:                                      // Control Point Y
                yCp = Double.parseDouble(value);
                hasYcp = true;
                break;
            case 70:                                      // Flags
                int flags = Integer.parseInt(value);
                close = (flags & 0x01) != 0;
                break;
            case 42:                                      // Bulge factor  (positive = right, negative = left)
                cSeg.bulge = Double.parseDouble(value);
                break;
            case 90:                                      // Number of Vertices
                int vertices = Integer.parseInt(value);
                break;
        }
        if (hasXcp && hasYcp) {
            hasXcp = hasYcp = false;
            segments.add(cSeg = new LSegment(xCp, yCp));
        }
    }
}
