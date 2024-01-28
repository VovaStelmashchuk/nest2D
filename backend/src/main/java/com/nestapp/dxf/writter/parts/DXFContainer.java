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

import java.util.Vector;



/**
 * A base class for containers of objects in a DXF document, including sections and tables.
 *
 * @author jsevy
 *
 */
public abstract class DXFContainer extends Vector<DXFObject> implements DXFObject
{
    private static final long serialVersionUID = 1L;


    /**
     * Implementation of DXFObject interface method; just calls the toDXFString method of each of the
     * DXFObjects it contains. Subclasses of this class will generally insert their own text before and
     * after calling this superclass method to encapsulate the strings from the contained DXFObjects.
     */
    public String toDXFString()
    {
        String returnString = new String();

        for (int i = 0; i < this.size(); i++)
        {
            returnString += this.elementAt(i).toDXFString();
        }

        return returnString;
    }

}
