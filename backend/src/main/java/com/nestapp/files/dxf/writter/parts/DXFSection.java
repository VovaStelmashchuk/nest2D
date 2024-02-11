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



/**
 * DXFContainer subclass for sections in the DXF document.
 *
 * @author jsevy
 *
 */
public class DXFSection extends DXFContainer
{
    private static final long serialVersionUID = 1L;

    public String name;


    /**
     * Create a DXF section with the specified name.
     *
     * @param name	Name of the section
     */
    public DXFSection(String name)
    {
        this.name = name;
    }

    /**
     * Implementation of DXFObject interface method; creates DXF text for the section, embedding text generated
     * by its contained DXF objects/entities.
     */
    public String toDXFString()
    {
        String returnString = new String();

        returnString += "0\nSECTION\n";
        returnString += "2\n" + name + "\n";

        returnString += super.toDXFString();

        returnString += "0\nENDSEC\n";

        return returnString;
    }

}
