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

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Interface for all DXF database objects, including graphical entities. Each
 * subclass will define a method to generate its corresponding DXF text for use
 * in a DXF file, including the database subclass marker, and should call this
 * superclass method to insert the database handle.
 *
 * @author jsevy
 *
 */
public class DXFDatabaseObject implements DXFObject {

    private static final ThreadLocal<AtomicInteger> HANDLE_COUNT = new ThreadLocal<AtomicInteger>() {
        @Override
        protected AtomicInteger initialValue() {
            return new AtomicInteger(1);
        }
    };

    protected int handle;

    public DXFDatabaseObject() {
        // assign handle and increment handle count so will be unique
        handle = HANDLE_COUNT.get().getAndIncrement();
    }

    /**
     * Implement DXFObject method; just print out group code and value of
     * handle.
     *
     * @return group code and value of handle.
     */
    @Override
    public String toDXFString() {
        // print out handle
        String result = "5\n" + Integer.toHexString(handle) + "\n";

        return result;
    }

    /**
     * Return handle for this object
     *
     * @return Handle for object
     */
    public int getHandle() {
        return handle;
    }

    /**
     * Return current handle count so can know what ones have been used
     *
     * @return Current handle count
     */
    public static int getHandleCount() {
        return HANDLE_COUNT.get().get();
    }

    /**
     * Reset the count
     */
    public static void reset() {
        HANDLE_COUNT.get().set(1);
    }

}
