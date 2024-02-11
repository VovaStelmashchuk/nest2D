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
 * Class representing a line segment.
 *
 * @author jsevy
 */
public class DXFLine extends DXFEntity {
    private RealPoint start, end;

    /**
     * Create a line segment between the specified endpoints.
     *
     * @param start One endpoint
     * @param end   The other endpoint
     */
    public DXFLine(RealPoint start, RealPoint end) {
        this.start = new RealPoint(start);
        this.end = new RealPoint(end);
    }


    /**
     * Implementation of DXFObject interface method; creates DXF text representing the line segment.
     */
    public String toDXFString() {
        String result = "0\nLINE\n";

        // print out handle and superclass marker(s)
        result += super.toDXFString();

        // print out subclass marker
        result += "100\nAcDbLine\n";

        result += "10\n" + setPrecision(start.x) + "\n";
        result += "20\n" + setPrecision(start.y) + "\n";

        result += "11\n" + setPrecision(end.x) + "\n";
        result += "21\n" + setPrecision(end.y) + "\n";

        return result;
    }


    public String getDXFHatchInfo() {
        // line
        String result = "72\n" + "1" + "\n";

        result += "10\n" + setPrecision(start.x) + "\n";
        result += "20\n" + setPrecision(start.y) + "\n";

        result += "11\n" + setPrecision(end.x) + "\n";
        result += "21\n" + setPrecision(end.y) + "\n";

        return result;
    }
}
