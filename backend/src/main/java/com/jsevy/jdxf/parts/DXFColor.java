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

import java.awt.*;



/**
 * Class providing static methods for handling colors of entities in DXF file. Colors are represented by
 * an index into a table, rather than RGB triples. The methods of this class help to find the match in the
 * table for a specified RGB color so that the table index can be used in the entity definition.
 *
 * @author jsevy
 *
 */
public class DXFColor
{

	/**
	 * Find the closest match for a given RGB color in the standard DXF color table. The best match is selected on
	 * the basis of minimum distance using R, G and B asaxes.
	 *
	 * @param rgbColor	The RGB color for which a match is desired, represented as a standard Java argb color
	 * 					(alpha is ignored)
	 * @return			The standard DXF color table index for the color that is the closest match to that supplied
	 */
	public static int getClosestDXFColor(int rgbColor)
    {
        // run through the list of colors and return the one that's closest in the sense of RGB distance
        Color color = new Color(rgbColor);
        int r1 = color.getRed();
        int g1 = color.getGreen();
        int b1 = color.getBlue();

        int bestMatch = 0;
        int closestDistance = 3*255*255;

        // start at index 1 since 0 not used - except it is used by AutoCAD - gah!!
        for (int i = 0; i < rgbTable.length; i++)
        {
            int r2 = rgbTable[i][0];
            int g2 = rgbTable[i][1];
            int b2 = rgbTable[i][2];

            int distance = (r1-r2)*(r1-r2) + (g1-g2)*(g1-g2) + (b1-b2)*(b1-b2);
            if (distance < closestDistance)
            {
                bestMatch = i;
                closestDistance = distance;
            }

            if (distance == 0)
            {
                // found exact match
                break;
            }
        }

        return bestMatch;
    }


	/**
	 * The standard DXF color table rgb values.
	 */
    private static int[][] rgbTable =
        {
            {0,0,0},    // index 0 - not used - actually, used by AutoCAD online viewer
            {255,0,0},
            {255,255,0},
            {0,255,0},
            {0,255,255},
            {0,0,255},
            {255,0,255},
            {255,255,255},
            {128,128,128},
            {192,192,192},
            {255,0,0},
            {255,127,127},
            {165,0,0},
            {165,82,82},
            {127,0,0},
            {127,63,63},
            {76,0,0},
            {76,38,38},
            {38,0,0},
            {38,19,19},
            {255,63,0},
            {255,159,127},
            {165,41,0},
            {165,103,82},
            {127,31,0},
            {127,79,63},
            {76,19,0},
            {76,47,38},
            {38,9,0},
            {38,23,19},
            {255,127,0},
            {255,191,127},
            {165,82,0},
            {165,124,82},
            {127,63,0},
            {127,95,63},
            {76,38,0},
            {76,57,38},
            {38,19,0},
            {38,28,19},
            {255,191,0},
            {255,223,127},
            {165,124,0},
            {165,145,82},
            {127,95,0},
            {127,111,63},
            {76,57,0},
            {76,66,38},
            {38,28,0},
            {38,33,19},
            {255,255,0},
            {255,255,127},
            {165,165,0},
            {165,165,82},
            {127,127,0},
            {127,127,63},
            {76,76,0},
            {76,76,38},
            {38,38,0},
            {38,38,19},
            {191,255,0},
            {223,255,127},
            {124,165,0},
            {145,165,82},
            {95,127,0},
            {111,127,63},
            {57,76,0},
            {66,76,38},
            {28,38,0},
            {33,38,19},
            {127,255,0},
            {191,255,127},
            {82,165,0},
            {124,165,82},
            {63,127,0},
            {95,127,63},
            {38,76,0},
            {57,76,38},
            {19,38,0},
            {28,38,19},
            {63,255,0},
            {159,255,127},
            {41,165,0},
            {103,165,82},
            {31,127,0},
            {79,127,63},
            {19,76,0},
            {47,76,38},
            {9,38,0},
            {23,38,19},
            {0,255,0},
            {127,255,127},
            {0,165,0},
            {82,165,82},
            {0,127,0},
            {63,127,63},
            {0,76,0},
            {38,76,38},
            {0,38,0},
            {19,38,19},
            {0,255,63},
            {127,255,159},
            {0,165,41},
            {82,165,103},
            {0,127,31},
            {63,127,79},
            {0,76,19},
            {38,76,47},
            {0,38,9},
            {19,38,23},
            {0,255,127},
            {127,255,191},
            {0,165,82},
            {82,165,124},
            {0,127,63},
            {63,127,95},
            {0,76,38},
            {38,76,57},
            {0,38,19},
            {19,38,28},
            {0,255,191},
            {127,255,223},
            {0,165,124},
            {82,165,145},
            {0,127,95},
            {63,127,111},
            {0,76,57},
            {38,76,66},
            {0,38,28},
            {19,38,33},
            {0,255,255},
            {127,255,255},
            {0,165,165},
            {82,165,165},
            {0,127,127},
            {63,127,127},
            {0,76,76},
            {38,76,76},
            {0,38,38},
            {19,38,38},
            {0,191,255},
            {127,223,255},
            {0,124,165},
            {82,145,165},
            {0,95,127},
            {63,111,127},
            {0,57,76},
            {38,66,76},
            {0,28,38},
            {19,33,38},
            {0,127,255},
            {127,191,255},
            {0,82,165},
            {82,124,165},
            {0,63,127},
            {63,95,127},
            {0,38,76},
            {38,57,76},
            {0,19,38},
            {19,28,38},
            {0,63,255},
            {127,159,255},
            {0,41,165},
            {82,103,165},
            {0,31,127},
            {63,79,127},
            {0,19,76},
            {38,47,76},
            {0,9,38},
            {19,23,38},
            {0,0,255},
            {127,127,255},
            {0,0,165},
            {82,82,165},
            {0,0,127},
            {63,63,127},
            {0,0,76},
            {38,38,76},
            {0,0,38},
            {19,19,38},
            {63,0,255},
            {159,127,255},
            {41,0,165},
            {103,82,165},
            {31,0,127},
            {79,63,127},
            {19,0,76},
            {47,38,76},
            {9,0,38},
            {23,19,38},
            {127,0,255},
            {191,127,255},
            {82,0,165},
            {124,82,165},
            {63,0,127},
            {95,63,127},
            {38,0,76},
            {57,38,76},
            {19,0,38},
            {28,19,38},
            {191,0,255},
            {223,127,255},
            {124,0,165},
            {145,82,165},
            {95,0,127},
            {111,63,127},
            {57,0,76},
            {66,38,76},
            {28,0,38},
            {33,19,38},
            {255,0,255},
            {255,127,255},
            {165,0,165},
            {165,82,165},
            {127,0,127},
            {127,63,127},
            {76,0,76},
            {76,38,76},
            {38,0,38},
            {38,19,38},
            {255,0,191},
            {255,127,223},
            {165,0,124},
            {165,82,145},
            {127,0,95},
            {127,63,111},
            {76,0,57},
            {76,38,66},
            {38,0,28},
            {38,19,33},
            {255,0,127},
            {255,127,191},
            {165,0,82},
            {165,82,124},
            {127,0,63},
            {127,63,95},
            {76,0,38},
            {76,38,57},
            {38,0,19},
            {38,19,28},
            {255,0,63},
            {255,127,159},
            {165,0,41},
            {165,82,103},
            {127,0,31},
            {127,63,79},
            {76,0,19},
            {76,38,47},
            {38,0,9},
            {38,19,23},
            {84,84,84},
            {118,118,118},
            {160,160,160},
            {192,192,192},
            {224,224,224},
            {0,0,0}
        };


}
