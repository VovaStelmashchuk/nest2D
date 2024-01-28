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
 * Utility class representing a spline control point, specifying both a 3-dimensional position and a weight.
 *
 * @author jsevy
 *
 */
public class SplineControlPoint extends RealPoint
{
    private static final long serialVersionUID = 1L;

	public int multiplicity;

	// used internally, within expanded point vector
    public int expandedIndex;


    public SplineControlPoint (SplineControlPoint other)
    {
        super(other.x, other.y);
        this.multiplicity = other.multiplicity;
    }

    public boolean equals(Object object)
    {
        if(!(object instanceof SplineControlPoint))
            return false;

        SplineControlPoint other = (SplineControlPoint)object;

        return (this.x == other.x) && (this.y == other.y) && (this.multiplicity == other.multiplicity);
    }
}
