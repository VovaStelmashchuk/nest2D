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
 * Class representing a block for use in BLOCKs table.
 * @author jsevy
 *
 */
public class DXFBlock extends DXFEntity
{
    private String name;

    /**
     * Create a BLOCK table record object with specified name.
     *
     * @param name	name of table record
     */
    public DXFBlock(String name)
    {
        this.name = name;
    }


    /**
     * Implementation of DXFObject interface method; creates DXF text representing the object.
     */
    public String toDXFString()
    {
        String returnString = new String();

        returnString += "0\nBLOCK\n";

        // print out handle and superclass marker(s)
        returnString += super.toDXFString();

        // print out subclass marker
        returnString += "100\nAcDbBlockBegin\n";

        // print out name
        returnString += "2\n" + name + "\n";

        // no flags set
        returnString += "70\n0\n";

        // block left corner
        returnString += "10\n0\n";
        returnString += "20\n0\n";
        returnString += "30\n0\n";

        // print out name again?
        returnString += "3\n" + name + "\n";

        // xref path name - nothing
        returnString += "1\n\n";



        return returnString;
    }

}
