package com.qunhe.util.nest.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.qunhe.util.nest.config.Config;
import com.qunhe.util.nest.data.NestPath;
import com.qunhe.util.nest.data.Placement;
import com.qunhe.util.nest.util.SvgUtil;


import com.qunhe.util.nest.data.NestPath;

class guiUtil {
	
	
	 static List<NestPath> transferSvgIntoPolygons() throws DocumentException {
	        List<NestPath> nestPaths = new ArrayList<>();
	        SAXReader reader = new SAXReader();
	        Document document = reader.read("input/test.xml");
	        List<Element> elementList = document.getRootElement().elements();
	        int count = 0;
	        for (Element element : elementList) {
	            count++;
	            if ("polygon".equals(element.getName())) {
	                String datalist = element.attribute("points").getValue();
	                NestPath polygon = new NestPath();
	                for (String s : datalist.split(" ")) {
	                    s = s.trim();
	                    if (s.indexOf(",") == -1) {
	                        continue;
	                    }
	                    String[] value = s.split(",");
	                    double x = Double.parseDouble(value[0]);
	                    double y = Double.parseDouble(value[1]);
	                    polygon.add(x, y);
	                }
	                polygon.bid = count;
	                polygon.setRotation(4);
	                nestPaths.add(polygon);
	            } else if ("rect".equals(element.getName())) {
	                double width = Double.parseDouble(element.attribute("width").getValue());
	                double height = Double.parseDouble(element.attribute("height").getValue());
	                double x = Double.parseDouble(element.attribute("x").getValue());
	                double y = Double.parseDouble(element.attribute("y").getValue());
	                NestPath rect = new NestPath();
	                rect.add(x, y);
	                rect.add(x + width, y);
	                rect.add(x + width, y + height);
	                rect.add(x, y + height);
	                rect.bid = count;
	                rect.setRotation(4);
	                nestPaths.add(rect);
	            }
	        }
	        return nestPaths;
	    }
	 
	 
	 static void showError(String msg, JFrame frame, JLabel label)
	 {
		 JOptionPane.showMessageDialog(frame,msg,"error",JOptionPane.ERROR_MESSAGE);
		 label.setText(msg);
		 label.setForeground(Color.RED);
	 }
	 
	 static void setMessageLabel(String msg, JLabel label)
	 {
		 label.setText(msg);
		 label.setForeground(Color.BLACK);
	 }
	 
	 static void saveSvgFile(List<String> strings, String htmlfile, double binwidth, double binheight) throws Exception {
	        File f = new File(htmlfile);
	        if (!f.exists()) {
	            f.createNewFile();
	        }
	        Writer writer = new FileWriter(f, false);
	        writer.write("<?xml version=\"1.0\" standalone=\"no\"?>\n" +
	                "\n" +
	                "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \n" +
	                "\"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n" +
	                " \n" +
	                //"<svg width=\"100%\" height=\"100%\" version=\"1.1\"\n" +
	                //added for correct "AUTOZOOM"
	                "<svg width=\"" + binwidth + "\" height=\"" + binheight + "\" viewBox=\"0 0 " + binwidth + " " +  binheight +"\" version=\"1.1\"\n" +

	                
	                "xmlns=\"http://www.w3.org/2000/svg\">\n");
	        for(String s : strings){
	            writer.write(s);
	        }
	        writer.write("</svg>");
	        writer.close();
	    }

}
