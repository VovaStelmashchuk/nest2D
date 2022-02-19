package com.qunhe.util.nest;


import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.Test;

import com.qunhe.util.nest.Nest.ListPlacementObserver;
import com.qunhe.util.nest.Nest.ResultObserver;
import com.qunhe.util.nest.config.Config;
import com.qunhe.util.nest.data.NestPath;
import com.qunhe.util.nest.data.Placement;
import com.qunhe.util.nest.data.Result;
import com.qunhe.util.nest.util.SvgUtil;


public class NestTest {


    @Test
    public void HoleTest() throws Exception {
        NestPath binPolygon = new NestPath();
        double width = 400;
        double height = 400;
        binPolygon.add(0, 0);
        binPolygon.add(0, height);
        binPolygon.add(width, height);
        binPolygon.add(width, 0);
        NestPath outer = new NestPath();
        outer.add(600, 0);
        outer.add(600, 200);
        outer.add(800, 200);
        outer.add(800, 0);
        outer.setRotation(0);
        assert outer.getBid() == 1;
        NestPath inner = new NestPath();
        inner.add(650, 50);
        inner.add(650, 150);
        inner.add(750, 150);
        inner.add(750, 50);
        assert inner.getBid() == 2;
        //inner.setBid(2);
        NestPath little = new NestPath();
        little.add(900, 0);
        little.add(870, 20);
        little.add(930, 20);
        assert little.getBid() == 3;
        //little.setBid(3);
        little.setRotation(4);
        List<NestPath> list = new ArrayList<>();
        list.add(inner);
        list.add(outer);
        list.add(little);
        Config config = new Config();
        config.USE_HOLE = true;
        Nest nest = new Nest(binPolygon, list, config, 10);
        List<List<Placement>> appliedPlacement = nest.startNest();
        List<String> strings = SvgUtil.svgGenerator(list, appliedPlacement, width, height);
        for (String s : strings) {
            System.out.println(s);
        }
    }


    @Test
    public void testSample() throws Exception {
        List<NestPath> polygons = transferSvgIntoPolygons();
        NestPath bin = new NestPath();
        double binWidth = 1511.822;
        double binHeight = 339.235;
        bin.add(0, 0);
        bin.add(binWidth, 0);
        bin.add(binWidth, binHeight);
        bin.add(0, binHeight);
        bin.setBid(-1);
        Config config = new Config();
        config.SPACING = 0;
        config.POPULATION_SIZE = 6;
        Nest nest = new Nest(bin, polygons, config, 1);
        
        List<List<Placement>> appliedPlacement = nest.startNest();
        List<String> strings = SvgUtil.svgGenerator(polygons, appliedPlacement, binWidth, binHeight);
        saveSvgFile(strings,Config.OUTPUT_DIR+"problem.html");
        // find solution
        nest = new Nest(bin, polygons, config, 10);
        nest.observers.add(new ListPlacementObserver() {			
			@Override
			public void populationUpdate(List<List<Placement>> appliedPlacement) {
				System.out.println(" new placement");				
			}
		});  
        nest.resultobservers.add(new ResultObserver() {
			@Override
			public void muationStepDone(Result result) {
				System.out.println("fitness " + result.fitness + " area " + result.area);
			}
		});        
        appliedPlacement = nest.startNest();
        strings = SvgUtil.svgGenerator(polygons, appliedPlacement, binWidth, binHeight);
        saveSvgFile(strings,Config.OUTPUT_DIR+"solution.html");
    }

    private static List<NestPath> transferSvgIntoPolygons() throws DocumentException {
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
                polygon.setBid(count);
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
                rect.setBid(count);
                rect.setRotation(4);
                nestPaths.add(rect);
            }
        }
        return nestPaths;
    }

    private void saveSvgFile(List<String> strings, String htmlfile) throws Exception {
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
                "<svg width=\"100%\" height=\"100%\" version=\"1.1\"\n" +
                "xmlns=\"http://www.w3.org/2000/svg\">\n");
        for(String s : strings){
            writer.write(s);
        }
        writer.write("</svg>");
        writer.close();
    }

}
