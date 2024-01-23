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

import java.util.Vector;

/**
 * Class representing a header for use in HEADERS section.
 * @author jsevy
 *
 */
public class DXFHeaderSegment implements DXFObject
{
    private class HeaderLine
    {
        public String name;
        public int code;
        public String value;
        
        public HeaderLine(String name, int code, String value)
        {
            this.name = name;
            this.code = code;
            this .value = value;
        }
       
    }
    
    private Vector<HeaderLine> lines;
    
    /**
     * Create a header segment that can have header lines.
     */
    public DXFHeaderSegment()
    {
        lines = new Vector<HeaderLine>();
    }
    
    
    public void addHeaderLine(String name, int code, String value)
    {
        HeaderLine headerLine = new HeaderLine(name, code, value);
        lines.add(headerLine);
    }
    
    
    /**
     * Implementation of DXFObject interface method; creates DXF text representing the object.
     */
    public String toDXFString()
    {
        String returnString = new String();
        
        // iterate over header lines
        for (int i = 0; i < lines.size(); i++)
        {
            HeaderLine headerLine = lines.elementAt(i);
                    
            // print out each line
            returnString += "9\n";
            returnString += headerLine.name + "\n";
            returnString += headerLine.code + "\n";
            returnString += headerLine.value + "\n";
        }
        
        return returnString;
    }
    
}