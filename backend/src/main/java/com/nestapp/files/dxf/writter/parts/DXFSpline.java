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

import java.awt.*;
import java.util.Vector;


/**
 * Class representing a general B-spline
 *
 * @author jsevy
 */
public class DXFSpline extends DXFEntity {
    private int degree;
    private Vector<RealPoint> expandedControlPoints;
    private double[] knots;
    private boolean closed;


    /**
     * Create a spline of specified degree for the specified control points, using uniform knot vector.
     *
     * @param degree           Degree of the piecewise-polynomial spline segments
     * @param controlPoints    Locations and weights of the control points for the spline
     * @param throughEndpoints If true, the spline will be forced to pass through the endpoints by setting the end
     *                         control point multiplicities to degree + 1
     */
    /*public DXFSpline(int degree, Vector<SplineControlPoint> controlPoints, boolean throughEndpoints) {

        // if pass through endpoints, set multiplicities of first and last control points to degree + 1
        if (throughEndpoints) {
            controlPoints.elementAt(0).multiplicity = degree + 1;
            controlPoints.elementAt(controlPoints.size() - 1).multiplicity = degree + 1;
        }

        this.degree = degree;
        createExpandedPointVector(controlPoints);
        this.closed = false;

        // here we use evenly spaced knots, one per expanded control point; have extras at end to keep AutoCAD happy...
        knots = new double[expandedControlPoints.size() + degree + 1];
        for (int i = 0; i < knots.length; i++) {
            knots[i] = i;
        }
    }*/


    /**
     * Create a spline of specified degree for the specified control points, using supplied knot vector.
     *
     * @param degree        Degree of the piecewise-polynomial spline segments
     * @param controlPoints Locations of the control points for the spline
     * @param knots         The knot sequence; length must be controlPoints.size() + degree + 1
     */
    public DXFSpline(int degree, double[] controlPoints, double[] knots) {

        this.degree = degree;
        this.closed = true;

        // populate expanded control point vector
        expandedControlPoints = new Vector<>();
        for (int i = 0; i < controlPoints.length / 2; i++) {
            RealPoint controlPoint = new RealPoint(controlPoints[2 * i], controlPoints[2 * i + 1]);
            expandedControlPoints.add(controlPoint);
        }

        // here we use the supplied knots; number expected to be equal to controlPoints.size() + degree + 1
        this.knots = knots;

    }


    /**
     * Implementation of DXFObject interface method; creates DXF text representing the spline.
     */
    public String toDXFString() {
        String result = "0\nSPLINE\n";

        // print out handle and superclass marker(s)
        result += super.toDXFString();

        // print out subclass marker
        result += "100\nAcDbSpline\n";

        // include degree of spline
        result += "71\n" + degree + "\n";

        // include number of knots
        result += "72\n" + (expandedControlPoints.size() + degree + 1) + "\n";

        // include number of control points
        result += "73\n" + expandedControlPoints.size() + "\n";

        // indicate if closed
        if (closed) {
            result += "70\n1\n";
        }

        // include expanded list of control points, with multiplicities for control points;
        // knots are augmented at end with n+1 points to make AutoCAD happy

        // knots first (since there are more of them than control points)
        for (int i = 0; i < expandedControlPoints.size() + degree + 1; i++) {
            result += "40\n" + setPrecision(knots[i]) + "\n";
        }

        // now control points and weights
        for (int i = 0; i < expandedControlPoints.size(); i++) {
            RealPoint point = expandedControlPoints.elementAt(i);
            result += "10\n" + setPrecision(point.x) + "\n";
            result += "20\n" + setPrecision(point.y) + "\n";
            result += "41\n1\n";                // all weights 1; multiplicities already accounted for
        }

        return result;
    }


    public String getDXFHatchInfo() {
        // spline
        String result = "72\n" + "4" + "\n";

        // degree
        result += "94\n" + degree + "\n";

        // not rational
        result += "73\n" + "0" + "\n";

        // not periodic
        result += "74\n" + "0" + "\n";

        // include number of knots
        result += "95\n" + (expandedControlPoints.size() + degree + 1) + "\n";

        // include number of control points
        result += "96\n" + expandedControlPoints.size() + "\n";

        // knots first (since there are more of them than control points)
        for (int i = 0; i < expandedControlPoints.size() + degree + 1; i++) {
            result += "40\n" + setPrecision(knots[i]) + "\n";
        }

        // now control points and weights
        for (int i = 0; i < expandedControlPoints.size(); i++) {
            RealPoint point = expandedControlPoints.elementAt(i);
            result += "10\n" + setPrecision(point.x) + "\n";
            result += "20\n" + setPrecision(point.y) + "\n";
            // all weights 1; multiplicities already accounted for
            //result += "42\n1\n";
        }

        return result;
    }


    /**
     * Create vector of control points with points multiply represented according to their multiplicities,
     * and appropriate multiplicity at endpoints to pass through these.
     */
    private void createExpandedPointVector(Vector<SplineControlPoint> controlPoints) {

        int index = 0;

        expandedControlPoints = new Vector<RealPoint>();

        if (controlPoints.size() != 0) {

            for (int j = 0; j < controlPoints.size(); j++) {
                SplineControlPoint controlPoint = controlPoints.elementAt(j);
                controlPoint.expandedIndex = index;

                for (int i = 0; i < controlPoint.multiplicity; i++) {
                    expandedControlPoints.add(new RealPoint(controlPoint.x, controlPoint.y));
                    index++;
                }
            }
        }
    }
}
