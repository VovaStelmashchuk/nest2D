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


import java.awt.*;
import java.util.Vector;




/**
 * Class representing a line segment.
 * @author jsevy
 *
 */
public class DXFHatch extends DXFEntity
{
    // need a list of boundaries to handle regions with holes
    private Vector<Vector<DXFEntity>> boundaries;
    private Color color;

    /**
     * Create a Hatch with the supplied list of boundaries
     *
     * @param boundaries     The boundaries of the hatch, as a vector of vectors of DXF entities
     * @param color         The color of the hatch
     */
    public DXFHatch(Vector<Vector<DXFEntity>> boundaries, Color color)
    {
        this.boundaries = boundaries;
        this.color = color;
    }



    /**
     * Implementation of DXFObject interface method; creates DXF text representing the line segment.
     */
    public String toDXFString()
    {
        String result = "0\nHATCH\n";

        // print out handle and superclass marker(s)
        result += super.toDXFString();

        // print out subclass marker
        result += "100\nAcDbHatch\n";

        // elevation - just 0's - AutoCAD wants it...
        result += "10\n" + "0" + "\n";
        result += "20\n" + "0" + "\n";
        result += "30\n" + "0" + "\n";

        // extrusion direction - supposed to be optional, but not for AutoCAD viewer... grr...
        result += "210\n" + "0" + "\n";
        result += "220\n" + "0" + "\n";
        result += "230\n" + "1" + "\n";

        // needs to have a name for AutoCAD; just call it a fill with handle number for uniqueness
        result += "2\n" + "FILL_" + this.handle + "\n";

        // solid fill indicator (rather than pattern)
        result += "70\n" + "1" + "\n";

        // associativity(?) - (make AutoCAD happy)
        result += "71\n" + "0" + "\n";

        // number of boundaries
        result += "91\n" + boundaries.size() + "\n";

        for (int j = 0; j < boundaries.size(); j++)
        {
            Vector<DXFEntity> boundary = boundaries.elementAt(j);

            // type of boundary - default
            result += "92\n" + "0" + "\n";

            // number of boundary curves
            result += "93\n" + boundary.size() + "\n";

            // print out boundary data for each boundary curve
            for (int i = 0; i < boundary.size(); i++)
            {
                result += boundary.elementAt(i).getDXFHatchInfo();
            }

            // number of source boundary objects - 0, for AutoCAD
            result += "97\n" + "0" + "\n";

        }

        // add color number
        result += "62\n" + DXFColor.getClosestDXFColor(color.getRGB()) + "\n";

        // y'know how order really shouldn't matter for these hatch parameters? Well
        // somebody should tell AutoCAD Viewer that!!! These need to be here, not before the boundary specs - gah!!!
        // hatch style - odd parity (make AutoCAD happy)
        result += "75\n" + "0" + "\n";

        // hatch type - predefined (make AutoCAD happy)
        result += "76\n" + "1" + "\n";

        // number of seed points - 0 (make AutoCAD happy)
        result += "98\n" + "0" + "\n";

        return result;
    }
}
