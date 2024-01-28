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
 * Class representing a viewport for use in VPORTs table.
 * @author jsevy
 *
 */
public class DXFViewport extends DXFTableRecord
{
    private String name;
    private int viewportHeight;


    /**
     * Create a VPORT table record object with specified name.
     *
     * @param name  name of table record
     * @param viewportHeight  Height of viewport
     */
    public DXFViewport(String name, int viewportHeight)
    {
        // generate a name - just use digits
        this.name = name;
        this.viewportHeight = viewportHeight;
    }


    /**
     * Implementation of DXFObject interface method; creates DXF text representing the text style.
     */
    public String toDXFString()
    {
        String returnString = new String();

        returnString += "0\nVPORT\n";

        // print out handle and superclass marker(s)
        returnString += super.toDXFString();

        // print out subclass marker
        returnString += "100\nAcDbViewportTableRecord\n";

        returnString += "2\n" + name + "\n";

        returnString += "40\n" + viewportHeight + "\n";

        // no flags set
        returnString += "70\n0\n";

        return returnString;
    }

}
