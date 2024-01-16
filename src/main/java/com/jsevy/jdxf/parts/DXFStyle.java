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

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Vector;




/**
 * Class representing a text style for use in a DXF document. Encapsulates text parameters (font, style), 
 * and associates an arbitrary "name" for use in text entities which use that style. Unfortunately, some programs (e.g. LibreCAD)
 * use the "name" - group code 2 - as the name of the font file, skipping the style table. Thus we use the font file
 * name (group code 3) as the "name" (group code 2) to accommodate those programs skipping the style table. Grrr....
 * @author jsevy
 *
 */
public class DXFStyle extends DXFTableRecord
{
    private String name;
    private String dxfFontName;
    private Font javaFont;
        
    
    
    /**
     * Create a style object corresponding to the supplied Java typeface object.
     * 
     * @param font	Font whose parameters (size, style, typeface) should be represented in the style object
     */
    public DXFStyle(Font font)
    {
        this.javaFont = font;
        this.dxfFontName = getDXFFontName(font);
        
        // generate a name - just use the font name. This isn't required by the standard, but we do it to accommodate
        // cheaters like LibreCAD that skip the style table
        this.name = this.dxfFontName;
        
    }
    
    
    /**
     * Implementation of DXFObject interface method; creates DXF text representing the text style.
     */
    public String toDXFString()
    {
        String returnString = new String();
        
        returnString += "0\nSTYLE\n";
        
        // print out handle and superclass marker(s)
        returnString += super.toDXFString();
        
        // print out subclass marker
        returnString += "100\nAcDbTextStyleTableRecord\n";
        
        returnString += "2\n" + name + "\n";
        returnString += "3\n" + dxfFontName + "\n";
        
        // no flags set
        returnString += "70\n0\n";
        
        return returnString;
    }
    
    
    /**
     * Equals method for use in determining if a Style is already present.
     * @param other		Another style representing a font
     * @return			True if the associated Android fonts used to create the styles are equal
     */
    public boolean equals(DXFStyle other)
    {
        if (this.javaFont.equals(other.javaFont))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    
    /**
     * Get the name for this style object, for use within a DXFText entity.
     * @return  identifier for this Style
     */
    public String getStyleName()
    {
        return name;
    }
    
    
    
    /**
     * See if the font is a standard Java font, and if so, return a corresponding standard DXF font name. If not, just return the font's name.
     *   
     * @param javaFont	Java typeface
     * @return			Corresponding DXF font style string
     */
    private static String getDXFFontName(Font javaFont)
    {
    	// see if it's a standard Java font, and if so, return a corresponding standard DXF font name
    	if (isStandardJavaFont(javaFont))
    	    return javaToDXFFontMap(javaFont);
    	else
    	    return javaFont.getFontName();
    	
    }
    
    
    private static boolean isStandardJavaFont(Font javaFont)
    {
        if (javaFont.getFamily().equals(Font.SERIF) || javaFont.getFamily().equals(Font.MONOSPACED) || javaFont.getFamily().equals(Font.SANS_SERIF))
            return true;
        else
            return false;
    }
    
    /**
     * Determine a standard DXF font corresponding to the supplied Java typeface if it's one of the standard Java fonts.
     * 
     * Standard DXF type style strings:
     *    STANDARD    
     *    ARIAL   
     *    ARIAL_BOLD
     *    ARIAL_ITALIC
     *    ARIAL_BOLD_ITALIC
     *    ARIAL_BLACK
     *    ISOCPEUR
     *    ISOCPEUR_ITALIC
     *    TIMES
     *    TIMES_BOLD
     *    TIMES_ITALIC
     *    TIMES_BOLD_ITALIC
     *   
     * @param javaFont  Java typeface
     * @return          Corresponding DXF font style string
     */
    private static String javaToDXFFontMap(Font javaFont)
    {
        String returnString;
        
        // figure out which typeface to use
        if (javaFont.getFamily().equals(Font.SERIF))
        {
            //returnString = "times";
            returnString = "romanc";
        }
        else if (javaFont.getFamily().equals(Font.MONOSPACED))
        {
            returnString = "isocpeur";
        }
        else
        {
            returnString = "arial";
        }
        
        // add bold/italic modifier
        int fontStyle = javaFont.getStyle();
        switch (fontStyle)
        {
            case Font.BOLD:
            {
                returnString += "_bold";
                break;
            }
            case Font.ITALIC:
            {
                returnString += "_italic";
                break;
            }
            case Font.BOLD+Font.ITALIC:
            {
                returnString += "_bold_italic";
                break;
            }
            default:
            {
                break;
            }
        }
        
        return returnString;
    }
    
}