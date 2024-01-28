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



/**
 * Class representing a header segment for use in HEADERS section. This one is
 * specific to stuff that AutoCAD needs to keep it happy; while it's technically
 * a subclass of the general DXFHeaderSegment class, it's handled distinctly.
 * @author jsevy
 *
 */
public class DXFHeaderSegmentAutoCAD extends DXFHeaderSegment
{
    private String acadVersion;
    private int handleLimit;

    /**
     * Create a header object with AutoCAD-specific attributes.
     *
     * @param acadVersion	String indicating version of AutoCAD this is compatible with
     */
    public DXFHeaderSegmentAutoCAD(String acadVersion)
    {
        //super();

        this.acadVersion = acadVersion;

        // set handleLimit to some huge value; will trim later after entities defined
        this.handleLimit = 20000;
    }


    /**
     * Set handleLimit; used after entities defined to trim from initial (large) value
     *
     * @param handleLimit   Maximum value for handles used in the file
     */
    public void setHandleLimit(int handleLimit)
    {
        this.handleLimit = handleLimit;
    }


    /**
     * Implementation of DXFObject interface method; creates DXF text representing the object.
     */
    public String toDXFString()
    {
        String returnString = new String();

        // print our superclass stuff in case the user decided to add some
        // additional header lines here...
        returnString += super.toDXFString();

        // print out minimum AutoCAD version
        returnString += "9\n$ACADVER\n";
        returnString += "1\n" + acadVersion + "\n";

        // print out limit of handles used
        returnString += "9\n$HANDSEED\n";
        returnString += "5\n" + Integer.toHexString(handleLimit) + "\n";

        return returnString;
    }

}
