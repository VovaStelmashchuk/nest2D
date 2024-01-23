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


import java.awt.geom.AffineTransform;
import java.io.Serializable;




/**
 * Utility class representing a 3-dimensional point
 * 
 * @author jsevy
 *
 */
public class RealPoint implements Serializable
{
    private static final long serialVersionUID = 1L;
	
	public double x, y, z;
    
    public RealPoint (double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public RealPoint (RealPoint other)
    {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
    }
    
   
    public static RealPoint sum(RealPoint p1, RealPoint p2)
    {
        return new RealPoint(p1.x + p2.x, p1.y + p2.y, p1.z + p2.z);
    }
    
    public static RealPoint difference(RealPoint p1, RealPoint p2)
    {
        return new RealPoint(p1.x - p2.x, p1.y - p2.y, p1.z - p2.z);
    }
    
    public static RealPoint scalarProduct(double a, RealPoint p)
    {
        return new RealPoint(a * p.x, a * p.y, a * p.z);
    }
    
    
    public static RealPoint mapPoint(AffineTransform m, RealPoint p)
    {
        double[] src = {p.x, p.y};
        double[] dst = new double[2];
        m.transform(src, 0, dst, 0, 1);
        return new RealPoint(dst[0], dst[1], p.z);
    }
    
    public static RealPoint mapVector(AffineTransform m, RealPoint p)
    {
        // create a version of the transform without the translation
        double[] matrixCoeffs = {0,0,0,0,0,0};
        m.getMatrix(matrixCoeffs);
        // zero out translation components
        matrixCoeffs[4] = 0;
        matrixCoeffs[5] = 0;
        AffineTransform l = new AffineTransform(matrixCoeffs);
        
        double[] src = {p.x, p.y};
        double[] dst = new double[2];
        l.transform(src, 0, dst, 0, 1);
        return new RealPoint(dst[0], dst[1], p.z);
    }
    
    
    public static double magnitude(RealPoint p)
    {
        return Math.sqrt(p.x*p.x + p.y*p.y + p.z*p.z);
    }
    
    public static double dotProduct(RealPoint p, RealPoint q)
    {
        return p.x*q.x + p.y*q.y + p.z*q.z;
    }
    
    public static RealPoint crossProduct(RealPoint p, RealPoint q)
    {
        RealPoint result = new RealPoint(p.y*q.z-p.z*q.y, p.z*q.x-p.x*q.z, p.x*q.y-p.y*q.x);
        return result;
    }
    
    public static double angleBetween(RealPoint p, RealPoint q)
    {
        double magnitudeProd = magnitude(p)*magnitude(q);
        if (magnitudeProd == 0)
        {
            return 0;
        }
        else
        {
            return Math.acos(dotProduct(p, q)/magnitudeProd);
        }
    }
    
    public boolean equals(Object object)
    {
        if(!(object instanceof RealPoint))
            return false;
            
        RealPoint other = (RealPoint)object;
        
        if ((this.x == other.x) && (this.y == other.y) && (this.z == other.z))
            return true;
        else
            return false;
    }
    
    
}