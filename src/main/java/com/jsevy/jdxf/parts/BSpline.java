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

package com.jsevy.jdxf.parts;



import java.awt.Graphics;
import java.util.*;


/**
 * Class to represent a B-spline (non-rational and with evenly spaced knots).
 * @author jsevy
 *
 */
public class BSpline
{
    private Vector<SplineControlPoint> controlPoints;
    private Vector<RealPoint> expandedPoints;
    private BasisFunction basisFunction;
    
    
    
    /**
     * Create e new B-spline of the specified degree using the supplied control points
     * 
     * @param degree		Degree of the spline polynomial pieces
     * @param controlPoints	The set of control points used to define the spline
     */
    public BSpline(int degree, Vector<SplineControlPoint> controlPoints)
    {
        this.basisFunction = new BSplineBasisFunction(degree + 1 /*support*/);
        this.controlPoints = controlPoints;
        createExpandedPointVector();
    }
    
    
    
    /**
     * Create a spline of the specified degree using the supplied x,y coordinate pairs as the control points. 
     * 
     * @param degree			The degree of the underlying piecewise-polynomial segments
     * @param controlPoints		The control points used to define the shape of the spline, {x1, y1, x2, y2, ..., xn, yn}
     * @param multiplicities	Array giving the multiplicity of each of the control points. The size of the multiplicities
     * 							must be half the size of the controlPoints array.
     * @param throughEndpoints	Set true to force the spline curve to pass through the end control points. This is accomplished
     * 							by forcing the multiplicity of the first and last control points to degree + 1. Note that this
     * 							will give a different spline curve than that typically referred to as a "clamped" spline, in
     * 							which the spline knots are repeated at the beginning and end to force the spline to pass through
     * 							these points. The repeated control-point approach has the advantage of having the "open" spline
     * 							(where throughEndpoints is false) being a subset of the "closed" curve; this is not true for 
     * 							"unclamped" and "clamped" splines.
     */
    public BSpline(int degree, double[] controlPoints, int[] multiplicities, boolean throughEndpoints)
    {
        this.basisFunction = new BSplineBasisFunction(degree + 1 /*support*/);
        this.controlPoints = new Vector<SplineControlPoint>();
        
        // copy the control point and weight info into our control point vector
        for (int i = 0; i < multiplicities.length; i++)
        {
            SplineControlPoint controlPoint = new SplineControlPoint(controlPoints[2*i], controlPoints[2*i+1], 0, multiplicities[i]);
            this.controlPoints.add(controlPoint);
        }
        
        // if pass through endpoints, set multiplicities of first and last control points to support
        if (throughEndpoints)
        {
        	int support = basisFunction.getSupport();
        	this.controlPoints.elementAt(0).multiplicity = support;
        	this.controlPoints.elementAt(this.controlPoints.size()-1).multiplicity = support;
        }
        
        createExpandedPointVector();
    }
    
    
    /**
     * An extension method that supports the drawing of a B-spline. If the supplied Graphics is an instance of DXFGraphics,
     * this generates a native DXF spline type. For other Graphics types, this draws the spline as a set of line segments
     * since there are no native spline-drawing routines in the standard Java. The tolerance parameter indicates the 
     * maximum length of each line segment to control the smoothness of the representation. For the common case of a spline 
     * that passes through the first and last control points, set the multiplicity of these equal to the spline degree plus 1, 
     * or set the throughEndpoints parameter to true, which does the same internally.
     * 
     * @param degree        The degree of the spline; must be greater than 0
     * @param controlPoints The control points that define the shape of the spline, {x1, y1, x2, y2, ..., xn, yn}.
     * @param multiplicities    The multiplicities for the control points; the number of multiplicity values must equal the
     *                      number of control points (i.e., the array size must be half that of the controlPoints array)
     * @param throughEndpoints  If true, the spline will be forced to pass through the endpoints by setting the end
     *                      control point multiplicities to degree + 1
     * @param tolerance     For non-DXF Graphics types, the spline is drawn as a set of connected line segments; the tolerance
     *                      specifies that maximum lenth of each line segment to control the smoothness of the representation.
     *                      Ignored for DXFGraphics, which has a true spline representation.                     
     * @param graphics          The graphics object specifying parameters for the arc (color, thickness)
     */
    public static void drawSpline(Graphics graphics, int degree, double[] controlPoints, int[] multiplicities, boolean throughEndpoints, double tolerance)
    {
        if (graphics instanceof DXFGraphics)
        {
            // use the extension method in DXFGraphics to create a true spline rather than a line-segment approximation
            ((DXFGraphics)graphics).drawSpline(degree, controlPoints, multiplicities, throughEndpoints);
        }
        else
        {
            // create a spline and draw line segments
            BSpline bSpline = new BSpline(degree, controlPoints, multiplicities, throughEndpoints);
            bSpline.drawSpline(graphics, tolerance);
        }
    }
    
    
    
    /**
     * Draw the spline on the supplied Graphics as a series of line segments of maximum length tolerance.
     * 
     * @param graphics	The Graphics onto which the spline should be drawn
     * @param paint		The Paint to use in drawing (determines line width, color, etc.)
     * @param tolerance	The maximum length of each of the line segments used to approximate the smooth curve
     */
    private void drawSpline(Graphics graphics, double tolerance)
    {
    	
    	Vector<RealPoint> points = this.getCurvePoints(tolerance);
        for (int i = 0; i < points.size()-1; i++)
        {
            graphics.drawLine((int)points.elementAt(i).x, (int)points.elementAt(i).y, (int)points.elementAt(i+1).x, (int)points.elementAt(i+1).y);
        }
    }
    
    
    
    /**
     * Get a set of points on the spline curve, where the maximum distance between two adjacent points is given by tolerance.
     * 
     * @param tolerance		The maximum distance between any two adjacent points in the return vector
     * @return				A vector containing a set of points which lie on the spline curve
     */
    public Vector<RealPoint>getCurvePoints(double tolerance)
    {
        double tStart, tEnd;
        Vector<RealPoint> pointsVector = new Vector<RealPoint>();
        
        // draw entire curve
        if (controlPoints.size() > 0)
        {
            //SplineControlPoint controlPoint = controlPoints.elementAt(0);
            //tStart = controlPoint.expandedIndex;
        	tStart = basisFunction.getSupport();
            
            //controlPoint = controlPoints.elementAt(controlPoints.size() - 1);
            //tEnd = controlPoint.expandedIndex + basisFunction.getSupport();
        	tEnd = expandedPoints.size() - 1;
            
            for (int i = (int)Math.round(tStart); i < (int)Math.round(tEnd); i++)
            {
                pointsVector.addAll(drawCurve(i, i+1, tolerance));
            }
        }
        
        return pointsVector;
        
    }
    
    
    
    private RealPoint computePoint(double t)
    {
        int i = (int)Math.round(Math.floor(t));
        RealPoint curvePoint = new RealPoint(0,0,0);
        int support = basisFunction.getSupport();
        
        for (int j = 0; j < support; j++)
        {
            RealPoint controlPoint = expandedPoints.elementAt(i - j);
     
            double basisFn = basisFunction.value(t - (i - j));
            
            curvePoint.x +=  controlPoint.x * basisFn;
            curvePoint.y +=  controlPoint.y * basisFn;
            curvePoint.z +=  controlPoint.z * basisFn;
        }
        
        return curvePoint;
    }    
    
    
    
    private Vector<RealPoint> drawCurve(double tStart, double tEnd, double tolerance)
    {
        Vector<RealPoint> pointsVector = new Vector<RealPoint>();
        
        RealPoint startPoint = computePoint(tStart);
        RealPoint endPoint = computePoint(tEnd);
        
        pointsVector.add(startPoint);
        
        // fill in remainder of pointsVector through recursive draw routine
        drawCurve(tStart, startPoint, tEnd, endPoint, tolerance, pointsVector);
        
        return pointsVector;
        
    }
    
    
    private void drawCurve(double tStart, RealPoint startPoint, double tEnd, RealPoint endPoint, double tolerance, Vector<RealPoint> pointsVector)
    {
        // recursive draw routine
        //System.out.println("drawCurve entered: " + tStart + ", "  + tEnd);

        if (RealPoint.magnitude(RealPoint.difference(startPoint, endPoint)) <= tolerance)
        {
            //System.out.println("  added point: " + endPoint.x + ", "  + endPoint.y + ", " + endPoint.z);
            pointsVector.add(endPoint);
        }
        else
        {
            double tMid = (tStart + tEnd)/2;
            RealPoint midPoint = computePoint(tMid);
            drawCurve(tStart, startPoint, tMid, midPoint, tolerance, pointsVector);
            drawCurve(tMid, midPoint, tEnd, endPoint, tolerance, pointsVector);
        }
    }
    
    
    /**
     * Create vector of control points with points duplicated according to their multiplicities
     */
    private void createExpandedPointVector()
    {
        int support = basisFunction.getSupport();
        int index = -support + 1;
        
        expandedPoints = new Vector<RealPoint>();
        
        if (controlPoints.size() == 0)
            return;
            
        for (int j = 0; j < controlPoints.size(); j++)
        {
            SplineControlPoint controlPoint = controlPoints.elementAt(j);
            controlPoint.expandedIndex = index;
            
            for (int i = 0; i < controlPoint.multiplicity; i++)
            {
                expandedPoints.add(controlPoint);
                index++;
            }
            
        }
        
    }
    
    
    
    public String toString()
    {
        String result = new String();
        
        result += "Control points:\n";
        for (int i = 0; i < controlPoints.size(); i++)
        {
            SplineControlPoint controlPoint = controlPoints.elementAt(i);
            
            result += "x = " + controlPoint.x + ", y = " + controlPoint.y + ", z = " + controlPoint.z + ", weight = " + controlPoint.multiplicity + ", expanded index = " + controlPoint.expandedIndex + "\n";
        }
        
        result += "Expanded points:\n";
        for (int i = 0; i < expandedPoints.size(); i++)
        {
            RealPoint point = expandedPoints.elementAt(i);
            
            result += "x = " + point.x + ", y = " + point.y + ", z = " + point.z + "\n";
        }
        
        return result;
    }
    
    

    
}