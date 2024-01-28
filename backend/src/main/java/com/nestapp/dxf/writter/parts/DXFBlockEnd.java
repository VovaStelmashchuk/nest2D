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
 * Class representing a block end object for use with block in BLOCKs table.
 * @author jsevy
 *
 */
public class DXFBlockEnd extends DXFEntity
{
    @SuppressWarnings("unused")
    private DXFBlock block;

    /**
     * Create a block end table record object for specified block.
     *
     * @param block	Associated block
     */
    public DXFBlockEnd(DXFBlock block)
    {
        this.block = block;
    }


    /**
     * Implementation of DXFObject interface method; creates DXF text representing the object.
     */
    public String toDXFString()
    {
        String returnString = new String();

        returnString += "0\nENDBLK\n";

        // print out handle and superclass marker(s)
        returnString += super.toDXFString();

        // set reference to the block that begins this; not actually needed, though...
        //returnString += "330\n" + Integer.toHexString(block.getHandle()) + "\n";

        // print out subclass marker
        returnString += "100\nAcDbBlockEnd\n";

        return returnString;
    }

}
