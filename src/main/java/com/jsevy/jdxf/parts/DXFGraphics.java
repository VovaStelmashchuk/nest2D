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


import com.jsevy.jdxf.DXFDocument;

import java.util.*;
import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;



/**
 * The DXFGraphics class is the main class of the DXFGraphics library. It extends the standard
 * Java Graphics and Graphics2D classes, reimplementing their methods to generate DXF objects
 * which can then generate DXF output for use with CAD programs. A DXFGraphics object is always 
 * associated with a DXFDocument instance, and is obtained from the DXFDocument through a call to
 * the getGraphics() method. The typical workflow is as follows:
 * 
 * <pre>
 * 
 * // Create a DXF document and get its associated DXFGraphics instance
 * DXFDocument dxfDocument = new DXFDocument("Example"); 
 * DXFGraphics dxfGraphics = dxfDocument.getGraphics(); 
 * 
 * // Do drawing commands as on any other Graphics. If you have a paint(Graphics) method, 
 * // you can just use it with the DXFGraphics instance since it's a subclass of Graphics. 
 * graphics.setColor(Color.RED);  
 * graphics.setStroke(new BasicStroke(3));
 * graphics.drawLine(0, 0, 1000, 500); 
 * graphics.drawRect(1000, 500, 150, 150); 
 * graphics.drawRoundRect(20, 200, 130, 100, 20, 10); 
 * 
 * // Get the DXF output as a string - it's just text - and  save  in a file for use with a CAD package 
 * String stringOutput = dxfDocument.toDXFString(); 
 * String filePath = "path/to/file.dxf"; 
 * FileWriter fileWriter = new FileWriter(filePath); 
 * fileWriter.write(dxfText); 
 * fileWriter.flush(); 
 * fileWriter.close();
 * 
 * </pre>
 * 
 * @author jsevy
 */
public class DXFGraphics extends Graphics2D
{

    private DXFDocument dxfDocument;
    
    // class access; used to indicate if circles and circular arcs should be generated,
    // or just more general elliptical arcs
    public boolean useCircles;
    
    // class access; used to indicate if zero-length lines should be represented as points in DXF output
    public boolean usePoints;
    
    private Color color;
    private Color backColor;
    private PaintMode paintMode;
    private Font font;
    private BasicStroke stroke;  // pen parameters
    
    private AffineTransform javaTransformMatrix;
    private AffineTransform javaToDXFGraphicsMatrix;
    private AffineTransform graphicsMatrix;
    
    private enum PaintMode
    {
        PAINT_MODE,
        XOR_MODE
    };
    
    
    /**
     * Constructs a new DXFGraphics object. Since a DXFGraphics object is
     * always associated with a DXFDocument instance, applications
     * should not call this constructor directly, but rather use the getGraphics() method 
     * of a DXFDocument.
     * 
     * @param   dxfDocument     The associated DXFDocument instance
     */
    public DXFGraphics(DXFDocument dxfDocument) 
    {
        this.dxfDocument = dxfDocument;
        
        useCircles = true;
        usePoints = true;
        
        color = Color.BLACK;
        backColor = Color.WHITE;
        paintMode = PaintMode.PAINT_MODE;
        font = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        stroke = new BasicStroke();
        
        // user-defined Java transform - initialize to identity matrix
        javaTransformMatrix = new AffineTransform();
        
        // transform from Java Space to DXF Space; y axis flipped
        javaToDXFGraphicsMatrix = new AffineTransform();
        javaToDXFGraphicsMatrix.scale(1f, -1f);
        
        // combine Java and Java2DXF matrices for overall transform (graphicsMatrix)
        setMatrix();
        
    }

    /**
     * Creates a new DXFGraphics object that is a copy of this DXFGraphics object.
     * Since a DXFGraphics object is always associated with a DXFDocument instance, applications
     * should not call this constructor directly, but rather use the getGraphics() method 
     * of a DXFDocument.
     * @return     a new DXFGraphics context that is a copy of
     *                       this graphics context.
     */
    public DXFGraphics create()
    {
        DXFGraphics newGraphics = new DXFGraphics(this.dxfDocument);
        
        newGraphics.color = this.color;
        newGraphics.backColor = this.backColor;
        newGraphics.paintMode = this.paintMode;
        newGraphics.font = this.font;
        newGraphics.stroke = this.stroke;
        
        newGraphics.javaTransformMatrix = new AffineTransform(this.javaTransformMatrix);
        newGraphics.javaToDXFGraphicsMatrix = new AffineTransform(this.javaToDXFGraphicsMatrix);
        newGraphics.graphicsMatrix = new AffineTransform(this.graphicsMatrix);
        
        return newGraphics;
    }

    /**
     * Creates a new DXFGraphics object based on this DXFGraphics object, but with a new translation.
     * Since a DXFGraphics object is always associated with a DXFDocument instance, applications
     * should not call this constructor directly, but rather use the getGraphics() method 
     * of a DXFDocument.
     */
    public DXFGraphics create(int x, int y, int width, int height) {
        DXFGraphics graphics = create();
        if (graphics == null) return null;
        graphics.translate(x, y);
        return graphics;
    }
    
    
    
    /**
     * Create composite matrix from Java transform and Java-to-DXF transform
     */
    private void setMatrix()
    {
        // set graphics matrix
        graphicsMatrix = new AffineTransform(javaToDXFGraphicsMatrix);
        graphicsMatrix.concatenate(javaTransformMatrix);
    }
    

    

    /**
     * Gets this graphic's foreground color.
     * @return    the foreground color
     */
    public Color getColor()
    {
        return color;
    }

    /**
     * Sets this graphics foreground color to the specified
     * color. All subsequent graphics operations using this graphics
     * objects use this new color.
     * @param     color   the new rendering color.
     */
    public void setColor(Color color)
    {
        this.color = color;
    }

    /**
     * Sets the paint mode of this graphics context to overwrite the
     * destination with this graphics context's current color. Note that
     * this is the only mode supported by DXFGraphics.
     */
    public void setPaintMode()
    {
        paintMode = PaintMode.PAINT_MODE;
    }

    /**
     * Currently does nothing, as only PAINT_MODE is supported
     */
    public void setXORMode(Color xorColor)
    {
        paintMode = PaintMode.XOR_MODE;
    }

    /**
     * Gets the current font.
     * @return    this graphic's current font.
     */
    public Font getFont()
    {
        return font;
    }

    /**
     * Sets this graphics context's font to the specified font.
     * All subsequent text operations using this graphics context
     * use this font. A null argument is silently ignored.
     * @param  font   the new font.
    */
    public void setFont(Font font)
    {
        if (font != null)
            this.font = font;
    }

    /**
     * Gets the font metrics of the current font.
     * @return    the font metrics of this graphics
     *                    context's current font.
     */
    public FontMetrics getFontMetrics() 
    {
        return getFontMetrics(getFont());
    }

    /**
     * Gets the font metrics for the specified font.
     * @return    the font metrics for the specified font.
     * @param     f the specified font
     */
    public FontMetrics getFontMetrics(Font f)
    {
        // get the system FontMetrics for the current Font; will not match precisely the DXF font
        Canvas c = new Canvas();
        return c.getFontMetrics(f);
    }


    /**
     * Returns null, as clipping not supported.
     * @return      null
     */
    public Rectangle getClipBounds()
    {
        return null;
    }

    /**
     * Does nothing, as clipping not supported
     */
    public void clipRect(int x, int y, int width, int height)
    {
        // do nothing
    }

    /**
     * Does nothing, as clipping not supported
     */
    public void setClip(int x, int y, int width, int height)
    {
        // do nothing
    }

    /**
     * Returns null, as clipping not supported
     * @return      null
     */
    public Shape getClip()
    {
        return null;
    }

    /**
     * Does nothing, as clipping not supported
     */
    public void setClip(Shape clip)
    {
        // do nothing
    }

    /**
     * Throws UnsupportedOperationException, as copyArea not supported.
     */
    public void copyArea(int x, int y, int width, int height, int dx, int dy)
    {
        throw new UnsupportedOperationException("copyArea not supported");
    }
    
    
    /**
     * Draws a point, using the current color, at location
     * <code>(x,&nbsp;y)</code>
     * in this graphics context's coordinate system.
     * @param   x  the point's <i>x</i> coordinate.
     * @param   y  the point's <i>y</i> coordinate.
     */
    public void drawPoint(int x, int y)
    {
        this.drawPoint((double)x, (double)y);
    }
    
    
    /**
     * Draws a point, using the current color, at location
     * <code>(x,&nbsp;y)</code>
     * in this graphics context's coordinate system.
     * @param   x  the point's <i>x</i> coordinate.
     * @param   y  the point's <i>y</i> coordinate.
     */
    public void drawPoint(double x, double y)
    {
        // transform the coordinates using the current graphics transform matrix
        double[] points = new double[]{x, y};
        graphicsMatrix.transform(points, 0, points, 0, 1);
        
        dxfDocument.addEntity(new DXFPoint(new RealPoint(points[0], points[1], 0), this));
    }
    

    /**
     * Draws a line, using the current color, between the points
     * <code>(x1,&nbsp;y1)</code> and <code>(x2,&nbsp;y2)</code>
     * in this graphics context's coordinate system.
     * @param   x1  the first point's <i>x</i> coordinate.
     * @param   y1  the first point's <i>y</i> coordinate.
     * @param   x2  the second point's <i>x</i> coordinate.
     * @param   y2  the second point's <i>y</i> coordinate.
     */
    public void drawLine(int x1, int y1, int x2, int y2)
    {
        this.drawLine((double)x1, (double)y1, (double)x2, (double)y2);
    }
    
    /**
     * Draws a line, using the current color, between the points
     * <code>(x1,&nbsp;y1)</code> and <code>(x2,&nbsp;y2)</code>
     * in this graphics context's coordinate system.
     * @param   x1  the first point's <i>x</i> coordinate.
     * @param   y1  the first point's <i>y</i> coordinate.
     * @param   x2  the second point's <i>x</i> coordinate.
     * @param   y2  the second point's <i>y</i> coordinate.
     */
    public void drawLine(double x1, double y1, double x2, double y2)
    {
        // see if usePoints flag is true; is so, generate a point if line is zero length
        if((usePoints == true) && (x1 == x2) && (y1 == y2))
        {
            // zero-length line; generate a DXF point
            drawPoint(x1, y1);
        }
        else
        {
            DXFLine line = createDXFLine(x1, y1, x2, y2);
            dxfDocument.addEntity(line);
        }

    }
    
    /**
     * Creates a DXFLine object corresponding to the line between
     * the supplied endpoints.
     * @param   x1  the first point's <i>x</i> coordinate.
     * @param   y1  the first point's <i>y</i> coordinate.
     * @param   x2  the second point's <i>x</i> coordinate.
     * @param   y2  the second point's <i>y</i> coordinate.
     * @return  corresponding DXFLine Object
     */
    private DXFLine createDXFLine(double x1, double y1, double x2, double y2)
    {
        // transform the coordinates using the current graphics transform matrix
        double[] points = new double[]{x1, y1, x2, y2};
        graphicsMatrix.transform(points, 0, points, 0, 2);//.mapPoints(pts);
        
        //System.out.println("Line: start point: (" + points[0] + ", " + points[1] + ")");
        //System.out.println("        end point: (" + points[2] + ", " + points[3] + ")");
        
        return new DXFLine(new RealPoint(points[0], points[1], 0), new RealPoint(points[2], points[3], 0), this);
    }

    /**
     * Fills the specified rectangle.
     * The left and right edges of the rectangle are at
     * <code>x</code> and <code>x&nbsp;+&nbsp;width&nbsp;-&nbsp;1</code>.
     * The top and bottom edges are at
     * <code>y</code> and <code>y&nbsp;+&nbsp;height&nbsp;-&nbsp;1</code>.
     * The rectangle is filled using the graphics context's current color.
     * @param         x   the <i>x</i> coordinate
     *                         of the rectangle to be filled.
     * @param         y   the <i>y</i> coordinate
     *                         of the rectangle to be filled.
     * @param         width   the width of the rectangle to be filled.
     * @param         height   the height of the rectangle to be filled.
     */
    public void fillRect(int x, int y, int width, int height)
    {
        this.fillRect((double)x, (double)y, (double)width, (double)height);
    }
    
    
    /**
     * Fills the specified rectangle.
     * The left and right edges of the rectangle are at
     * <code>x</code> and <code>x&nbsp;+&nbsp;width&nbsp;-&nbsp;1</code>.
     * The top and bottom edges are at
     * <code>y</code> and <code>y&nbsp;+&nbsp;height&nbsp;-&nbsp;1</code>.
     * The rectangle is filled using the graphics context's current color.
     * @param         x   the <i>x</i> coordinate
     *                         of the rectangle to be filled.
     * @param         y   the <i>y</i> coordinate
     *                         of the rectangle to be filled.
     * @param         width   the width of the rectangle to be filled.
     * @param         height   the height of the rectangle to be filled.
     */
    public void fillRect(double x, double y, double width, double height)
    {
        // Create Shape and use general Shape routine
        Rectangle2D rect = new Rectangle2D.Double(x, y, width, height);
        fill(rect);
    }

    /**
     * Draws the outline of the specified rectangle.
     * The left and right edges of the rectangle are at
     * <code>x</code> and <code>x&nbsp;+&nbsp;width</code>.
     * The top and bottom edges are at
     * <code>y</code> and <code>y&nbsp;+&nbsp;height</code>.
     * The rectangle is drawn using the graphics context's current color.
     * @param         x   the <i>x</i> coordinate
     *                         of the rectangle to be drawn.
     * @param         y   the <i>y</i> coordinate
     *                         of the rectangle to be drawn.
     * @param         width   the width of the rectangle to be drawn.
     * @param         height   the height of the rectangle to be drawn.
     */
    public void drawRect(int x, int y, int width, int height)
    {
        this.drawRect((double)x, (double)y, (double)width, (double)height);
    }
    
    
    /**
     * Draws the outline of the specified rectangle.
     * The left and right edges of the rectangle are at
     * <code>x</code> and <code>x&nbsp;+&nbsp;width</code>.
     * The top and bottom edges are at
     * <code>y</code> and <code>y&nbsp;+&nbsp;height</code>.
     * The rectangle is drawn using the graphics context's current color.
     * @param         x   the <i>x</i> coordinate
     *                         of the rectangle to be drawn.
     * @param         y   the <i>y</i> coordinate
     *                         of the rectangle to be drawn.
     * @param         width   the width of the rectangle to be drawn.
     * @param         height   the height of the rectangle to be drawn.
     */
    public void drawRect(double x, double y, double width, double height)
    {
        // transform the coordinates using the current graphics transform matrix
        double[] pts = {x, y, x+width, y, x+width, y+height, x, y+height};
        graphicsMatrix.transform(pts, 0, pts, 0, 4);
        
        // create as lightweight polyline
        Vector<RealPoint> vertices = new Vector<RealPoint>();
        vertices.add(new RealPoint(pts[0], pts[1], 0));
        vertices.add(new RealPoint(pts[2], pts[3], 0));
        vertices.add(new RealPoint(pts[4], pts[5], 0));
        vertices.add(new RealPoint(pts[6], pts[7], 0));
        
        DXFLWPolyline polyline = new DXFLWPolyline(4 /*vertices*/, vertices, true /*closed*/, this);
        dxfDocument.addEntity(polyline);
    }
    
    
    /* Original version...
    public void drawRect(int x, int y, int width, int height) {
    
       if ((width < 0) || (height < 0)) {
           return;
       }

       if (height == 0 || width == 0) {
           drawLine(x, y, x + width, y + height);
       } else {
           drawLine(x, y, x + width - 1, y);
           drawLine(x + width, y, x + width, y + height - 1);
           drawLine(x + width, y + height, x + 1, y + height);
           drawLine(x, y + height, x, y + 1);
       }
   }
   */

    /**
     * Clears the specified rectangle by filling it with the background
     * color.
     * @param       x the <i>x</i> coordinate of the rectangle to clear.
     * @param       y the <i>y</i> coordinate of the rectangle to clear.
     * @param       width the width of the rectangle to clear.
     * @param       height the height of the rectangle to clear.
     */
    public void clearRect(int x, int y, int width, int height)
    {
        this.clearRect((double)x, (double)y, (double)width, (double)height);
    }
    
    
    /**
     * Clears the specified rectangle by filling it with the background
     * color.
     * @param       x the <i>x</i> coordinate of the rectangle to clear.
     * @param       y the <i>y</i> coordinate of the rectangle to clear.
     * @param       width the width of the rectangle to clear.
     * @param       height the height of the rectangle to clear.
     */
    public void clearRect(double x, double y, double width, double height)
    {
        // Create Shape and use general Shape routine
        Rectangle2D rect = new Rectangle2D.Double(x, y, width, height);
        clear(rect);
    }

    /**
     * Draws an outlined round-cornered rectangle using this graphics
     * context's current color. The left and right edges of the rectangle
     * are at <code>x</code> and <code>x&nbsp;+&nbsp;width</code>,
     * respectively. The top and bottom edges of the rectangle are at
     * <code>y</code> and <code>y&nbsp;+&nbsp;height</code>.
     * @param      x the <i>x</i> coordinate of the rectangle to be drawn.
     * @param      y the <i>y</i> coordinate of the rectangle to be drawn.
     * @param      width the width of the rectangle to be drawn.
     * @param      height the height of the rectangle to be drawn.
     * @param      arcWidth the horizontal diameter of the arc
     *                    at the four corners.
     * @param      arcHeight the vertical diameter of the arc
     *                    at the four corners.
     */
    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight)
    {
        this.drawRoundRect((double)x, (double)y, (double)width, (double)height, (double)arcWidth, (double)arcHeight);        
    }
    
    
    /**
     * Draws an outlined round-cornered rectangle using this graphics
     * context's current color. The left and right edges of the rectangle
     * are at <code>x</code> and <code>x&nbsp;+&nbsp;width</code>,
     * respectively. The top and bottom edges of the rectangle are at
     * <code>y</code> and <code>y&nbsp;+&nbsp;height</code>.
     * @param      x the <i>x</i> coordinate of the rectangle to be drawn.
     * @param      y the <i>y</i> coordinate of the rectangle to be drawn.
     * @param      width the width of the rectangle to be drawn.
     * @param      height the height of the rectangle to be drawn.
     * @param      arcWidth the horizontal diameter of the arc
     *                    at the four corners.
     * @param      arcHeight the vertical diameter of the arc
     *                    at the four corners.
     */
    public void drawRoundRect(double x, double y, double width, double height, double arcWidth, double arcHeight)
    {
        
        double left = x;
        double right = x + width;
        double top = y;
        double bottom = y + height;
        
        double rx = arcWidth/2;
        double ry = arcHeight/2;
        
        // ick: draw ovals in corners connected by lines, clockwise from top left
        // note that the subroutines will take care of transforming these as needed
        
        // top line and top-right oval segment
        drawLine(left + rx, top, right - rx, top);
        drawArc(right - 2 * rx, top, arcWidth, arcHeight, 90, -90);
        
        // right line and bottom-right oval segment
        drawLine(right, top+ry, right, bottom - ry);
        drawArc(right - 2 * rx, bottom - 2 * ry, arcWidth, arcHeight, 0, -90);
        
        // bottom line and bottom-left oval segment
        drawLine(right-rx, bottom, left + rx, bottom);
        drawArc(left, bottom - 2 * ry, arcWidth, arcHeight, 270, -90);
        
        // left line and top-left arc
        drawLine(left, bottom-ry,left, top + ry);
        drawArc(left, top, arcWidth, arcHeight, 180, -90);
        
    }
                                       

    /**
     * Fills the specified rounded corner rectangle with the current color.
     * The left and right edges of the rectangle
     * are at <code>x</code> and <code>x&nbsp;+&nbsp;width&nbsp;-&nbsp;1</code>,
     * respectively. The top and bottom edges of the rectangle are at
     * <code>y</code> and <code>y&nbsp;+&nbsp;height&nbsp;-&nbsp;1</code>.
     * @param       x the <i>x</i> coordinate of the rectangle to be filled.
     * @param       y the <i>y</i> coordinate of the rectangle to be filled.
     * @param       width the width of the rectangle to be filled.
     * @param       height the height of the rectangle to be filled.
     * @param       arcWidth the horizontal diameter
     *                     of the arc at the four corners.
     * @param       arcHeight the vertical diameter
     *                     of the arc at the four corners.
     */
    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight)
    {
        this.fillRoundRect((double)x, (double)y, (double)width, (double)height, (double)arcWidth, (double)arcHeight);
    }
    
    
    /**
     * Fills the specified rounded corner rectangle with the current color.
     * The left and right edges of the rectangle
     * are at <code>x</code> and <code>x&nbsp;+&nbsp;width&nbsp;-&nbsp;1</code>,
     * respectively. The top and bottom edges of the rectangle are at
     * <code>y</code> and <code>y&nbsp;+&nbsp;height&nbsp;-&nbsp;1</code>.
     * @param       x the <i>x</i> coordinate of the rectangle to be filled.
     * @param       y the <i>y</i> coordinate of the rectangle to be filled.
     * @param       width the width of the rectangle to be filled.
     * @param       height the height of the rectangle to be filled.
     * @param       arcWidth the horizontal diameter
     *                     of the arc at the four corners.
     * @param       arcHeight the vertical diameter
     *                     of the arc at the four corners.
     */
    public void fillRoundRect(double x, double y, double width, double height, double arcWidth, double arcHeight)
    {
        // Create Shape and use general Shape routine
        //RoundRectangle2D rect = new RoundRectangle2D.Double(x, y, width, height, arcWidth, arcHeight);
        //fill(rect);
        
        // create boundary for hatch
        Vector<DXFEntity> boundary = new Vector<DXFEntity>();
        
        double left = x;
        double right = x + width;
        double top = y;
        double bottom = y + height;
        
        double rx = arcWidth/2;
        double ry = arcHeight/2;
        
        /*
         * So it seems that many CAD packages aren't happy with clockwise arcs; all seem to react differently,
         * and none as expected. Thus sticking to counterclockwise boundary spec for  hatches with arcs...
         */
        /*
         * 
        // draw ovals in corners connected by lines, clockwise from top left
        // note that the subroutines will take care of transforming these as needed
        
        // top line and top-right oval segment
        boundary.add(createDXFLine(left + rx, top, right - rx, top));
        boundary.add(createDXFEllipticalArc(right - 2 * rx, top, arcWidth, arcHeight, 90, -90));
        
        // right line and bottom-right oval segment
        boundary.add(createDXFLine(right, top+ry, right, bottom - ry));
        boundary.add(createDXFEllipticalArc(right - 2 * rx, bottom - 2 * ry, arcWidth, arcHeight, 0, -90));
        
        // bottom line and bottom-left oval segment
        boundary.add(createDXFLine(right-rx, bottom, left + rx, bottom));
        boundary.add(createDXFEllipticalArc(left, bottom - 2 * ry, arcWidth, arcHeight, -90, -90));
        
        // left line and top-left arc
        boundary.add(createDXFLine(left, bottom-ry,left, top + ry));
        boundary.add(createDXFEllipticalArc(left, top, arcWidth, arcHeight, -180, -90));
        */
        
        
        // create counterclockwise boundary curve...
        // top right arc
        boundary.add(createDXFEllipticalArc(right - 2 * rx, top, arcWidth, arcHeight, 0, 90));
        
        // top line 
        boundary.add(createDXFLine(right - rx, top, left + rx, top));
        
        // top left arc
        boundary.add(createDXFEllipticalArc(left, top, arcWidth, arcHeight, 90, 90));
        
        // left line
        boundary.add(createDXFLine(left, top + ry, left, bottom-ry));
        
        // bottom-left arc
        boundary.add(createDXFEllipticalArc(left, bottom - 2 * ry, arcWidth, arcHeight, 180, 90));
        
        // bottom line
        boundary.add(createDXFLine(left + rx, bottom, right-rx, bottom));
        
        // bottom-right arc
        boundary.add(createDXFEllipticalArc(right - 2 * rx, bottom - 2 * ry, arcWidth, arcHeight, 270, 90));
        
        // right line
        boundary.add(createDXFLine(right, bottom - ry, right, top+ry));
        
        
        // add Hatch with the above boundary
        Vector<Vector<DXFEntity>> boundaries = new Vector<Vector<DXFEntity>>();
        boundaries.add(boundary);
        DXFHatch hatch = new DXFHatch(boundaries, color);
        dxfDocument.addEntity(hatch);
    }

    /**
     * Draws a 3-D highlighted outline of the specified rectangle.
     * The edges of the rectangle are highlighted so that they
     * appear to be beveled and lit from the upper left corner.
     * <p>
     * The colors used for the highlighting effect are determined
     * based on the current color.
     * The resulting rectangle covers an area that is
     * <code>width&nbsp;+&nbsp;1</code> pixels wide
     * by <code>height&nbsp;+&nbsp;1</code> pixels tall.
     * @param       x the <i>x</i> coordinate of the rectangle to be drawn.
     * @param       y the <i>y</i> coordinate of the rectangle to be drawn.
     * @param       width the width of the rectangle to be drawn.
     * @param       height the height of the rectangle to be drawn.
     * @param       raised a boolean that determines whether the rectangle
     *                      appears to be raised above the surface
     *                      or sunk into the surface.
     */
    public void draw3DRect(int x, int y, int width, int height,
                           boolean raised) {
        super.draw3DRect(x, y, width, height, raised);
    }

    /**
     * Paints a 3-D highlighted rectangle filled with the current color.
     * The edges of the rectangle will be highlighted so that it appears
     * as if the edges were beveled and lit from the upper left corner.
     * The colors used for the highlighting effect will be determined from
     * the current color.
     * @param       x the <i>x</i> coordinate of the rectangle to be filled.
     * @param       y the <i>y</i> coordinate of the rectangle to be filled.
     * @param       width the width of the rectangle to be filled.
     * @param       height the height of the rectangle to be filled.
     * @param       raised a boolean value that determines whether the
     *                      rectangle appears to be raised above the surface
     *                      or etched into the surface.
     * @see         java.awt.Graphics#draw3DRect
     */
    public void fill3DRect(int x, int y, int width, int height,
                           boolean raised) {
        super.fill3DRect(x, y, width, height, raised);
    }

    /**
     * Draws the outline of an oval.
     * The result is a circle or ellipse that fits within the
     * rectangle specified by the <code>x</code>, <code>y</code>,
     * <code>width</code>, and <code>height</code> arguments.
     * @param       x the <i>x</i> coordinate of the upper left
     *                     corner of the oval to be drawn.
     * @param       y the <i>y</i> coordinate of the upper left
     *                     corner of the oval to be drawn.
     * @param       width the width of the oval to be drawn.
     * @param       height the height of the oval to be drawn.
     */
    public void drawOval (int x, int y, int width, int height)
    {
        this.drawOval((double)x, (double)y, (double)width, (double)height);
    }
    
    
    /**
     * Draws the outline of an oval.
     * The result is a circle or ellipse that fits within the
     * rectangle specified by the <code>x</code>, <code>y</code>,
     * <code>width</code>, and <code>height</code> arguments.
     * @param       x the <i>x</i> coordinate of the upper left
     *                     corner of the oval to be drawn.
     * @param       y the <i>y</i> coordinate of the upper left
     *                     corner of the oval to be drawn.
     * @param       width the width of the oval to be drawn.
     * @param       height the height of the oval to be drawn.
     */
    public void drawOval (double x, double y, double width, double height)
    {
        // see if usePoints flag is true; is so, generate a point if radius is 0
        if((usePoints == true) && (width == 0) && (height == 0))
        {
            // zero-radius circle; generate a DXF point
            drawPoint(x, y);
        }
        else
        {
            drawArc(x, y, width, height, 0, 360);
        }
         
    }

    
    /**
     * Fills an oval bounded by the specified rectangle with the
     * current color.
     * @param       x the <i>x</i> coordinate of the upper left corner
     *                     of the oval to be filled.
     * @param       y the <i>y</i> coordinate of the upper left corner
     *                     of the oval to be filled.
     * @param       width the width of the oval to be filled.
     * @param       height the height of the oval to be filled.
     */
    public void fillOval(int x, int y, int width, int height)
    {
        this.fillOval((double)x, (double)y, (double)width, (double)height);
    }
    
    
    /**
     * Fills an oval bounded by the specified rectangle with the
     * current color.
     * @param       x the <i>x</i> coordinate of the upper left corner
     *                     of the oval to be filled.
     * @param       y the <i>y</i> coordinate of the upper left corner
     *                     of the oval to be filled.
     * @param       width the width of the oval to be filled.
     * @param       height the height of the oval to be filled.
     */
    public void fillOval(double x, double y, double width, double height)
    {
        // create boundary for hatch
        Vector<DXFEntity> boundary = new Vector<DXFEntity>();
        DXFEntity dxfEllipse = createDXFEllipticalArc(x, y, width, height, 0, 360);
        boundary.add(dxfEllipse);
        
        // add Hatch with the above boundary
        Vector<Vector<DXFEntity>> boundaries = new Vector<Vector<DXFEntity>>();
        boundaries.add(boundary);
        DXFHatch hatch = new DXFHatch(boundaries, color);
        dxfDocument.addEntity(hatch);
    }

    
    /**
     * Draws the outline of a circular or elliptical arc
     * covering the specified rectangle.
     * <p>
     * The resulting arc begins at <code>startAngle</code> and extends
     * for <code>arcAngle</code> degrees, using the current color.
     * Angles are interpreted such that 0&nbsp;degrees
     * is at the 3&nbsp;o'clock position.
     * A positive value indicates a counter-clockwise rotation
     * while a negative value indicates a clockwise rotation.
     * <p>
     * The center of the arc is the center of the rectangle whose origin
     * is (<i>x</i>,&nbsp;<i>y</i>) and whose size is specified by the
     * <code>width</code> and <code>height</code> arguments.
     * <p>
     * The angles are specified relative to the non-square extents of
     * the bounding rectangle such that 45 degrees always falls on the
     * line from the center of the ellipse to the upper right corner of
     * the bounding rectangle. As a result, if the bounding rectangle is
     * noticeably longer in one axis than the other, the angles to the
     * start and end of the arc segment will be skewed farther along the
     * longer axis of the bounds.
     * @param        x the <i>x</i> coordinate of the
     *                    upper-left corner of the arc to be drawn.
     * @param        y the <i>y</i>  coordinate of the
     *                    upper-left corner of the arc to be drawn.
     * @param        width the width of the arc to be drawn.
     * @param        height the height of the arc to be drawn.
     * @param        startAngle the beginning angle, in degrees
     * @param        sweepAngle the angular extent of the arc, in degrees,
     *                    relative to the start angle.
     */
    public void drawArc(int x, int y, int width, int height, int startAngle, int sweepAngle)
    {
        this.drawArc((double)x, (double)y, (double)width, (double)height, (double)startAngle, (double)sweepAngle);
    }
    
    
    /**
     * Draws the outline of a circular or elliptical arc
     * covering the specified rectangle.
     * <p>
     * The resulting arc begins at <code>startAngle</code> and extends
     * for <code>arcAngle</code> degrees, using the current color.
     * Angles are interpreted such that 0&nbsp;degrees
     * is at the 3&nbsp;o'clock position.
     * A positive value indicates a counter-clockwise rotation
     * while a negative value indicates a clockwise rotation.
     * <p>
     * The center of the arc is the center of the rectangle whose origin
     * is (<i>x</i>,&nbsp;<i>y</i>) and whose size is specified by the
     * <code>width</code> and <code>height</code> arguments.
     * <p>
     * The angles are specified relative to the non-square extents of
     * the bounding rectangle such that 45 degrees always falls on the
     * line from the center of the ellipse to the upper right corner of
     * the bounding rectangle. As a result, if the bounding rectangle is
     * noticeably longer in one axis than the other, the angles to the
     * start and end of the arc segment will be skewed farther along the
     * longer axis of the bounds.
     * @param        x the <i>x</i> coordinate of the
     *                    upper-left corner of the arc to be drawn.
     * @param        y the <i>y</i>  coordinate of the
     *                    upper-left corner of the arc to be drawn.
     * @param        width the width of the arc to be drawn.
     * @param        height the height of the arc to be drawn.
     * @param        startAngle the beginning angle, in degrees
     * @param        sweepAngle the angular extent of the arc, in degrees,
     *                    relative to the start angle.
     */
    public void drawArc(double x, double y, double width, double height, double startAngle, double sweepAngle)
    {
        // finally, create an ellipse with the given parameters
        DXFEntity arc = createDXFEllipticalArc(x, y, width, height, startAngle, sweepAngle);
        dxfDocument.addEntity(arc);
    }
    
    
    /**
     * Create a DXFEllipse representing the specified arc.
     * @param        x the <i>x</i> coordinate of the
     *                    upper-left corner of the arc to be drawn.
     * @param        y the <i>y</i>  coordinate of the
     *                    upper-left corner of the arc to be drawn.
     * @param        width the width of the arc to be drawn.
     * @param        height the height of the arc to be drawn.
     * @param        startAngle the beginning angle, in degrees
     * @param        sweepAngle the angular extent of the arc, in degrees,
     *                    relative to the start angle.
     * @return       DXFEllipse object corresponding to the specified arc                   
     */
    private DXFEntity createDXFEllipticalArc(double x, double y, double width, double height, double startAngle, double sweepAngle)
    {
        
        // get oval parameters
        double xCen = x + width/2;
        double yCen = y + height/2;
        
        // negate angles since y axis inverted going from Java to DXF
        startAngle = -startAngle;
        sweepAngle = -sweepAngle;
        
        double a = width/2;
        double b = height/2;
        
        double startParameter = (startAngle * (Math.PI / 180));
        double endParameter = ((startAngle + sweepAngle) * (Math.PI / 180));
        double sweepAngleRadians = (sweepAngle * (Math.PI / 180));
        
        RealPoint center = new RealPoint(xCen, yCen, 0);
        RealPoint startPoint = new RealPoint(xCen + a*Math.cos(startParameter), yCen + b*Math.sin(startParameter), 0);
        RealPoint endPoint = new RealPoint(xCen + a*Math.cos(endParameter), yCen + b*Math.sin(endParameter), 0);
        //RealPoint startPointRelative = RealPoint.difference(startPoint, center);
        //RealPoint endPointRelative = RealPoint.difference(endPoint, center);
        
        // The common case where the transform doesn't rotate the axes - not using this since the general case covers everything,
        // and this doesn't buy us anything in the ultimate DXF output - still get an ellipse
        /*
        if (preservesAxisOrientations(javaTransformMatrix))
        {
            RealPoint transformedCenter = RealPoint.mapPoint(graphicsMatrix, center);
            RealPoint transformedStartPoint = RealPoint.mapPoint(graphicsMatrix, startPoint);
            RealPoint transformedEndPoint = RealPoint.mapPoint(graphicsMatrix, endPoint);
            
            // map semi axes; since transformation is just a scaling plus translation, gives us new a and b
            double[] xSemiAxis = {a, 0};
            double[] ySemiAxis = {0, b};
            graphicsMatrix.mapVectors(xSemiAxis);
            graphicsMatrix.mapVectors(ySemiAxis);
            a = xSemiAxis[0];
            b = ySemiAxis[1];
            
            // need to adjust start and end angles based on java-to-dxf axis flip and clockwise vs counterclockwise angles
            if (sweepAngleRadians < 0)
            {
                startAngleRadians = endAngleRadians;
                sweepAngleRadians = -sweepAngleRadians;
            }
            
            
            DXFEllipse ellipse = new DXFEllipse(transformedCenter, a, b, startParameter, endParameter, paint);
            return ellipse;
        }
        */
        
        // Here goes the complex stuff for the general transformation case involving rotation and skewing of the ellipse    
       
        // Finding the new major axis under a linear transformation is actually a bit tricky: though an
        // ellipse is mapped to an ellipse under a linear (even affine) transformation, the major axis 
        // of the original ellipse is not mapped to the transformed ellipse's major axis by the transformation.
        // To find the new major axis, we do the following: represent points on the ellipse parametrically, as
        //   (x, y) = (a*cos(t), b*sin(t)) (which we'll use as a column vector). 
        // Representing the linear transformation by the matrix 
        //   (c11, c12)
        //   (c21, c22), 
        // the points of the linearly transformed ellipse become
        //   (x, y) = (c11*a*cos(t) + c12*b*sin(t), c21*a*cos(t) + c22*b*sin(t)).
        // Now the points on the major axis are the points farthest from the center (here 
        // at (0, 0) since we're using just the linear part of the affine transfomation). We can
        // then find these point by solving for t that maximizes the (square of) the distance from the center:
        //   D  =  x^2 + y^2  =  (c11*a*cos(t) + c12*b*sin(t))^2  +  (c21*a*cos(t) + c22*b*sin(t))^2
        // i.e., solve for t such that dD/dt = 0.
        //   dD/dt  =  2 * (c11*a*cos(t) + c12*b*sin(t)) * (-c11*a*sin(t) + c12*b*cos(t))
        //           + 2 * (c21*a*cos(t) + c22*b*sin(t)) * (-c21*a*sin(t) + c22*b*cos(t))
        //          =  2 * { [b^2*(c12^2 + c22^2) - a^2*(c11^2 + c21^2)] * sin(t) * cos(t)
        //                      + a*b*(c11*c12 + c21*c22) * (cos(t)^2 - sin(t)^2) }
        // Setting this equal to 0, and dividing by -2 and cos(t), gives
        //   A*tan(t)^2 + B*tan(t) - A  =  0,
        //      where  A  =  a*b*(c11*c12 + c21*c22)
        //      and    B  =  a^2*(c11^2 + c21^2) - b^2*(c12^2 + c22^2)
        // The quadratic formula then gives us our solution:
        //   tan(t)  =  (-B +/- sqrt(B^2 - 4*A*C))/(2*A)  =  (-B +/- sqrt(B^2 + 4*A^2))/(2*A)
        
        // get matrix coefficients
        double[] basisVects = {1, 0, 0, 1};
        mapVectors(graphicsMatrix, basisVects);
        double c11 = basisVects[0];
        double c12 = basisVects[2];
        double c21 = basisVects[1];
        double c22 = basisVects[3];
        
        double A = a*b*(c11*c12 + c21*c22);
        double B = a*a*(c11*c11 + c21*c21) - b*b*(c12*c12 + c22*c22);
        
        double tangent1;
        //double tangent2;
        if (A != 0)
        {
            tangent1 = (-B + Math.sqrt(B*B + 4*A*A))/(2*A);
            //tangent2 = (-B - Math.sqrt(B*B + 4*A*A))/(2*A);
        }
        else
        {
            tangent1 = 0;
            //tangent2 = 0;
        }
        
        double t1 = Math.atan(tangent1);
        double t2 = t1 + Math.PI/2;
        
        double majorAxisParameter;
        double minorAxisParameter;
        double majorAxisLength;
        double minorAxisLength;
        
        // see which is the major axis
        double D1 = ((c11*a*Math.cos(t1) + c12*b*Math.sin(t1))*(c11*a*Math.cos(t1) + c12*b*Math.sin(t1))  +  (c21*a*Math.cos(t1) + c22*b*Math.sin(t1))*(c21*a*Math.cos(t1) + c22*b*Math.sin(t1)));
        double D2 = ((c11*a*Math.cos(t2) + c12*b*Math.sin(t2))*(c11*a*Math.cos(t2) + c12*b*Math.sin(t2))  +  (c21*a*Math.cos(t2) + c22*b*Math.sin(t2))*(c21*a*Math.cos(t2) + c22*b*Math.sin(t2)));
        
        if (D1 >= D2)
        {
            majorAxisParameter = t1;
            minorAxisParameter = t2;
            majorAxisLength = Math.sqrt(D1);
            minorAxisLength = Math.sqrt(D2);
        }
        else
        {
            majorAxisParameter = t2;
            minorAxisParameter = t1;
            majorAxisLength = Math.sqrt(D2);
            minorAxisLength = Math.sqrt(D1);
        }
        
        double transformedAxisRatio = minorAxisLength/majorAxisLength;
        
        
        // get the transformed center, major and (relative) minor aces
        RealPoint transformedCenter = RealPoint.mapPoint(graphicsMatrix, center);
        RealPoint transformedMajorAxisRelative = new RealPoint(c11*a*Math.cos(majorAxisParameter) + c12*b*Math.sin(majorAxisParameter), c21*a*Math.cos(majorAxisParameter) + c22*b*Math.sin(majorAxisParameter), 0);
        RealPoint transformedMinorAxisRelative = new RealPoint(c11*a*Math.cos(minorAxisParameter) + c12*b*Math.sin(minorAxisParameter), c21*a*Math.cos(minorAxisParameter) + c22*b*Math.sin(minorAxisParameter), 0);
        
        // Since angles in dxf always go counterclockwise, we want the minor axis which is counterclockwise from the major axis; check the cross product
        // to see if we have the right one, and negate if not
        RealPoint crossProduct = RealPoint.crossProduct(transformedMajorAxisRelative, transformedMinorAxisRelative);
        if (crossProduct.z < 0)
            transformedMinorAxisRelative = RealPoint.scalarProduct(-1, transformedMinorAxisRelative);
        
        // get transformed start and end points for center lines if needed, as well as to find corresponding ellipse parameters
        RealPoint transformedStartPoint = RealPoint.mapPoint(graphicsMatrix, startPoint);
        RealPoint transformedEndPoint = RealPoint.mapPoint(graphicsMatrix, endPoint);
        RealPoint transformedStartPointRelative = RealPoint.difference(transformedStartPoint, transformedCenter);
        RealPoint transformedEndPointRelative = RealPoint.difference(transformedEndPoint, transformedCenter);
        
        // want the arc that goes between the start and end points; but need to supply the start and end parametric t values,
        // and these are relative to the major axis. So we need to find the t values corresponding to the start and end points.
        // To do this, we decompose relative to the major and minor axes and use the basic ellipse equation to find the corresponding t values.
        // Let (u, v) represent the coordinates of a point on the ellipse in the major/minor axis coordinate system. Then
        //    (u, v)  =  (majorAxisLength*cos(t), minorAxisLength*sin(t)),  or
        //    v/u  =  minorAxisLength/majorAxisLength*tan(t), or   t = atan((v*majorAxisLength)/(u*minorAxisLength)), where
        //    u  =  startVector dotproduct majorAxisVector/magnitude(majorAxisVector), and v  =  same with minorAxisVector
        
        double transformedStartParameter;
        double transformedEndParameter;
        
        double uStart = RealPoint.dotProduct(transformedStartPointRelative, transformedMajorAxisRelative)/RealPoint.magnitude(transformedMajorAxisRelative);
        double vStart = RealPoint.dotProduct(transformedStartPointRelative, transformedMinorAxisRelative)/RealPoint.magnitude(transformedMinorAxisRelative);
        if (uStart == 0)
        {
            if (vStart > 0)
                transformedStartParameter = Math.PI/2;
            else
                transformedStartParameter = -Math.PI/2;
        }
        else
        {
            transformedStartParameter = Math.atan((vStart*majorAxisLength)/(uStart*minorAxisLength));
            if (uStart < 0)
            {
                transformedStartParameter += Math.PI;
            }
        }
        
        
        double uEnd = RealPoint.dotProduct(transformedEndPointRelative, transformedMajorAxisRelative)/RealPoint.magnitude(transformedMajorAxisRelative);
        double vEnd = RealPoint.dotProduct(transformedEndPointRelative, transformedMinorAxisRelative)/RealPoint.magnitude(transformedMinorAxisRelative); 
        if (uEnd == 0)
        {
            if (vEnd > 0)
                transformedEndParameter = Math.PI/2;
            else
                transformedEndParameter = -Math.PI/2;
        }
        else
        {
            transformedEndParameter = Math.atan((vEnd*majorAxisLength)/(uEnd*minorAxisLength));
            if (uEnd < 0)
            {
                transformedEndParameter += Math.PI;
            }
        }
        
        // still need to handle the orientation of the start and end parameters; need to supply them in the right
        // order so we get the proper arc. Since angles in Java go clockwise, we'll need to flip the start and end parameters
        // unless the sweep angle's negative or we don't flip the basis vector orientations. We determine the basic vector 
        // orientations using the cross product.
        double dxfStartParameter = transformedStartParameter;
        double dxfEndParameter = transformedEndParameter;
        boolean dxfIsCounterclockwise = true;
        
        RealPoint basisVectorI = new RealPoint(1, 0, 0);
        RealPoint basisVectorJ = new RealPoint(0, 1, 0);
        RealPoint transformedBasisVectorI = RealPoint.mapVector(graphicsMatrix, basisVectorI);
        RealPoint transformedBasisVectorJ = RealPoint.mapVector(graphicsMatrix, basisVectorJ);
        RealPoint transformedBasisVectorCrossProduct = RealPoint.crossProduct(transformedBasisVectorI, transformedBasisVectorJ);
        double orientationSign = transformedBasisVectorCrossProduct.z;
        
        if (orientationSign * sweepAngle < 0)
        {
            dxfStartParameter = transformedEndParameter;
            dxfEndParameter = transformedStartParameter;
            dxfIsCounterclockwise = false;
        }
        
        // cover special case where sweep angle >= 2*PI or <= -2*PI
        if (Math.abs(sweepAngleRadians) >= 2 * Math.PI)
        {
            dxfEndParameter = dxfStartParameter + 2 * Math.PI;
        }
        
        //System.out.println("Arc:  start point: (" + transformedStartPoint.x + ", " + transformedStartPoint.y + ")");
        //System.out.println("        end point: (" + transformedEndPoint.x + ", " + transformedEndPoint.y + ")");
        //System.out.println("      start angle: " + dxfStartParameter);
        //System.out.println("        end angle: " + dxfEndParameter);
        //System.out.println(" counterclockwise: " + dxfIsCounterclockwise);
        
        // finally, create an ellipse or arc or circle with the given parameters
        if ((useCircles) && (transformedAxisRatio == 1))
        {
            // we have a circle or circular arc, and are configured to generate DXF circles and arcs
            double radius = RealPoint.magnitude(transformedMajorAxisRelative);
            
            if (Math.abs(dxfEndParameter - dxfStartParameter) >= 2*Math.PI)
            {
                // full circle
                return new DXFCircle(transformedCenter, radius, this);
            }
            else
            {
                // circular arc - need to determine start and end parameters relative to standard axes
                double majorAxisAngle = RealPoint.angleBetween(new RealPoint(1,0,0), transformedMajorAxisRelative);
                if (transformedMajorAxisRelative.y < 0)
                    majorAxisAngle = -majorAxisAngle;
                double arcStartAngle = majorAxisAngle + dxfStartParameter;
                double arcEndAngle = majorAxisAngle + dxfEndParameter;
                return new DXFArc(transformedCenter, radius, arcStartAngle, arcEndAngle, dxfIsCounterclockwise, this);
            }
        }
        else
        {
            return new DXFEllipse(transformedCenter, transformedMajorAxisRelative, transformedAxisRatio, dxfStartParameter, dxfEndParameter, dxfIsCounterclockwise, this);
        }
    }
    
    
    
    /* Original simple version...
    public void drawArc(int x, int y, int width, int height, int startAngle, int sweepAngle)
    {
        RealPoint center = new RealPoint(x + width/2, y + height/2, 0);
        RealPoint majorAxisEndpoint;
        double axisRatio;
        double startParameter = startAngle * (Math.PI/180);
        double endParameter = (startAngle + sweepAngle) * (Math.PI/180);
        
        if (width > height)
        {
            // major axis is parallel to x axis
            majorAxisEndpoint = new RealPoint(center.x + width/2, center.y, 0);
            axisRatio = height/width;
        }
        else
        {
            // major axis is parallel to y axis
            majorAxisEndpoint = new RealPoint(center.x, center.y + height/2, 0);
            axisRatio = width/height;
        }
        
        DXFEllipse ellipse = new DXFEllipse(center, majorAxisEndpoint, axisRatio, startParameter, endParameter, this);
        dxfDocument.addEntity(ellipse);
    }
    */

    /**
     * Fills a circular or elliptical arc wedge covering the specified rectangle.
     * <p>
     * The resulting arc begins at <code>startAngle</code> and extends
     * for <code>arcAngle</code> degrees.
     * Angles are interpreted such that 0&nbsp;degrees
     * is at the 3&nbsp;o'clock position.
     * A positive value indicates a counter-clockwise rotation
     * while a negative value indicates a clockwise rotation.
     * <p>
     * The center of the arc is the center of the rectangle whose origin
     * is (<i>x</i>,&nbsp;<i>y</i>) and whose size is specified by the
     * <code>width</code> and <code>height</code> arguments.
     * <p>
     * The angles are specified relative to the non-square extents of
     * the bounding rectangle such that 45 degrees always falls on the
     * line from the center of the ellipse to the upper right corner of
     * the bounding rectangle. As a result, if the bounding rectangle is
     * noticeably longer in one axis than the other, the angles to the
     * start and end of the arc segment will be skewed farther along the
     * longer axis of the bounds.
     * @param        x the <i>x</i> coordinate of the
     *                    upper-left corner of the arc to be filled.
     * @param        y the <i>y</i>  coordinate of the
     *                    upper-left corner of the arc to be filled.
     * @param        width the width of the arc to be filled.
     * @param        height the height of the arc to be filled.
     * @param        startAngle the beginning angle.
     * @param        arcAngle the angular extent of the arc,
     *                    relative to the start angle.
     */
    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle)
    {
        // create boundary path for the wedge
        Vector<DXFEntity> boundary = new Vector<DXFEntity>();
        
        double startParameter = (startAngle * (Math.PI / 180));
        double endParameter = ((startAngle + arcAngle) * (Math.PI / 180));
        double halfWidth = width/2.0;
        double halfHeight = height/2.0;
        
        RealPoint center = new RealPoint(x + halfWidth, y + halfHeight, 0);
        RealPoint startPoint = new RealPoint(center.x + halfWidth * Math.cos(startParameter), center.y - halfHeight * Math.sin(startParameter), 0);
        RealPoint endPoint = new RealPoint(center.x + halfWidth * Math.cos(endParameter), center.y - halfHeight * Math.sin(endParameter), 0);
        
        // want it to be counterclockwise, so differentiate between positive and negative sweep angles
        if (arcAngle >= 0)
        {
            boundary.add(createDXFEllipticalArc(x, y, width, height, startAngle, arcAngle));
            boundary.add(createDXFLine(endPoint.x, endPoint.y, center.x, center.y));
            boundary.add(createDXFLine(center.x, center.y, startPoint.x, startPoint.y));
        }
        else
        {
            boundary.add(createDXFEllipticalArc(x, y, width, height, startAngle + arcAngle, -arcAngle));
            boundary.add(createDXFLine(startPoint.x, startPoint.y, center.x, center.y));
            boundary.add(createDXFLine(center.x, center.y, endPoint.x, endPoint.y));
        }
        
        // add Hatch with the above boundary
        Vector<Vector<DXFEntity>> boundaries = new Vector<Vector<DXFEntity>>();
        boundaries.add(boundary);
        DXFHatch hatch = new DXFHatch(boundaries, color);
        dxfDocument.addEntity(hatch);
        
    }

    /**
     * Draws a sequence of connected lines defined by
     * arrays of <i>x</i> and <i>y</i> coordinates.
     * Each pair of (<i>x</i>,&nbsp;<i>y</i>) coordinates defines a point.
     * The figure is not closed if the first point
     * differs from the last point.
     * @param       xPoints an array of <i>x</i> points
     * @param       yPoints an array of <i>y</i> points
     * @param       nPoints the total number of points
     */
    public void drawPolyline(int xPoints[], int yPoints[], int nPoints)
    {
        double[] xPointsFloat = new double[nPoints];
        double[] yPointsFloat = new double[nPoints];
        for (int i = 0; i < nPoints; i++)
        {
            xPointsFloat[i] = xPoints[i];
            yPointsFloat[i] = yPoints[i];
        }
        
        this.drawPolyline(xPointsFloat, yPointsFloat, nPoints);
    }
    
    
    /**
     * Draws a sequence of connected lines defined by
     * arrays of <i>x</i> and <i>y</i> coordinates.
     * Each pair of (<i>x</i>,&nbsp;<i>y</i>) coordinates defines a point.
     * The figure is not closed if the first point
     * differs from the last point.
     * @param       xPoints an array of <i>x</i> points
     * @param       yPoints an array of <i>y</i> points
     * @param       nPoints the total number of points
     */
    public void drawPolyline(double xPoints[], double yPoints[], int nPoints)
    {
        // close if last point equals first point
        boolean isClosed;
        if((xPoints[0] == xPoints[nPoints-1]) && (yPoints[0] == yPoints[nPoints-1]))
        {
            isClosed = true;
        }
        else
        {
            isClosed = false;
        }
        
        drawPolyline(xPoints, yPoints, nPoints, isClosed);
    }
    
    
    /**
     * Draws a sequence of connected lines defined by
     * arrays of <i>x</i> and <i>y</i> coordinates.
     * Each pair of (<i>x</i>,&nbsp;<i>y</i>) coordinates defines a point.
     * The isClosed parameter indicates if the polygon should be closed,
     * i.e., have a line segment added joining the first and last points
     * if they are different.
     * @param       xPoints an array of <i>x</i> points
     * @param       yPoints an array of <i>y</i> points
     * @param       nPoints the total number of points
     * @param       isClosed - indicate if the polygon should be closed
     */
    private void drawPolyline(double xPoints[], double yPoints[], int nPoints, boolean isClosed)
    {
        // map points
        double[] pts = new double[2*nPoints];
        for (int i = 0; i < nPoints; i++)
        {
            pts[2*i] = xPoints[i];
            pts[2*i+1] = yPoints[i];
        }
        graphicsMatrix.transform(pts, 0, pts, 0, nPoints);
        
        // create as lightweight polyline
        Vector<RealPoint> vertices = new Vector<RealPoint>();
        for (int i = 0; i < nPoints; i++)
        {
            vertices.add(new RealPoint(pts[2*i], pts[2*i+1], 0));
        }
        
        DXFLWPolyline polyline = new DXFLWPolyline(nPoints, vertices, isClosed, this);
        dxfDocument.addEntity(polyline);
    }

    /**
     * Draws a closed polygon defined by
     * arrays of <i>x</i> and <i>y</i> coordinates.
     * Each pair of (<i>x</i>,&nbsp;<i>y</i>) coordinates defines a point.
     * <p>
     * This method draws the polygon defined by <code>nPoint</code> line
     * segments, where the first <code>nPoint&nbsp;-&nbsp;1</code>
     * line segments are line segments from
     * <code>(xPoints[i&nbsp;-&nbsp;1],&nbsp;yPoints[i&nbsp;-&nbsp;1])</code>
     * to <code>(xPoints[i],&nbsp;yPoints[i])</code>, for
     * 1&nbsp;&le;&nbsp;<i>i</i>&nbsp;&le;&nbsp;<code>nPoints</code>.
     * The figure is automatically closed by drawing a line connecting
     * the final point to the first point, if those points are different.
     * @param        xPoints   a an array of <code>x</code> coordinates.
     * @param        yPoints   a an array of <code>y</code> coordinates.
     * @param        nPoints   a the total number of points.
     */
    public void drawPolygon(int xPoints[], int yPoints[], int nPoints)
    {
        double[] xPointsFloat = new double[nPoints];
        double[] yPointsFloat = new double[nPoints];
        for (int i = 0; i < nPoints; i++)
        {
            xPointsFloat[i] = xPoints[i];
            yPointsFloat[i] = yPoints[i];
        }
        
        this.drawPolygon(xPointsFloat, yPointsFloat, nPoints);
    }
    
    
    /**
     * Draws a closed polygon defined by
     * arrays of <i>x</i> and <i>y</i> coordinates.
     * Each pair of (<i>x</i>,&nbsp;<i>y</i>) coordinates defines a point.
     * <p>
     * This method draws the polygon defined by <code>nPoint</code> line
     * segments, where the first <code>nPoint&nbsp;-&nbsp;1</code>
     * line segments are line segments from
     * <code>(xPoints[i&nbsp;-&nbsp;1],&nbsp;yPoints[i&nbsp;-&nbsp;1])</code>
     * to <code>(xPoints[i],&nbsp;yPoints[i])</code>, for
     * 1&nbsp;&le;&nbsp;<i>i</i>&nbsp;&le;&nbsp;<code>nPoints</code>.
     * The figure is automatically closed by drawing a line connecting
     * the final point to the first point, if those points are different.
     * @param        xPoints   a an array of <code>x</code> coordinates.
     * @param        yPoints   a an array of <code>y</code> coordinates.
     * @param        nPoints   a the total number of points.
     */
    public void drawPolygon(double xPoints[], double yPoints[], int nPoints)
    {
        this.drawPolyline(xPoints, yPoints, nPoints, true);
    }

    
    /**
     * Draws the outline of a polygon defined by the specified
     * <code>Polygon</code> object.
     * @param        p the polygon to draw.
     */
    public void drawPolygon(Polygon p) 
    {
        drawPolygon(p.xpoints, p.ypoints, p.npoints);
    }

    
    /**
     * Fills a closed polygon defined by
     * arrays of <i>x</i> and <i>y</i> coordinates.
     * <p>
     * This method fills the polygon defined by <code>nPoint</code> line
     * segments, where the first <code>nPoint&nbsp;-&nbsp;1</code>
     * line segments are line segments from
     * <code>(xPoints[i&nbsp;-&nbsp;1],&nbsp;yPoints[i&nbsp;-&nbsp;1])</code>
     * to <code>(xPoints[i],&nbsp;yPoints[i])</code>, for
     * 1&nbsp;&le;&nbsp;<i>i</i>&nbsp;&le;&nbsp;<code>nPoints</code>.
     * The figure is automatically closed by drawing a line connecting
     * the final point to the first point, if those points are different.
     * @param        xPoints   a an array of <code>x</code> coordinates.
     * @param        yPoints   a an array of <code>y</code> coordinates.
     * @param        nPoints   a the total number of points.
     */
    public void fillPolygon(int xPoints[], int yPoints[], int nPoints)
    {
        Polygon p = new Polygon(xPoints, yPoints, nPoints);
        fillPolygon(p);
        
        /*
        double[] xPointsFloat = new double[nPoints];
        double[] yPointsFloat = new double[nPoints];
        for (int i = 0; i < nPoints; i++)
        {
            xPointsFloat[i] = xPoints[i];
            yPointsFloat[i] = yPoints[i];
        }
        
        this.fillPolygon(xPointsFloat, yPointsFloat, nPoints);
        */
    }
    
    
    /**
     * Fills a closed polygon defined by
     * arrays of <i>x</i> and <i>y</i> coordinates.
     * <p>
     * This method fills the polygon defined by <code>nPoint</code> line
     * segments, where the first <code>nPoint&nbsp;-&nbsp;1</code>
     * line segments are line segments from
     * <code>(xPoints[i&nbsp;-&nbsp;1],&nbsp;yPoints[i&nbsp;-&nbsp;1])</code>
     * to <code>(xPoints[i],&nbsp;yPoints[i])</code>, for
     * 1&nbsp;&le;&nbsp;<i>i</i>&nbsp;&le;&nbsp;<code>nPoints</code>.
     * The figure is automatically closed by drawing a line connecting
     * the final point to the first point, if those points are different.
     * @param        xPoints   a an array of <code>x</code> coordinates.
     * @param        yPoints   a an array of <code>y</code> coordinates.
     * @param        nPoints   a the total number of points.
     */
    public void fillPolygon(double xPoints[], double yPoints[], int nPoints)
    {
        int[] xPointsInt = new int[nPoints];
        int[] yPointsInt = new int[nPoints];
        for (int i = 0; i < nPoints; i++)
        {
            xPointsInt[i] = (int)xPoints[i];
            yPointsInt[i] = (int)yPoints[i];
        }
        
        Polygon p = new Polygon(xPointsInt, yPointsInt, nPoints);
        this.fill(p);
    }

    
    /**
     * Fills the polygon defined by the specified Polygon object with
     * the graphics context's current color.
     * @param        p the polygon to fill.
     */
    public void fillPolygon(Polygon p) {
        fill(p);
    }
    
    
    
    /**
     * An extension method that supports the drawing of a B-spline. For the common case of a spline that passes through 
     * the first and last control points, set the multiplicity of these equal to the spline degree plus 1.
     * 
     * @param degree        The degree of the spline; must be greater than 0
     * @param controlPoints The control points that define the shape of the spline, {x1, y1, x2, y2, ..., xn, yn}.
     * @param multiplicities    The multiplicities for the control points; the number of multiplicity values must equal the
     *                      number of control points (i.e., the array size must be half that of the controlPoints array)
     * @param throughEndpoints  If true, the spline will be forced to pass through the endpoints by setting the end
     *                      control point multiplicities to degree + 1
     */
    public void drawSpline(int degree, double[] controlPoints, int[] multiplicities, boolean throughEndpoints)
    {
        Vector<SplineControlPoint> transformedControlPoints = new Vector<SplineControlPoint>();
        
        // transform the control points according to the current transform matrix
        for (int i = 0; i < controlPoints.length/2; i++)
        {
            double[] coords = new double[]{controlPoints[2*i], controlPoints[2*i+1]};
            graphicsMatrix.transform(coords, 0, coords, 0, 1);
            SplineControlPoint transformedControlPoint = new SplineControlPoint(coords[0], coords[1], 0, multiplicities[i]);
            transformedControlPoints.add(transformedControlPoint);
        }
        
        DXFSpline spline = new DXFSpline(degree, transformedControlPoints, throughEndpoints, this);
        dxfDocument.addEntity(spline);
    }
    
    
    
    /**
     * An extension method that supports the drawing of a B-spline with an arbitrary knot sequence. This is included
     * specifically to cover the case of the quadratic and cubic curve segments generated by PathIterator, which are
     * specified as Bezier curves, or B-splines with knots {0,0,0,0,1,1,1,1} (for the cubic). This will handle other
     * knot sequences if needed; however, note the constraint on the number of knots vs the number of control points.
     * Also, the multiplicities of the control points are all set to 1.
     * 
     * @param degree        The degree of the spline; must be greater than 0
     * @param controlPoints The control points that define the shape of the spline, {x1, y1, x2, y2, ..., xn, yn}.
     * @param knots         The knots for the control points; the number of knot values must equal the
     *                      number of control points plus the degree plus 1. Setting the first degree + 1 knots equal 
     *                      and the last degree + 1 knots equal will make the spline pass through the endpoints
     */
    public void drawSpline(int degree, double[] controlPoints, double[] knots)
    {
        // transform the control points according to the current transform matrix
        graphicsMatrix.transform(controlPoints, 0, controlPoints, 0, controlPoints.length/2);
        DXFSpline spline = new DXFSpline(degree, controlPoints, knots, this);
        dxfDocument.addEntity(spline);
    }
    

    /**
     * Draws the text given by the specified string, using this
     * graphics context's current font and color. The baseline of the
     * leftmost character is at position (<i>x</i>,&nbsp;<i>y</i>) in this
     * graphics context's coordinate system.
     * @param       str      the string to be drawn.
     * @param       x        the <i>x</i> coordinate.
     * @param       y        the <i>y</i> coordinate.
     * @throws NullPointerException if <code>str</code> is <code>null</code>.
     */
    public void drawString(String str, int x, int y)
    {
        drawString(str, (double)x, (double)y);
    }

    /**
     * Renders the text of the specified iterator, but ignoring text attributes.
     * <p>
     * The baseline of the leftmost character is at position
     * (<i>x</i>,&nbsp;<i>y</i>) in this graphics context's coordinate system.
     * @param       charIterator   the iterator whose text is to be drawn
     * @param       x        the <i>x</i> coordinate.
     * @param       y        the <i>y</i> coordinate.
     * @throws NullPointerException if <code>iterator</code> is
     * <code>null</code>.
     */
   public void drawString(AttributedCharacterIterator charIterator, int x, int y)
   {
       if (charIterator == null)
       {
           throw new NullPointerException();
       }
       else
       {
           // Just use String method
           StringBuffer stringBuffer = new StringBuffer();
           
           for(char c = charIterator.first(); c != CharacterIterator.DONE; c = charIterator.next()) 
           {
               stringBuffer.append(c);
           }
           
           drawString(stringBuffer.toString(), x, y);
       }
   }

    /**
     * Draws the text given by the specified character array, using this
     * graphics context's current font and color. The baseline of the
     * first character is at position (<i>x</i>,&nbsp;<i>y</i>) in this
     * graphics context's coordinate system.
     * @param data the array of characters to be drawn
     * @param offset the start offset in the data
     * @param length the number of characters to be drawn
     * @param x the <i>x</i> coordinate of the baseline of the text
     * @param y the <i>y</i> coordinate of the baseline of the text
     * @throws NullPointerException if <code>data</code> is <code>null</code>.
     * @throws IndexOutOfBoundsException if <code>offset</code> or
     * <code>length</code>is less than zero, or
     * <code>offset+length</code> is greater than the length of the
     * <code>data</code> array.
     * @see         java.awt.Graphics#drawBytes
     * @see         java.awt.Graphics#drawString
     */
    public void drawChars(char data[], int offset, int length, int x, int y) 
    {
        drawString(new String(data, offset, length), x, y);
    }

    
    /**
     * Draws the text given by the specified byte array, using this
     * graphics context's current font and color. The baseline of the
     * first character is at position (<i>x</i>,&nbsp;<i>y</i>) in this
     * graphics context's coordinate system.
     * <p>
     * Use of this method is not recommended as each byte is interpreted
     * as a Unicode code point in the range 0 to 255, and so can only be
     * used to draw Latin characters in that range.
     * @param data the data to be drawn
     * @param offset the start offset in the data
     * @param length the number of bytes that are drawn
     * @param x the <i>x</i> coordinate of the baseline of the text
     * @param y the <i>y</i> coordinate of the baseline of the text
     * @throws NullPointerException if <code>data</code> is <code>null</code>.
     * @throws IndexOutOfBoundsException if <code>offset</code> or
     * <code>length</code>is less than zero, or <code>offset+length</code>
     * is greater than the length of the <code>data</code> array.
     */
    @SuppressWarnings("deprecation")
    public void drawBytes(byte data[], int offset, int length, int x, int y) {
        drawString(new String(data, 0, offset, length), x, y);
    }

    
    /**
     * Throws UnsupportedOperationException, as drawImage not supported.
     */
    public boolean drawImage(Image img, int x, int y, ImageObserver observer)
    {
        // Not supported
        throw new UnsupportedOperationException("Image drawing not supported");
    }

    
    /**
     * Throws UnsupportedOperationException, as drawImage not supported.
     */
    public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer)
    {
        // Not supported
        throw new UnsupportedOperationException("Image drawing not supported");
    }
    
    
    
    /**
     * Throws UnsupportedOperationException, as drawImage not supported.
     */
    public boolean drawImage(Image img, int x, int y,
                                      Color bgcolor,
                                      ImageObserver observer)
    {
        // Not supported
        throw new UnsupportedOperationException("Image drawing not supported");
    }

    
    /**
     * Throws UnsupportedOperationException, as drawImage not supported.
     */
    public boolean drawImage(Image img, int x, int y,
                                      int width, int height,
                                      Color bgcolor,
                                      ImageObserver observer)
    {
        // Not supported
        throw new UnsupportedOperationException("Image drawing not supported");
    }

    
    /**
     * Throws UnsupportedOperationException, as drawImage not supported.
     */
    public boolean drawImage(Image img,
                                      int dx1, int dy1, int dx2, int dy2,
                                      int sx1, int sy1, int sx2, int sy2,
                                      ImageObserver observer)
    {
        // Not supported
        throw new UnsupportedOperationException("Image drawing not supported");
    }
    
    
    
    /**
     * Throws UnsupportedOperationException, as drawImage not supported.
     */
    public boolean drawImage(Image img,
                                      int dx1, int dy1, int dx2, int dy2,
                                      int sx1, int sy1, int sx2, int sy2,
                                      Color bgcolor,
                                      ImageObserver observer)
    {
        // Not supported
        throw new UnsupportedOperationException("Image drawing not supported");
    }
    
    
    
    
    /**
     * Disposes of this graphics context and releases
     * any system resources that it is using. Does nothing here.
     */
    public void dispose()
    {
        // nothing special here to do...
    }

    
    /**
     * Disposes of this graphics context once it is no longer referenced.
     * @see #dispose
     */
    public void finalize() 
    {
        dispose();
    }

    
    /**
     * Returns a <code>String</code> object representing this
     *                        <code>Graphics</code> object's value.
     * @return       a string representation of this graphics context.
     */
    public String toString() 
    {
        return getClass().getName() + "[font=" + getFont() + ",color=" + getColor() + "]";
    }

    
    /**
     * Returns null as clipping not supported.
     * @return      null
     */
    @Deprecated
    public Rectangle getClipRect() {
        return getClipBounds();
    }

    
    /**
     * Returns true as clipping not supported.
     * @return true.
     */
    public boolean hitClip(int x, int y, int width, int height) 
    {    
        return true;
    }

    
    /**
     * Returns r, as clipping not supported.
     * @param  r    the rectangle where the current clipping area is
     *              copied to.  Any current values in this rectangle are
     *              overwritten.
     * @return      r.
     */
    public Rectangle getClipBounds(Rectangle r) 
    {
        return r;
    }
    
    
    /**
     * Add a font style if a corresponding one isn't already present.
     * 
     * @param newPaint
     */
    private DXFStyle addFontStyle()
    {
        
        DXFStyle style = new DXFStyle(this.font);
        return dxfDocument.addStyle(style);
       
    }

    /**
     * Does nothing...
     */
    @Override
    public void addRenderingHints(Map<?, ?> arg0)
    {
        // do nothing
        
    }

    /**
     * Does nothing as clipping not supported.
     */
    @Override
    public void clip(Shape arg0)
    {
        // do nothing no clipping
        
    }

    
    /**
     * Draw the specified shape.
     * 
     * @param   shape  shape to be drawn
     */
    @Override
    public void draw(Shape shape)
    {
        
        // branch based on the type of shape
        if (shape instanceof Line2D)
        {
            Line2D line = (Line2D)shape;
            drawLine(line.getX1(), line.getY1(), line.getX2(), line.getY2());
        }
        else if (shape instanceof Ellipse2D)
        {
            Ellipse2D ellipse = (Ellipse2D)shape;
            Rectangle2D rect = ellipse.getFrame();
            this.drawOval(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
        }
        else if (shape instanceof Arc2D)
        {
            Arc2D arc = (Arc2D)shape;
            Rectangle2D rect = arc.getFrame();
            this.drawArc(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), arc.getAngleStart(), arc.getAngleExtent());
        }
        else if (shape instanceof Rectangle2D)
        {
            Rectangle2D rect = (Rectangle2D)shape;
            this.drawRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
        }
        else if (shape instanceof RoundRectangle2D)
        {
            RoundRectangle2D rect = (RoundRectangle2D)shape;
            this.drawRoundRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), rect.getArcWidth(), rect.getArcHeight());
        }
        else if (shape instanceof QuadCurve2D)
        {
            QuadCurve2D spline = (QuadCurve2D)shape;
            double[] controlPoints = {spline.getX1(), spline.getY1(), spline.getCtrlX(), spline.getCtrlY(), spline.getX2(), spline.getY2()};
            double[] knots = {0,0,0,1,1,1};
            this.drawSpline(2, controlPoints, knots);
        }
        else if (shape instanceof CubicCurve2D)
        {
            CubicCurve2D spline = (CubicCurve2D)shape;
            double[] controlPoints = {spline.getX1(), spline.getY1(), spline.getCtrlX1(), spline.getCtrlY1(), spline.getCtrlX2(), spline.getCtrlY2(), spline.getX2(), spline.getY2()};
            double[] knots = {0,0,0,0,1,1,1,1};
            this.drawSpline(3, controlPoints, knots);
        }
        else
        {
            // general approach: get a PathIterator, and draw each returned path segment
            PathIterator iterator = shape.getPathIterator(new AffineTransform());
            double currentX = 0;
            double currentY = 0;
            double initialX = 0;
            double initialY = 0;
            
            double[] coords = new double[6]; 
            int curveType = iterator.currentSegment(coords);
            
            // first step has to be a MOVETO, to set initial point
            if (curveType == PathIterator.SEG_MOVETO)
            {
                initialX = coords[0];
                initialY = coords[1];
                
                currentX = coords[0];
                currentY = coords[1];
                
                iterator.next();
                
                while (!iterator.isDone())
                {
                   curveType = iterator.currentSegment(coords);
                    
                    switch (curveType)
                    {
                        case PathIterator.SEG_MOVETO:
                        {
                            // set as initial point for use in subsequent SEG_CLOSE operation
                            initialX = coords[0];
                            initialY = coords[1];
                            
                            currentX = coords[0];
                            currentY = coords[1];
                            break;
                        }
                        
                        case PathIterator.SEG_LINETO:
                        {
                            drawLine(currentX, currentY, coords[0], coords[1]);
                            currentX = coords[0];
                            currentY = coords[1];
                            break;
                        }
                        
                        case PathIterator.SEG_QUADTO:
                        {
                            double[] controlPoints = {currentX /*X1*/, currentY /*Y1*/, coords[0] /*CtrlX*/, coords[1] /*CtrlY*/, coords[2] /*X2*/, coords[3] /*Y2*/};
                            double[] knots = {0,0,0,1,1,1};
                            this.drawSpline(2, controlPoints, knots);
                            
                            currentX = coords[2];
                            currentY = coords[3];
                            break;
                        }
                        
                        case PathIterator.SEG_CUBICTO:
                        {
                            double[] controlPoints = {currentX /*X1*/, currentY /*Y1*/, coords[0] /*CtrlX1*/, coords[1] /*CtrlY1*/, coords[2] /*CtrlX2*/, coords[3] /*CtrlY2*/, coords[4] /*X2*/, coords[5] /*Y2*/};
                            double[] knots = {0,0,0,0,1,1,1,1};
                            this.drawSpline(3, controlPoints, knots);
                            
                            currentX = coords[4];
                            currentY = coords[5];
                            break;
                        }
                        
                        case PathIterator.SEG_CLOSE:
                        {
                            // add a line if we're not already closed
                            if ((currentX != initialX) || (currentY != initialY))
                            {
                                drawLine(currentX, currentY, initialX, initialY);
                            }
                            
                            break;
                        }
                        
                        default:
                        {
                            // do nothing
                            break;
                        }
                    }
                    
                    iterator.next();
                    
                }
               
            
            }
            
        }   
        
        
        
    }

    
    /**
     * Throws UnsupportedOperationException as glyph drawing not supported.
     */
    @Override
    public void drawGlyphVector(GlyphVector arg0, float arg1, float arg2)
    {
        throw new UnsupportedOperationException("Glyph drawing not supported");
    }

    
    /**
     * Throws UnsupportedOperationException as image drawing not supported.
     */
    @Override
    public boolean drawImage(Image arg0, AffineTransform arg1, ImageObserver arg2)
    {
        // Not supported
        throw new UnsupportedOperationException("Image drawing not supported");
    }

    
    /**
     * Throws UnsupportedOperationException as image drawing not supported.
     */
    @Override
    public void drawImage(BufferedImage arg0, BufferedImageOp arg1, int arg2, int arg3)
    {
        // Not supported
        throw new UnsupportedOperationException("Image drawing not supported");
    }

    
    /**
     * Throws UnsupportedOperationException as image drawing not supported.
     */
    @Override
    public void drawRenderableImage(RenderableImage arg0, AffineTransform arg1)
    {
        // Not supported
        throw new UnsupportedOperationException("Image drawing not supported");
    }

    
    /**
     * Throws UnsupportedOperationException as image drawing not supported.
     */
    @Override
    public void drawRenderedImage(RenderedImage arg0, AffineTransform arg1)
    {
        // Not supported
        throw new UnsupportedOperationException("Image drawing not supported");
    }

    @Override
    public void drawString(String str, float x, float y)
    {
        drawString(str, (double)x, (double)y);
    }
    
    
    /**
     * Draws the text given by the specified string, using this
     * graphics context's current font and color. The baseline of the
     * leftmost character is at position (<i>x</i>,&nbsp;<i>y</i>) in this
     * graphics context's coordinate system.
     * @param       string   the string to be drawn.
     * @param       x        the <i>x</i> coordinate.
     * @param       y        the <i>y</i> coordinate.
     * @throws NullPointerException if <code>str</code> is <code>null</code>.
     */
    @SuppressWarnings("unused")
    public void drawString(String string, double x, double y)
    {
        if (string == null)
        {
            throw new NullPointerException();
        }
        else
        {
            DXFStyle style = addFontStyle();
            
            // transform the coordinates using the current graphics matrix
            double[] coords = new double[]{x, y};
            graphicsMatrix.transform(coords, 0, coords, 0, 1);
            
            // get the rotation and oblique angles from the current graphics matrix
            // by mapping the unit vectors
            double[] unitVectorICoords = new double[]{1, 0};
            double[] unitVectorJCoords = new double[]{0, 1};
            double[] unitVectorITransformedCoords = new double[]{1, 0};
            double[] unitVectorJTransformedCoords = new double[]{0, 1};
            mapVectors(graphicsMatrix, unitVectorITransformedCoords);
            mapVectors(graphicsMatrix, unitVectorJTransformedCoords);
            
            RealPoint unitVectorITransformed = new RealPoint(unitVectorITransformedCoords[0], unitVectorITransformedCoords[1], 0);
            RealPoint unitVectorI = new RealPoint(unitVectorICoords[0], unitVectorICoords[1], 0);
            RealPoint unitVectorJTransformed = new RealPoint(unitVectorJTransformedCoords[0], unitVectorJTransformedCoords[1], 0);
            
            // compute rotation and oblique angles, using cross product to determine sign
            double rotationAngle = RealPoint.angleBetween(unitVectorI, unitVectorITransformed);
            if (unitVectorITransformed.y < 0)
                rotationAngle = -rotationAngle;
            
            double obliqueAngle = RealPoint.angleBetween(unitVectorITransformed, unitVectorJTransformed) - Math.PI/2;
            if (unitVectorJTransformed.x * unitVectorITransformed.y - unitVectorJTransformed.y * unitVectorITransformed.x < 0)
                obliqueAngle = -obliqueAngle;
            
            // convert to degrees
            rotationAngle = (180/Math.PI) * rotationAngle;
            obliqueAngle = (180/Math.PI) * obliqueAngle;
            
            
            DXFText text = new DXFText(string, new RealPoint(coords[0], coords[1], 0), rotationAngle, obliqueAngle, style, this);
            dxfDocument.addEntity(text);
        }
    }

    @Override
    public void drawString(AttributedCharacterIterator charIterator, float x, float y)
    {
        if (charIterator == null)
        {
            throw new NullPointerException();
        }
        else
        {
            // Just use String method
            StringBuffer stringBuffer = new StringBuffer();
            
            for(char c = charIterator.first(); c != CharacterIterator.DONE; c = charIterator.next()) 
            {
                stringBuffer.append(c);
            }
            
            drawString(stringBuffer.toString(), x, y);
        }
    }

    
    /**
     * Fill the specified shape with the current foreground color.
     * 
     * @param   shape  shape to be filled
     */
    @Override
    public void fill(Shape shape)
    {
        fill(shape, this.getColor());
    }
    
    
    /**
     * Clear the specified shape by filling with the current background color.
     * 
     * @param   shape  shape to be cleared
     */
    public void clear(Shape shape)
    {
        fill(shape, this.getBackground());
    }
    
    
    /**
     * Fill the specified shape with the specified color.
     * 
     * @param   shape  shape to be filled
     * @param   color  color to be used to fill shape
     */
    public void fill(Shape shape, Color color)
    {
        
        // construct boundary; handle circle and ellipse separately since DXF has special boundary types
        // for these, while Java PathIterator surprisingly doesn't have circular arcs as primitives
        
        Vector<Vector<DXFEntity>> boundaries = new Vector<Vector<DXFEntity>>();
        
        // general approach: get a PathIterator, and draw each returned path segment
        PathIterator iterator = shape.getPathIterator(new AffineTransform());
        double currentX = 0;
        double currentY = 0;
        double initialX = 0;
        double initialY = 0;
        
        // first step has to be a MOVETO, to set initial point
        double[] coords = new double[6]; 
        int curveType = iterator.currentSegment(coords);
        
        if (curveType == PathIterator.SEG_MOVETO)
        {
            // first boundary curve
            Vector<DXFEntity> boundary = new Vector<DXFEntity>();
            boundaries.add(boundary);
            
            //graphicsMatrix.transform(coords, 0, coords, 0, 2);
            
            initialX = coords[0];
            initialY = coords[1];
            
            currentX = coords[0];
            currentY = coords[1];
            
            iterator.next();
            
            while (!iterator.isDone())
            {
               curveType = iterator.currentSegment(coords);
                
                switch (curveType)
                {
                    case PathIterator.SEG_MOVETO:
                    {
                        // consider this the start of a new boundary curve
                        boundary = new Vector<DXFEntity>();
                        boundaries.add(boundary);
                        
                        //graphicsMatrix.transform(coords, 0, coords, 0, 2);
                        initialX = coords[0];
                        initialY = coords[1];
                        
                        currentX = coords[0];
                        currentY = coords[1];
                        break;
                    }
                    
                    case PathIterator.SEG_LINETO:
                    {
                        // transform the coordinates using the current graphics transform matrix
                        double[] points = new double[]{currentX, currentY, coords[0], coords[1]};
                        graphicsMatrix.transform(points, 0, points, 0, 2);
                        
                        DXFLine line = new DXFLine(new RealPoint(points[0], points[1], 0), new RealPoint(points[2], points[3], 0), this);
                        boundary.add(line);
                        
                        currentX = coords[0];
                        currentY = coords[1];
                        break;
                    }
                    
                    case PathIterator.SEG_QUADTO:
                    {
                        double[] controlPoints = {currentX /*X1*/, currentY /*Y1*/, coords[0] /*CtrlX*/, coords[1] /*CtrlY*/, coords[2] /*X2*/, coords[3] /*Y2*/};
                        double[] knots = {0,0,0,1,1,1};
                        int degree = 2;
                        
                        graphicsMatrix.transform(controlPoints, 0, controlPoints, 0, 3);
                        DXFSpline spline = new DXFSpline(degree, controlPoints, knots, this);
                        boundary.add(spline);
                        
                        /*
                        Vector<SplineControlPoint> transformedControlPoints = new Vector<SplineControlPoint>();
                        
                        // transform the control points according to the current transform matrix
                        for (int i = 0; i < controlPoints.length/2; i++)
                        {
                            double[] controlPointCoords = new double[]{controlPoints[2*i], controlPoints[2*i+1]};
                            graphicsMatrix.transform(controlPointCoords, 0, controlPointCoords, 0, 1);
                            SplineControlPoint transformedControlPoint = new SplineControlPoint(controlPointCoords[0], controlPointCoords[1], 0, multiplicities[i]);
                            transformedControlPoints.add(transformedControlPoint);
                        }
                        
                        DXFSpline spline = new DXFSpline(degree, transformedControlPoints, true, this);
                        boundary.add(spline);
                        */
                        
                        currentX = coords[2];
                        currentY = coords[3];
                        break;
                    }
                    
                    case PathIterator.SEG_CUBICTO:
                    {
                        double[] controlPoints = {currentX /*X1*/, currentY /*Y1*/, coords[0] /*CtrlX1*/, coords[1] /*CtrlY1*/, coords[2] /*CtrlX2*/, coords[3] /*CtrlY2*/, coords[4] /*X2*/, coords[5] /*Y2*/};
                        double[] knots = {0,0,0,0,1,1,1,1};
                        int degree = 3;
                        
                        graphicsMatrix.transform(controlPoints, 0, controlPoints, 0, 4);
                        DXFSpline spline = new DXFSpline(degree, controlPoints, knots, this);
                        boundary.add(spline);
                                                
                        currentX = coords[4];
                        currentY = coords[5];
                        break;
                    }
                    
                    case PathIterator.SEG_CLOSE:
                    {
                        // add a line if we're not already closed
                        if ((currentX != initialX) || (currentY != initialY))
                        {
                            double[] points = new double[]{currentX, currentY, initialX, initialY};
                            graphicsMatrix.transform(points, 0, points, 0, 2);
                            
                            DXFLine line = new DXFLine(new RealPoint(points[0], points[1], 0), new RealPoint(points[2], points[3], 0), this);
                            boundary.add(line);
                        }
                        
                        break;
                    }
                    
                    default:
                    {
                        // do nothing
                        break;
                    }
                }
                
                iterator.next();
                
            }
        
        }
        
        // add Hatch with the above boundary
        DXFHatch hatch = new DXFHatch(boundaries, color);
        dxfDocument.addEntity(hatch);
        
    }

    
    /**
     * Return the current background color.
     * 
     * @return  current background color
     */
    @Override
    public Color getBackground()
    {
        return backColor;
    }

    
    /**
     * Returns null; Composite not supported.
     */
    @Override
    public Composite getComposite()
    {
        // No composite
        return null;
    }

    
    /**
     * Returns null; GraphicsConfiguration not supported.
     */
    @Override
    public GraphicsConfiguration getDeviceConfiguration()
    {
        // Just return null
        return null;
    }

    
    /**
     * Returns null; Font render context needed for Font sizing; use from current graphics environment.
     */
    @Override
    public FontRenderContext getFontRenderContext()
    {
        // No font rendering context
        //return null;
        return GraphicsEnvironment.getLocalGraphicsEnvironment().createGraphics(new BufferedImage(100, 100, BufferedImage.TYPE_4BYTE_ABGR)).getFontRenderContext();
    }

    
    /**
     * Just return the current foreground color object.
     * 
     * @return  current foreground color
     */
    @Override
    public Paint getPaint()
    {
        // Return our color object
        return color;
    }

    
    /**
     * Return null; rendering hints not supported.
     */
    @Override
    public Object getRenderingHint(Key arg0)
    {
        // Return null
        return null;
    }

    
    /**
     * Return null; rendering hints not supported.
     */
    @Override
    public RenderingHints getRenderingHints()
    {
        // Just return null
        return null;
    }

    
    /**
     * Return our current pen stroke.
     * 
     * @return  current pen stroke
     */
    @Override
    public Stroke getStroke()
    {
        return stroke;
    }

    
    /**
     * Return a copy of the current graphics transformation being applied
     * to drawing and text operations.
     * 
     * @return  copy of current graphics transform
     */
    @Override
    public AffineTransform getTransform()
    {
        return new AffineTransform(javaTransformMatrix);
    }

    /**
     * Not supported; return false.
     */
    @Override
    public boolean hit(Rectangle arg0, Shape arg1, boolean arg2)
    {
        // Just return false
        return false;
    }

    
    /**
     * Apply the specified rotation to the current graphics transformation. Subsequent
     * graphics operations will have the new combined transformation applied.
     * 
     * @param   radians  rotation in radians
     */
    @Override
    public void rotate(double radians)
    {
        javaTransformMatrix.rotate(radians);
        setMatrix();
    }

    
    /**
     * Apply the specified rotation to the current graphics transformation, but first translating
     * by (x,y) and after translating back by (-x, -y). Subsequent graphics operations will have 
     * the new combined transformation applied.
     * 
     * @param   radians  rotation in radians
     * @param   x  x coordinate of pre- and post-translation
     * @param   y  y coordinate of pre- and post-translation
     */
    @Override
    public void rotate(double radians, double x, double y)
    {
        translate(x,y);
        rotate(radians);
        translate(-x,-y);
    }

    
    /**
     * Apply the specified scaling to the current graphics transformation. Subsequent
     * graphics operations will have the new combined transformation applied.
     * 
     * @param   sx  amount to scale x coordinates
     * @param   sy  amount to scale y coordinates
     */
    @Override
    public void scale(double sx, double sy)
    {
        javaTransformMatrix.scale(sx, sy);
        setMatrix();
    }

    
    /**
     * Set the background color used in clear operations to the specified color
     * 
     * @param   color  color to use for background
     */
    @Override
    public void setBackground(Color color)
    {
        backColor = color;
    }

    
    /**
     * Do nothing - composite not supported.
     */
    @Override
    public void setComposite(Composite arg0)
    {
        // Do nothing
    }

    
    /**
     * Use to set foreground color if instance of Color, otherwise do nothing.
     * 
     * @param   paint  paint object; only used if it's an instance of Color
     */
    @Override
    public void setPaint(Paint paint)
    {
        // Use to set Color if it's a color subclass, otherwise do nothing
        if (paint instanceof Color)
        {
            this.color = (Color)paint;
        }
        
    }

    
    /**
     * Do nothing; rendering hints not supported.
     */
    @Override
    public void setRenderingHint(Key arg0, Object arg1)
    {
        // Do nothing
    }

    
    /**
     * Do nothing; rendering hints not supported.
     */
    @Override
    public void setRenderingHints(Map<?, ?> arg0)
    {
        // Do nothing
    }

    
    /**
     * Set stroke if instance of BasicStroke, otherwise throw UnsupportedOperationException.
     * 
     * @param   stroke  stroke to use (must be BasicStroke)
     */
    @Override
    public void setStroke(Stroke stroke)
    {
        if (stroke instanceof BasicStroke)
        {
            this.stroke = (BasicStroke)stroke;
        }
        else
        {
            throw new UnsupportedOperationException("Only BasicStroke supported by DXFGraphics");
        }
    }

    
    /**
     * Set current graphics transformation to that supplied.
     * 
     * @param   newTransform  new graphics transformation to be used.
     */
    @Override
    public void setTransform(AffineTransform newTransform)
    {
        javaTransformMatrix = new AffineTransform(newTransform);
        setMatrix();
    }

    
    /**
     * Apply the specified shearing to the current graphics transformation. Subsequent
     * graphics operations will have the new combined transformation applied.
     * 
     * @param   sx  amount to shear x coordinates
     * @param   sy  amount to shear y coordinates
     */
    @Override
    public void shear(double sx, double sy)
    {
        javaTransformMatrix.shear(sx, sy);
        setMatrix();
    }

    
    /**
     * Concatenate the supplied transformation to the current graphics transformation. Subsequent
     * graphics operations will have the new combined transformation applied.
     */
    @Override
    public void transform(AffineTransform transform)
    {
        javaTransformMatrix.concatenate(transform);
        setMatrix();
    }
    
    
    /**
     * Translates the origin of the graphics context to the point
     * (<i>x</i>,&nbsp;<i>y</i>) in the current coordinate system.
     * Modifies this graphics context so that its new origin corresponds
     * to the point (<i>x</i>,&nbsp;<i>y</i>) in this graphics context's
     * original coordinate system.  All coordinates used in subsequent
     * rendering operations on this graphics context will be relative
     * to this new origin.
     * @param  x   the <i>x</i> coordinate.
     * @param  y   the <i>y</i> coordinate.
     */
    public void translate(int x, int y)
    {
        translate((double)x, (double)y);
    }

    
    /**
     * Translates the origin of the graphics context to the point
     * (<i>x</i>,&nbsp;<i>y</i>) in the current coordinate system.
     * Modifies this graphics context so that its new origin corresponds
     * to the point (<i>x</i>,&nbsp;<i>y</i>) in this graphics context's
     * original coordinate system.  All coordinates used in subsequent
     * rendering operations on this graphics context will be relative
     * to this new origin.
     * @param  x   the <i>x</i> coordinate.
     * @param  y   the <i>y</i> coordinate.
     */
    @Override
    public void translate(double x, double y)
    {
        javaTransformMatrix.translate(x, y);
        setMatrix();
    }
    
    
    /**
     * Utility method to map a set of vectors using an Affine transformation; basically ignores translation.
     * @param   m         affine transformation to use
     * @param   vectors   array [x1, y1, x2, y2, ..., xn, yn] of (x,y) coordinates of vectors to be mapped
     */
    private void mapVectors(AffineTransform m, double[] vectors)
    {
     // create a version of the transform without the translation
        double[] matrixCoeffs = {0,0,0,0,0,0};
        m.getMatrix(matrixCoeffs);
        // zero out translation components
        matrixCoeffs[4] = 0;
        matrixCoeffs[5] = 0;
        AffineTransform l = new AffineTransform(matrixCoeffs);
        
        l.transform(vectors, 0, vectors, 0, vectors.length/2);
    }
    
    
}
