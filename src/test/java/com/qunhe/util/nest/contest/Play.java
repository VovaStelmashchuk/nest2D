package com.qunhe.util.nest.contest;


import com.google.gson.Gson;
import com.qunhe.util.nest.Nest;
import com.qunhe.util.nest.data.NestPath;
import com.qunhe.util.nest.data.Placement;
import com.qunhe.util.nest.util.Config;
import com.qunhe.util.nest.util.IOUtils;
import com.qunhe.util.nest.util.SvgUtil;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;
import static com.qunhe.util.nest.util.IOUtils.*;

public class Play {
   

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
        outer.bid = 1;
        NestPath inner = new NestPath();
        inner.add(650, 50);
        inner.add(650, 150);
        inner.add(750, 150);
        inner.add(750, 50);
        inner.bid = 2;
        NestPath little = new NestPath();
        little.add(900, 0);
        little.add(870, 20);
        little.add(930, 20);
        little.bid = 3;
        little.setRotation(4);
        List<NestPath> list = new ArrayList<NestPath>();
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
    public void play() throws Exception {
        // Config
        Config config = new Config();
        config.SPACING = 0;
        config.POPULATION_SIZE = 5;
        config.MUTATION_RATE = 10;
        config.USE_HOLE = false;
        //config.CONCAVE=true;

        Config.CLIIPER_SCALE = 10000;
        Config.CURVE_TOLERANCE = 0.3; // Used to simplify polygons
        Config.IS_DEBUG = true;
        Config.BIN_WIDTH = 20000;
        Config.BIN_HEIGHT = 1000;
        Config.NB_ITERATIONS = 2;
        Config.BOUND_SPACING = 0;
        Config.ASSUME_NO_INNER_PARTS = true;
        Config.ASSUME_ALL_PARTS_PLACABLE = true;
        Config.INPUT_FILE = "./data/L0002_lingjian.csv";
        //Config.INPUT_FILE = "./data/L0003_lingjian.csv";
        //inputFile = "./data/debug";
        Config.OUTPUT_FILE = "./submit/DatasetA/L0002.csv";
        //Config.OUTPUT_FILE = "./submit/DatasetA/L0003.csv";
        Config.OUTPUT_DIR = "./out/";
        Config.LIMIT = 10;
        // We can choose to load nfp from file
        Config.NFP_CACHE_PATH=null;

        // Start ------------
        //List<NestPath> polygons = IOUtils.readFromContestFile(inputFile);
        IOUtils.log("Loading data from "+Config.INPUT_FILE);
        List<ContestData> datas = ContestData.readFromFile(Config.INPUT_FILE);
        List<NestPath> polygons = datas.stream().map(ContestData::getPolygon).collect(Collectors.toList());
        IOUtils.log(polygons.size()+" parts loaded.");
        Config.INPUT = datas;
        if(Config.LIMIT>0){
            polygons = polygons.subList(0,3);
        }

        NestPath bin = new NestPath();
        bin.add(0, 0);
        bin.add(Config.BIN_WIDTH, 0);
        bin.add(Config.BIN_WIDTH, Config.BIN_HEIGHT);
        bin.add(0, Config.BIN_HEIGHT);
        bin.bid = -1;

        Nest nest = new Nest(bin, polygons, config, Config.NB_ITERATIONS);
        List<List<Placement>> appliedPlacement = nest.startNest();
        IOUtils.log("Save to svg...");
        List<String> strings = SvgUtil.svgGenerator(polygons, appliedPlacement, Config.BIN_WIDTH, Config.BIN_HEIGHT);
        saveSvgFile(strings,Config.OUTPUT_FILE+".html");
        IOUtils.log("Save to 'submit' folder...");
        ContestData.writeToFile( Config.OUTPUT_FILE, appliedPlacement, datas);
    }

    @Test
    public void testSeri(){
        Map<String, Integer> m = new HashMap<>();
        m.put("test",5);
        Gson g =new Gson();
        String res = g.toJson(m, m.getClass());
        log(res);
        Gson g2 = new Gson();
        Map<String, Integer> m2 = g2.fromJson(res, m.getClass());
        log("Done");
    }

    @Test
    public void testSample() throws Exception {
        List<NestPath> polygons = transferSvgIntoPolygons();
        NestPath bin = new NestPath();
        double binWidth = 511.822;
        double binHeight = 339.235;
        bin.add(0, 0);
        bin.add(binWidth, 0);
        bin.add(binWidth, binHeight);
        bin.add(0, binHeight);
        bin.bid = -1;
        Config config = new Config();
        config.SPACING = 0;
        config.POPULATION_SIZE = 5;
        Nest nest = new Nest(bin, polygons, config, 2);
        List<List<Placement>> appliedPlacement = nest.startNest();
        List<String> strings = SvgUtil.svgGenerator(polygons, appliedPlacement, binWidth, binHeight);
        saveSvgFile(strings,"contest.html");
    }

    private List<NestPath> transferSvgIntoPolygons() throws DocumentException {
        List<NestPath> nestPaths = new ArrayList<>();
        SAXReader reader = new SAXReader();
        Document document = reader.read("test.xml");
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

    private void saveSvgFile(List<String> strings, String file) throws Exception {
        File f = new File(file);
        if (!f.exists()) {
            f.createNewFile();
        }
        Writer writer = new FileWriter(f, false);
        writer.write("<?xml version=\"1.0\" standalone=\"no\"?>\n" +
                "\n" +
                "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \n" +
                "\"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n" +
                " \n" +
                "<svg  version=\"1.1\" viewBox=\"0 0 "+Config.BIN_WIDTH+" "+Config.BIN_HEIGHT+"\" \n" + //width=\""+Config.BIN_WIDTH+"\" height=\""+Config.BIN_HEIGHT+"\"
                "xmlns=\"http://www.w3.org/2000/svg\">\n");
        for(String s : strings){
            writer.write(s);
        }
        writer.write("</svg>");
        writer.close();
    }

}
