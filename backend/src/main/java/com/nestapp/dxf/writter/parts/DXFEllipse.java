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

package com.nestapp.dxf.writter.parts;

import com.nestapp.dxf.RealPoint;

import java.awt.*;



/**
 * Class representing an elliptical arc.
 *
 * @author jsevy
 *
 */
public class DXFEllipse extends DXFEntity
{
    private RealPoint center;
    private RealPoint majorAxisEndpoint;    // relative to center
    private double axisRatio;                // minor to major
    private double startParameter;	// parameter t corresponding to the start point of the arc represented as (a*cos(t), b*sin(t))
    private double endParameter;		// parameter t corresponding to the end point of the arc represented as (a*cos(t), b*sin(t))
    private boolean isCounterclockwise; // needed for hatch boundary spec
    private Color color;
    private BasicStroke stroke;


    /**
     * Create an elliptical arc corresponding to the specified parameters. Note that this is the most general case
     * of an ellipse, since the major and minor axes need not be parallel to the x or y axes.
     *
     * @param center			The center of the ellipse
     * @param majorAxisEndpoint	The endpoint of the major axis, relative to the center of the ellipse
     * @param axisRatio			The ratio between the length of the minor axis and the length of the major axis
     * @param startParameter	Parameter t corresponding to the start point of the arc represented as (a*cos(t), b*sin(t)),
     * 							rotated so that t = 0 corresponds to the major axis endpoint.
     * @param endParameter	    Parameter t corresponding to the end point of the arc represented as (a*cos(t), b*sin(t)),
     *                          rotated so that t = 0 corresponds to the major axis endpoint.
     * @param isCounterclockwise    Indicate direction of arc, clockwise or counterclockwise
     * @param graphics          The graphics object specifying parameters for the arc (color, thickness)
     */
    public DXFEllipse(RealPoint center, RealPoint majorAxisEndpoint, double axisRatio, double startParameter, double endParameter, boolean isCounterclockwise, Graphics2D graphics)
    {
        this.center = new RealPoint(center);
        this.majorAxisEndpoint = new RealPoint(majorAxisEndpoint);
        this.axisRatio = axisRatio;
        this.startParameter = startParameter;
        this.endParameter = endParameter;
        this.isCounterclockwise = isCounterclockwise;
        this.color = graphics.getColor();
        this.stroke = (BasicStroke)graphics.getStroke();
    }



    /**
     * Implementation of DXFObject interface method; creates DXF text representing the elliptical arc.
     */
    public String toDXFString()
    {
        String result = "0\nELLIPSE\n";

        // print out handle and superclass marker(s)
        result += super.toDXFString();

        // print out subclass marker
        result += "100\nAcDbEllipse\n";

        // center
        result += "10\n" + setPrecision(center.x) + "\n";
        result += "20\n" + setPrecision(center.y) + "\n";

        // major axis endpoint relative to center
        result += "11\n" + setPrecision(majorAxisEndpoint.x) + "\n";
        result += "21\n" + setPrecision(majorAxisEndpoint.y) + "\n";

        // radius ratio
        result += "40\n" + setPrecision(axisRatio) + "\n";

        // start/end angles
        result += "41\n" + setPrecision(startParameter) + "\n";
        result += "42\n" + setPrecision(endParameter) + "\n";

        // add thickness; specified in Java in pixels at 72 pixels/inch; needs to be in 1/100 of mm for DXF, and restricted range of values
        result += "370\n" + getDXFLineWeight(stroke.getLineWidth()) + "\n";

        // add color number
        result += "62\n" + DXFColor.getClosestDXFColor(color.getRGB()) + "\n";

        return result;
    }


    public String getDXFHatchInfo()
    {
        // elliptical arc
        String result = "72\n" + "3" + "\n";

        // center
        result += "10\n" + setPrecision(center.x) + "\n";
        result += "20\n" + setPrecision(center.y) + "\n";

        // major axis endpoint relative to center
        result += "11\n" + setPrecision(majorAxisEndpoint.x) + "\n";
        result += "21\n" + setPrecision(majorAxisEndpoint.y) + "\n";

        // radius ratio
        result += "40\n" + setPrecision(axisRatio) + "\n";

        // start/end angles - IN DEGREES FOR HATCH!
        double startAngle = findHatchAngleDegrees(startParameter);
        double endAngle = findHatchAngleDegrees(endParameter);


        // do some stuff to accommodate LibreCAD, which ignores counterclockwise flag and doesn't like negative angles or those
        // outside the range 0-360 - not sure how much to try to accommodate...
        // make 'em positive
        while (startAngle < 0)
            startAngle += 360;
        while (endAngle < 0)
            endAngle += 360;

        // stupid stuff for float/double rounding
        if (startAngle >= 360)
                startAngle -= 360;
        if (endAngle >= 360)
            endAngle -= 360;

        // LibreCAD draws counterclockwise if start < end, clockwise if end < start
        if (isCounterclockwise)
        {
            if (endAngle < startAngle)
                endAngle += 360;
        }

        if (!isCounterclockwise)
        {
            // need to reverse start and end angles
            double temp = startAngle;
            startAngle = endAngle;
            endAngle = temp;
        }


        result += "50\n" + setPrecision(startAngle) + "\n";
        result += "51\n" + setPrecision(endAngle) + "\n";

        // counterclockwise flag
        if (isCounterclockwise)
            result += "73\n" + "1" + "\n";
        else
            result += "73\n" + "0" + "\n";

        return result;
    }

    /*
     * Gah!!!!! For some godforsaken reason, everybody interprets the angle in a hatch as the geometric angle
     * rather than the parameter, which are different for a non-circular ellipse. For example, a parameter of
     * pi/4 represents the ray that goes from the center of the rectangle through the upper right corner, whereas
     * the geometric angle of 45 degrees only goes through the corner if the rectangle is a square. The hatch angle is
     * expected to be the geometric angle rather than the parameter in degrees. So we need to determine what the geometric
     * angle is to return in the hatch definition.
     */
    private double findHatchAngleDegrees(double parameter)
    {
        double x = Math.cos(parameter);
        double y = axisRatio * Math.sin(parameter);

        double angle;
        if (x == 0)
        {
            if (y > 0)
                angle = Math.PI/2;
            else
                angle = 3 * Math.PI/2;
        }
        else
        {
            angle = Math.atan(Math.abs(y/x));
        }

        if ((x < 0) && (y < 0))
        {
            angle += Math.PI;
        }
        else if ((x < 0) && (y > 0))
        {
            angle = Math.PI - angle;
        }
        else if ((x > 0) && (y < 0))
        {
            angle = 2 * Math.PI - angle;
        }

        return (angle * 180 / Math.PI);

    }

}
