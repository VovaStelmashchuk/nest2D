/*
 * JDXF Library
 *
 *   Copyright (C) 2018, Jonathan Sevy <jsevy@jsevy.com>
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in all
 *   copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *   SOFTWARE.
 *
 */

package com.nestapp.files.dxf.writter.parts;

import com.nestapp.files.dxf.common.RealPoint;


/**
 * @author jsevy
 */
public class DXFArc extends DXFEntity {
    protected RealPoint center;
    protected double radius;
    private final double startAngleRadians;
    private final double endAngleRadians;
    private final boolean isCounterclockwise;


    /**
     * Create a circular arc from the specified parameters.
     *
     * @param center             Center of the circle, as a RealPoint
     * @param radius             Radius of the circle
     * @param startAngleRadians  Starting angle of the circle, in radians, counterclockwise from the positive x axis
     * @param endAngleRadians    Starting angle of the circle, in radians, counterclockwise from the positive x axis
     * @param isCounterclockwise Indicate direction of arc, clockwise or counterclockwise
     */
    public DXFArc(RealPoint center, double radius, double startAngleRadians, double endAngleRadians, boolean isCounterclockwise) {
        this.startAngleRadians = startAngleRadians;
        this.endAngleRadians = endAngleRadians;
        this.isCounterclockwise = isCounterclockwise;
        this.center = new RealPoint(center);
        this.radius = radius;
    }

    /**
     * Implementation of DXFObject interface method; creates DXF text representing the circular arc.
     */
    public String toDXFString() {
        String result = "0\nARC\n";

        // print out handle and superclass marker(s) and data
        result += super.toDXFString();

        // print out subclass marker
        result += "100\nAcDbCircle\n";

        // center
        result += "10\n" + setPrecision(center.x) + "\n";
        result += "20\n" + setPrecision(center.y) + "\n";

        // radius
        result += "40\n" + setPrecision(radius) + "\n";

        // print out subclass marker
        result += "100\nAcDbArc\n";

        // angles - which are in degrees for circular arcs
        double startAngleDegrees = (startAngleRadians * 180 / Math.PI);
        double endAngleDegrees = (endAngleRadians * 180 / Math.PI);
        result += "50\n" + setPrecision(startAngleDegrees) + "\n";
        result += "51\n" + setPrecision(endAngleDegrees) + "\n";

        return result;
    }


    public String getDXFHatchInfo() {
        // circular arc
        String result = "72\n" + "2" + "\n";

        // center
        result += "10\n" + setPrecision(center.x) + "\n";
        result += "20\n" + setPrecision(center.y) + "\n";

        // radius
        result += "40\n" + setPrecision(radius) + "\n";

        // start/end angles - IN DEGREES FOR HATCH!
        double startAngleDegrees = (startAngleRadians * 180 / Math.PI);
        double endAngleDegrees = (endAngleRadians * 180 / Math.PI);

        // do some stuff to accommodate LibreCAD, which ignores counterclockwise flag and doesn't like negative angles or those
        // outside the range 0-360 - not sure how much to try to accommodate...
        // make 'em positive
        while (startAngleDegrees < 0)
            startAngleDegrees += 360;
        while (endAngleDegrees < 0)
            endAngleDegrees += 360;

        // stupid stuff for float/double rounding
        if (startAngleDegrees >= 360)
            startAngleDegrees -= 360;
        if (endAngleDegrees >= 360)
            endAngleDegrees -= 360;

        // LibreCAD draws counterclockwise if start < end, clockwise if end < start
        if (isCounterclockwise) {
            if (endAngleDegrees < startAngleDegrees)
                endAngleDegrees += 360;
        }

        if (!isCounterclockwise) {
            // need to reverse start and end angles
            double temp = startAngleDegrees;
            startAngleDegrees = endAngleDegrees;
            endAngleDegrees = temp;
        }

        result += "50\n" + setPrecision(startAngleDegrees) + "\n";
        result += "51\n" + setPrecision(endAngleDegrees) + "\n";

        // counterclockwise flag
        if (isCounterclockwise)
            result += "73\n" + "1" + "\n";
        else
            result += "73\n" + "0" + "\n";

        return result;
    }

}
