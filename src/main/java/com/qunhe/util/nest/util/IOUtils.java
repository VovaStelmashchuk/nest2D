package com.qunhe.util.nest.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.qunhe.util.nest.contest.ContestData;
import com.qunhe.util.nest.data.NestPath;
import com.qunhe.util.nest.data.Placement;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IOUtils {
    public static void log(Object... o){
        if(o != null) {
            for(int i=0; i<o.length;++i) {
                System.out.println(o[i]);
            }
        }
    }

    public static void debug(Object... o){
        if(o != null && Config.IS_DEBUG) {
            for(int i=0; i<o.length;++i) {
                System.out.println(o[i]);
            }
        }
    }

    public static List<NestPath> readFromContestFile(String filepath) throws Exception{
        List<NestPath> nestPaths = new ArrayList<>();
        BufferedReader reader = Files.newBufferedReader(Paths.get(filepath));
        String line = reader.readLine();
        int count = 0;
        while((line=reader.readLine())!=null){
//            if(count>100){
//                break;
//            }
            // Each line defines a part
            String[] fields = line.split("\"");
            String[] fields3 = fields[0].split(",");
            String lotId = fields3[0];
            String partId = fields3[1];
            int nbPart = Integer.parseInt(fields3[2]);
            String []rotDegrees = fields[3].split(",");
            String matId = fields[4].replace(",","");
            // Coord
            NestPath polygon = new NestPath();
            Pattern p = Pattern.compile("\\G\\[+([\\d\\.]+),\\s*([\\d\\.]+)[\\],\\]\\s]+");
            Matcher m = p.matcher(fields[1]);
            while(m.find()){
                //log(m.group(1));
                //log(m.group(2));
                double x = Double.parseDouble(m.group(1));
                double y = Double.parseDouble(m.group(2));
                polygon.add(x, y);
            }

            polygon.bid = count++;
            polygon.setRotation(0);//TODO
            nestPaths.add(polygon);
            while(--nbPart >= 0) {
                nestPaths.add(new NestPath(polygon));
            }
        }

        return nestPaths;
    }

    public static void saveNfpCache(Map<String,List<NestPath>> nfpCache, String filename){
        try {
            Gson g = new Gson();
            String res = g.toJson(nfpCache);
            FileWriter fw = new FileWriter(filename);
            fw.write(res);
            fw.close();
        }catch (Exception e){
            log(e);
        }
    }

    public static Map<String,List<NestPath>> loadNfpCache(String filename) {
        try {
            String json = new String(Files.readAllBytes(Paths.get(filename)));
            Gson g = new Gson();
            Map<String, List<NestPath>> nfpCache = g.fromJson(json, new HashMap<String, List<NestPath>>().getClass());
            return nfpCache;
        } catch (Exception e) {
            log(e);
        }
        return null;
    }

    public static void saveToMultiFile(String filename, List<List<Placement>> applied) {
        try {
            //Save to contest file
            ContestData.writeToFile(filename, applied, Config.INPUT);
            //Save to svg
            List<String> strings = SvgUtil.svgGenerator(Config.INPUT_POLY, applied, Config.BIN_WIDTH,
                Config.BIN_HEIGHT);
            saveSvgFile(strings, filename + ".html");
        } catch (Exception e) {
            debug(e);
        }
    }

    public static void saveSvgFile(List<String> strings, String file) throws Exception {
        debug(file);
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
