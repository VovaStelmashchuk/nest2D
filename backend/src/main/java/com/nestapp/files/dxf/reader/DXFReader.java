package com.nestapp.files.dxf.reader;

import java.awt.*;
import java.awt.geom.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.*;

/*
 *  This code implements a simple DXF file parser that can read many 2D DXF files containing POLYLINE and SPLINE
 *  outlines such as thoes used for embroidery patterns and input to machines like Silhouette paper cutters.
 *  It's designed to convert POLYLINE and SPLINE sequences into an array of Path2D.Double objects from Java's
 *  geom package.
 *  The parser assumes that DXF file's units are inches, but you can pass the parser a maximum
 *  size value and it will scale down the converted shape so that its maximum dimension fits within this limit.
 *  The code also contains a simple viewer app you can run to try it out on a DXF file.
 *
 *  I've tested this code with a variety of simple, 2D DXF files and it's able to read most of them.  However,
 *  the DXF file specification is very complex and I have only implemented a subset of it, so I cannot guarantee
 *  that this code will read all 2D DXF files.  Some instance variables are placeholders for features that have
 *  yet to be implmenented.
 *
 *  I'm publishing this source code under the MIT License (See: https://opensource.org/licenses/MIT)
 *
 *  Copyright 2017 Wayne Holder
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 *  documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 *  the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 *  to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 *  the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 *  THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 *  TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

public class DXFReader {
    private static final boolean DEBUG = false;
    private static final boolean INFO = false;
    public ArrayList<Entity> entities = new ArrayList<>();
    private ArrayList<Entity> stack = new ArrayList<>();
    private Map<String, Block> blockDict = new TreeMap<>();
    private Entity cEntity = null;

    class Section extends Entity {
        private Map<String, Map<Integer, String>> attributes = new TreeMap<>();
        private Map<Integer, String> attValues;
        private String sType;

        Section(String type) {
            super(type);
        }

        @Override
        public void addParam(int gCode, String value) {
            if (gCode == 2 && sType == null) {
                sType = value;
            } else if (gCode == 9) {
                attValues = new HashMap<>();
                attributes.put(value, attValues);
            } else if (attValues != null) {
                attValues.put(gCode, value);
            }
        }
    }

    /**
     * Crude implementation of TEXT using GlyphVector to create vector outlines of text
     * Note: this code should use, or support vector fonts such as those by Hershey
     */
    class Text extends Entity implements AutoPop {
        private Canvas canvas = new Canvas();
        private double ix, iy, ix2, iy2, textHeight, rotation;
        private int hAdjust, vAdjust;
        private String text;

        Text(String type) {
            super(type);
        }

        @Override
        public void addParam(int gCode, String value) {
            switch (gCode) {
                case 1:                                       // Text string
                    // Process Control Codes and Special Chars
                    // https://forums.autodesk.com/t5/autocad-forum/text-commands-eg-u/td-p/1977654
                    StringBuilder buf = new StringBuilder();
                    for (int ii = 0; ii < value.length(); ii++) {
                        char cc = value.charAt(ii);
                        if (cc == '%') {
                            cc = value.charAt(ii + 2);
                            ii += 2;
                            if (Character.isDigit(cc)) {
                                int code = 0;
                                while (Character.isDigit(cc = value.charAt(ii))) {
                                    code = (code * 10) + (cc - '0');
                                    ii++;
                                }
                                // todo: how to convert value of "code" into special character
                                buf.append("\uFFFD");                 // Insert Unicode "unknown character" symbol
                                ii--;
                            } else {
                                switch (cc) {
                                    case 'u':                             // Toggles underscoring on and off
                                        // Ignored
                                        break;
                                    case 'd':                             // Draws degrees symbol (°)
                                        buf.append("\u00B0");
                                        break;
                                    case 'p':                             // Draws plus/minus tolerance symbol (±)
                                        buf.append("\u00B1");
                                        break;
                                    case
                                        'c':                             // Draws circle diameter dimensioning symbol (Ø)
                                        buf.append("\u00D8");
                                        break;
                                    case 'o':                             // Toggles overscoring on and off
                                        // Ignored
                                        break;
                                }
                            }
                        } else {
                            buf.append(cc);
                        }
                    }
                    text = buf.toString();
                    break;
                case 10:                                      // Insertion X
                    ix = Double.parseDouble(value);
                    break;
                case 11:                                      // Second alignment point X
                    ix2 = Double.parseDouble(value);
                    break;
                case 20:                                      // Insertion Y
                    iy = Double.parseDouble(value);
                    break;
                case 21:                                      // Second alignment point Y
                    iy2 = Double.parseDouble(value);
                    break;
                case 40:                                      // Nominal (initial) text height
                    textHeight = Double.parseDouble(value);
                    break;
                case 50:                                      // Rotation angle in degrees
                    rotation = Double.parseDouble(value);
                    break;
                case 71:                                      // Text generation flags (optional, default = 0):
                    // Not implemented
                    // 2 = Text is backward (mirrored in X)
                    // 4 = Text is upside down (mirrored in Y)
                    break;
                case
                    72:                                      // Horizontal text justification type (optional, default = 0) integer codes
                    //0 = Left; 1= Center; 2 = Right
                    //3 = Aligned (if vertical alignment = 0)
                    //4 = Middle (if vertical alignment = 0)
                    //5 = Fit (if vertical alignment = 0)
                    hAdjust = Integer.parseInt(value);
                    break;
                case
                    73:                                      // Vertical text justification type (optional, default = 0): integer codes
                    // 0 = Baseline; 1 = Bottom; 2 = Middle; 3 = Top
                    vAdjust = Integer.parseInt(value);
                    break;
            }
        }
    }

    /**
     * Crude implementation of MTEXT (Multi-line Text) using GlyphVector to create vector outline of text
     * Note: the MTEXT spec is very complex and assumes the ability to decode embedded format codes, use vector fonts
     * such as those by Hershey, and other features I have not implemented.
     * https://knowledge.safe.com/articles/38908/autocad-workflows-reading-and-writing-text-mtext-f.html
     * <p>
     * Example Text with Format Codes: https://adndevblog.typepad.com/autocad/2017/09/dissecting-mtext-format-codes.html
     * \A1;3'-1"
     * \A1;6'-10{\H0.750000x;\S1/2;}"
     * \A1;PROVIDE 20 MIN. DOOR\PW/ SELF CLOSING HINGES
     * {\Farchquik.shx|c0;MIN. 22"x 30" ATTIC ACCESS}
     * "HEATILATOR" 42" GAS BURNING DIRECT VENT FIREPLACE, OR EQUAL
     * BOLLARD,\PFOR W.H.\PPROTECTION
     */
    class MText extends Entity implements AutoPop {
        private Canvas canvas = new Canvas();
        private String text;
        private double ix, iy, textHeight, refWidth, xRot, yRot;
        private int attachPoint;

        MText(String type) {
            super(type);
        }

        @Override
        public void addParam(int gCode, String value) {
            switch (gCode) {
                case 1:                                         // Text string
                    // Process Format Codes (most are ignored)
                    List<String> lines = new ArrayList<>();
                    StringBuilder buf = new StringBuilder();
                    for (int jj = 0; jj < value.length(); jj++) {
                        char cc = value.charAt(jj);
                        if (cc == '\\') {
                            cc = value.charAt(++jj);
                            switch (cc) {
                                case 'A':                               // Alignment
                                case 'C':                               // Color
                                case 'F':                               // Font file name
                                case 'H':                               // Text height
                                case 'Q':                               // Slanting (obliquing) text by angle
                                case 'S':                               // Stacking Fractions
                                case 'T':                               // Tracking, char.spacing - e.g. \T2;
                                case 'W':                               // Text width
                                    int tdx = value.indexOf(";", jj);
                                    String val = value.substring(jj + 1, tdx);
                                    jj = tdx;
                                    if (cc == 'S') {                      // Stacking Fractions (1/2, 1/3, etc)
                                        if ("1/2".equals(val)) {
                                            buf.append("\u00BD");             // Unicode for 1/2
                                        } else if ("1/3".equals(val)) {
                                            buf.append("\u2153");           // Unicode for 1/3
                                        } else if ("1/4".equals(val)) {
                                            buf.append("\u00BC");             // Unicode for 1/4
                                        } else if ("2/3".equals(val)) {
                                            buf.append("\u2154");             // Unicode for 2/3
                                        } else if ("3/4".equals(val)) {
                                            buf.append("\u00BE");             // Unicode for 3/4
                                        } else {
                                            String[] parts = val.split("/");
                                            if (parts.length == 2) {
                                                buf.append(parts[0]);
                                                buf.append("\u2044");
                                                buf.append(parts[1]);
                                            }
                                        }
                                    }
                                    break;
                                case 'P':                               // New paragraph (new line)
                                    lines.add(buf.toString());
                                    buf.setLength(0);
                                    break;
                                case '\\':                              // Escape character - e.g. \\ = "\", \{ = "{"
                                    buf.append(value.charAt(++jj));
                                    break;
                            }
                        } else if (cc == '{') {
                            // Begin area influenced by special code
                        } else if (cc == '}') {
                            // End area influenced by special code
                        } else {
                            buf.append(cc);
                        }
                    }
                    lines.add(buf.toString());
                    // Skip handling all but first line of text
                    text = lines.get(0);
                    if (text.length() > 30 && refWidth > 0) {
                        // KLudge until code to handle "refWidth" is added
                        text = text.substring(0, 30) + "...";
                    }
                    break;
                case 7:                                       // Text style name (STANDARD if not provided) (optional)
                    break;
                case 10:                                      // Insertion X
                    ix = Double.parseDouble(value);
                    break;
                case 11:                                      // X Rotation Unit Vector
                    xRot = Double.parseDouble(value);
                    break;
                case 20:                                      // Insertion Y
                    iy = Double.parseDouble(value);
                    break;
                case 21:                                      // Y Rotation Unit Vector
                    yRot = Double.parseDouble(value);
                    break;
                case 40:                                      // Nominal (initial) text height
                    textHeight = Double.parseDouble(value);
                    break;
                case 41:                                      // Reference rectangle width
                    refWidth = Double.parseDouble(value);
                    break;
                case 71:                                      // Attachment point
                    attachPoint = Integer.parseInt(value);
                    break;
                case
                    72:                                      // Drawing direction: 1 = Left to right; 3 = Top to bottom; 5 = By style
                    break;
            }
        }
    }

    class Block extends Entity {
        private List<Entity> entities = new ArrayList<>();
        private double baseX, baseY;
        private int flags;

        Block(String type) {
            super(type);
        }

        @Override
        public void addParam(int gCode, String value) {
            switch (gCode) {
                case 2:                                       // Block name
                    blockDict.put(value, this);
                    break;
                case 5:                                       // Block handle
                    break;
                case 10:                                      // Base Point X
                    baseX = Double.parseDouble(value);
                    break;
                case 20:                                      // Base Point Y
                    baseY = Double.parseDouble(value);
                    break;
                case 70:                                      // Flags
                    flags = Integer.parseInt(value);
                    break;
            }
        }

        void addEntity(Entity entity) {
            entities.add(entity);
        }
    }

    // TODO: implement when I understand how this is supposed to work...
    class Hatch extends Entity implements AutoPop {
        Hatch(String type) {
            super(type);
        }
    }

    /*
     * Note: code for "DIMENSION" is incomplete
     */
    class Dimen extends Entity implements AutoPop {
        private String blockHandle, blockName;
        private double ax, ay, mx, my;
        private int type, orientation;

        Dimen(String type) {
            super(type);
        }

        @Override
        public void addParam(int gCode, String value) {
            switch (gCode) {
                case 2:                                     // Name of Block to with Dimension graphics
                    blockName = value;
                    break;
                case 5:                                     // Handle of Block to with Dimension graphics
                    blockHandle = value;
                    break;
                case 10:                                    // Definition Point X
                    ax = Double.parseDouble(value);
                    break;
                case 20:                                    // Definition Point Y
                    ay = Double.parseDouble(value);
                    break;
                case 11:                                    // Mid Point X
                    mx = Double.parseDouble(value);
                    break;
                case 21:                                    // Mid Point Y
                    my = Double.parseDouble(value);
                    break;
                case 70:                                    // Dimension type (0-6 plus bits at 32,64,128)
                    type = Integer.parseInt(value);
                    break;
                case 71:                                    // Attachment orientation (1-9) for 1=UL, 2=UC, 3=UR, etc
                    orientation = Integer.parseInt(value);
                    break;
            }
        }
    }

    /**
     * Crude implementation of ELLIPSE
     */
    class Ellipse extends Entity implements AutoPop {
        RectangularShape ellipse;
        private double cx, cy, mx, my, ratio, start, end;

        Ellipse(String type) {
            super(type);
        }

        @Override
        public void addParam(int gCode, String value) {
            switch (gCode) {
                case 10:                                  // Center Point X1
                    cx = Double.parseDouble(value);
                    break;
                case 11:                                  // Endpoint of major axis X
                    mx = Double.parseDouble(value);
                    break;
                case 20:                                  // Center Point Y2
                    cy = Double.parseDouble(value);
                    break;
                case 21:                                  // Endpoint of major axis Y
                    my = Double.parseDouble(value);
                    break;
                case 40:                                  // Ratio of minor axis to major axis
                    ratio = Double.parseDouble(value);
                    break;
                case 41:                                  // Start parameter (this value is 0.0 for a full ellipse)
                    start = Double.parseDouble(value);
                    break;
                case 42:                                  // End parameter (this value is 2pi for a full ellipse)
                    end = Double.parseDouble(value);
                    break;
            }
        }

        @Override
        public void close() {
            if (start != 0 || end != 0) {
                ellipse = new Arc2D.Double();
                double startAngle = Math.toDegrees(start);
                double endAngle = Math.toDegrees(end);
                // Make angle negative so it runs clockwise when using Arc2D.Double
                ((Arc2D.Double) ellipse).setAngleStart(-startAngle);
                double extent = startAngle - (endAngle < startAngle ? endAngle + 360 : endAngle);
                ((Arc2D.Double) ellipse).setAngleExtent(extent);
            } else {
                ellipse = new Ellipse2D.Double();
            }
            double hoff = Math.abs(Math.sqrt(mx * mx + my * my));
            double voff = Math.abs(hoff * ratio);
            ellipse.setFrame(-hoff, -voff, hoff * 2, voff * 2);
            double angle = Math.atan2(my, mx);
            AffineTransform at = new AffineTransform();
            at.translate(cx, cy);
            at.rotate(angle);
        }
    }


    private void push() {
        stack.add(cEntity);
    }

    private void pop() {
        if (cEntity != null) {
            cEntity.close();
        }
        cEntity = stack.remove(stack.size() - 1);
    }

    private void addChildToTop(Entity child) {
        if (stack.size() > 0) {
            Entity top = stack.get(stack.size() - 1);
            if (top != null) {
                top.addChild(child);
            }
        }
    }

    private void addEntity(Entity entity) {
        if (cEntity instanceof Block) {
            Block block = (Block) cEntity;
            if (entity instanceof Insert && (block.flags & 2) != 0) {
                push();
                entities.add(entity);
                cEntity = entity;
            } else {
                push();
                block.addEntity(entity);
                cEntity = entity;
            }
        } else {
            push();
            entities.add(entity);
            cEntity = entity;
        }
    }

    private void debugPrint(String value) {
        for (int ii = 0; ii < stack.size(); ii++) {
            System.out.print("  ");
        }
        System.out.println(value);
    }

    public void parseFile(InputStream stream) {
        stack = new ArrayList<>();
        cEntity = null;
        Scanner lines = new Scanner(stream);
        parseFile(lines);
    }

    private void parseFile(Scanner lines) {
        while (lines.hasNextLine()) {
            String line = lines.nextLine().trim();
            String value = lines.nextLine().trim();
            int gCode = Integer.parseInt(line);
            if (gCode == 0) {                             // Entity type
                if (cEntity instanceof AutoPop) {
                    pop();
                }
                if (DEBUG) {
                    debugPrint(value);
                }
                switch (value) {
                    case "SECTION":
                        cEntity = new Section(value);
                        break;
                    case "ENDSEC":
                        cEntity = null;
                        stack.clear();
                        break;
                    case "TABLE":
                        push();
                        cEntity = new Entity(value);
                        break;
                    case "ENDTAB":
                        pop();
                        break;
                    case "BLOCK":
                        push();
                        cEntity = new Block(value);
                        break;
                    case "ENDBLK":
                        pop();
                        while ("BLOCK".equals(cEntity.type)) {
                            pop();
                        }
                        break;
                    case "SPLINE":
                        addEntity(new Spline(value));
                        break;
                    case "INSERT":
                        addEntity(new Insert(value));
                        break;
                    case "TEXT":
                        addEntity(new Text(value));
                        break;
                    case "MTEXT":
                        addEntity(new MText(value));
                        break;
                    case "HATCH":
                        addEntity(new Hatch(value));
                        break;
                    case "CIRCLE":
                        addEntity(new Circle(value));
                        break;
                    case "ELLIPSE":
                        addEntity(new Ellipse(value));
                        break;
                    case "ARC":
                        addEntity(new Arc(value));
                        break;
                    case "LINE":
                        addEntity(new Line(value));
                        break;
                    case "DIMENSION":
                        addEntity(new Dimen(value));
                        break;
                    case "POLYLINE":
                        addEntity(new Polyline(value));
                        break;
                    case "LWPOLYLINE":
                        addEntity(new LwPolyline(value));
                        break;
                    case "VERTEX":
                        if (cEntity != null && !"VERTEX".equals(cEntity.type)) {
                            push();
                        }
                        addChildToTop(cEntity = new Vertex(value));
                        break;
                    case "SEQEND":
                        while (!stack.isEmpty() && !"BLOCK".equals(cEntity.type)) {
                            pop();
                        }
                        break;
                }
            } else {
                if (cEntity != null) {
                    if (DEBUG) {
                        debugPrint(gCode + ": " + value);
                    }
                    cEntity.addParam(gCode, value);
                }
            }
        }
    }
}
