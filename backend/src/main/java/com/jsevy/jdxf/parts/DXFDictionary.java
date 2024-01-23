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
 * Class representing dictionary for use in OBJECTS table. Dictionaries contain other stuff, so are containers
 * @author jsevy
 *
 */
public class DXFDictionary extends Vector<DXFDictionary> implements DXFObject
{
    private static final long serialVersionUID = 1L;
    
    public String name;
    
    // define a DXFDatabaseObject so we have a handle; no multiple inheritance in Java...
    protected DXFDatabaseObject myDXFDatabaseObject;
    
    private int ownerHandle;
    
    
    /**
     * Create a dictionary object with specified name.
     * 
     * @param name	        Name of table record
     * @param ownerHandle   Handle of owner of this dictionary object
     */
    public DXFDictionary(String name, int ownerHandle)
    {
        this.name = name;
        this.ownerHandle = ownerHandle;
        
        // get our DXFDatabaseObject member so we have a handle
        myDXFDatabaseObject = new DXFDatabaseObject();
    }
    
    
    public String getName()
    {
        return this.name;
    }
    
    
    public int getHandle()
    {
        return this.myDXFDatabaseObject.getHandle();
    }
    
    
    /**
     * Implementation of DXFObject interface method; creates DXF text representing the object.
     */
    public String toDXFString()
    {
        String returnString = new String();
        
        returnString += "0\nDICTIONARY\n";
        
        // print out handle
        returnString += myDXFDatabaseObject.toDXFString();
        
        // print out owner handle
        returnString += "330\n" + Integer.toHexString(ownerHandle) + "\n";
        
        // print out subclass marker
        returnString += "100\nAcDbDictionary\n";
        
        // duplicate record cloning flag - keep existing
        returnString += "281\n1\n";
        
        // print out list of names and handles of child entries
        for (int i = 0; i < this.size(); i++)
        {
            returnString += "3\n" + this.elementAt(i).getName() + "\n";
            returnString += "350\n" + Integer.toHexString(this.elementAt(i).getHandle()) + "\n";
        }
        
        // print out all child dictionaries
        for (int i = 0; i < this.size(); i++)
        {
            returnString += this.elementAt(i).toDXFString();
        }
        
        return returnString;
    }
    
}