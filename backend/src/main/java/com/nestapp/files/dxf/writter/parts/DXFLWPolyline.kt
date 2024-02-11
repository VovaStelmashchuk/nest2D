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
package com.nestapp.files.dxf.writter.parts

import com.nestapp.files.dxf.common.LSegment

/**
 * Class representing a set of line segments defining a (possibly closed) polygon.
 * Create a set of line segments that connects the specified points, including a segment from the last
 * to the first if closed is indicated.
 * @param numVertices The number of vertices specified in the vertex list
 * @param vertices    The vertices
 * @param closed      If true, adds a segment between the last and first points
 */
class DXFLWPolyline(
    private val segments: List<LSegment>,
    private val closed: Boolean,
) : DXFEntity() {

    override fun toDXFString(): String {
        var result = "0\nLWPOLYLINE\n"

        // print out handle and superclass marker(s)
        result += super.toDXFString()

        // print out subclass marker
        result += "100\nAcDbPolyline\n"

        // include number of vertices
        result += "90\n${segments.size}\n"

        // indicate if closed
        result += if (closed) {
            "70\n1\n"
        } else {
            "70\n0\n"
        }

        // include list of vertices
        for (segment in segments) {
            result += """
                10
                ${setPrecision(segment.dx)}

                """.trimIndent()
            result += """
                20
                ${setPrecision(segment.dy)}

                """.trimIndent()

            result += """
            42
            ${setPrecision(segment.bulge)}

            """.trimIndent()
        }

        return result
    }
}
