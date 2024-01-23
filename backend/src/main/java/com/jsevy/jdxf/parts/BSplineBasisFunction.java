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




/**
 * Class for a B-spline basis function.
 * @author jsevy
 *
 */
public class BSplineBasisFunction
                implements BasisFunction
{
    private int degree;
    
    
    public BSplineBasisFunction(int support)
    {
        this.degree = support - 1;
    }
    
    
    public int getSupport()
    {
        return degree + 1;
    }
    
    
    public double value(double t)
    {
        //System.out.println("BSplineBasisFunction value called: t = " + t);
        return recursiveValue(0, degree, t);
    }
    
    
    private double recursiveValue(int i, int degree, double t)
    {
        double value;
        
        if (degree > 0)
        {
            value = ( (t - i) * recursiveValue(i, degree-1, t)  +  ((i+degree+1) - t) * recursiveValue(i+1, degree-1, t) )/degree;
        }
        else
        {
            if ((i <= t) && (t < (i+1)))
            {
                value = 1;
            }
            else
            {
                value = 0;
            }
        }
        
        //System.out.println("recursiveValue done: i = " + i + ", degree = " + degree + ", t = " + t + ", value = " + value);
        
        return value;
    }
    
}