package com.nestapp.nest.util;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.nestapp.nest.config.Config;
import com.nestapp.nest.contest.ContestData;
import com.nestapp.nest.contest.InputConfig;
import com.nestapp.nest.data.NestPath;
import com.nestapp.nest.data.Placement;
import com.nestapp.nest.data.Segment;

public class IOUtils {
	
	public static void log(Object... o) {
		if (o != null) {
			for (Object element : o) {
				if (element instanceof Exception) {
					((Exception) element).printStackTrace();
				} else {
					System.out.println(element);
				}
			}
		}
	}

	public static void debug(Object... o) {
		if (o != null && Config.IS_DEBUG) {
			for (Object element : o) {
				if (element instanceof Exception) {
					((Exception) element).printStackTrace();
				} else {
					System.out.println(element);
				}
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

            //polygon.setBid(count++);
            polygon.setRotation(0);//TODO
            nestPaths.add(polygon);
            while(--nbPart >= 0) {
                nestPaths.add(new NestPath(polygon));
            }
        }

        return nestPaths;
    }

	public static synchronized void saveNfpCache(Map<String, List<NestPath>> nfpCache, String filename) {
		try {
			Gson g = new Gson();
			String res = g.toJson(nfpCache);
			Path p = Paths.get(filename);
			if (Files.notExists(p, LinkOption.NOFOLLOW_LINKS)) {
				Path directory = p.getParent();
				if (Files.notExists(directory, LinkOption.NOFOLLOW_LINKS)) {
					Files.createDirectory(p.getParent(), new FileAttribute[0]);
				}
				Files.createFile(p, new FileAttribute[0]).toAbsolutePath();
			}
			FileWriter fw = new FileWriter(filename);
			fw.write(res);
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    public static Map<String,List<NestPath>> loadNfpCache(String filename) {
        try {
            String json = new String(Files.readAllBytes(Paths.get(filename)));
            Gson g = new Gson();
            Map<String, List<NestPath>> nfpCache = g.fromJson(json, new HashMap<String, List<NestPath>>().getClass());
            return nfpCache;
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @param filename
     * @param applied
     * @param list CONFIG.INPUT_POLY
     */
    public static void saveToMultiFile(String filename, List<List<Placement>> applied, List<NestPath> list) {
        try {
            //Save to contest file
            IOUtils.writeToFile(filename, applied, InputConfig.INPUT);
            //Save to svg
            List<String> strings = SvgUtil.svgGenerator(list, applied, Config.BIN_WIDTH,
                Config.BIN_HEIGHT);
            saveSvgFile(strings, filename + ".html");
        } catch (Exception e) {
            debug(e);
        }
    }

    public static void saveSvgFile(List<String> strings, String file) throws Exception {
        debug(file);
        Path p = Paths.get(file);
        
        if (Files.notExists(p, LinkOption.NOFOLLOW_LINKS)) {
        	Path directory = p.getParent();
        	if (Files.notExists(directory, LinkOption.NOFOLLOW_LINKS)) {
        		Files.createDirectory(p.getParent(), new FileAttribute[0]);        		
        	}
			Files.createFile(p, new FileAttribute[0]).toAbsolutePath();
		}
        Writer writer = new FileWriter(file, false);
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

	/**
	 * Write result to csv file
	 * @param csvFile
	 * @param applied
	 * @param list
	 * @throws Exception
	 */
	public static void writeToFile(String csvFile, List<List<Placement>> applied, List<ContestData> list) throws Exception{
	    StringBuilder sb = new StringBuilder();
	    for (List<Placement> binlist : applied) {
	        for (Placement placement : binlist) {
	            int bid = placement.bid;
	            ContestData data = ContestData.getContestDataByBid(bid, list);
	            sb.append(data.lotId).append(", ").append(data.partId).append(", ").append(data.binId).append(", [");

	            NestPath nestPath = new NestPath(data.getPolygon());
	            double ox = placement.translate.x;
	            double oy = placement.translate.y;
	            double rotate = placement.rotate;
	            nestPath.translate(ox,oy);
	            nestPath = GeometryUtil.rotatePolygon2Polygon(nestPath, (int)rotate);
	            for (int i = 0; i < nestPath.getSegments().size(); i++) {
	                Segment segment = nestPath.get(i);
	                if(i < nestPath.getSegments().size()-1) {
	                    sb.append("[" + segment.x + "," + segment.y + "], ");
	                }else{
	                    sb.append("[" + segment.x + "," + segment.y + "]]"+System.lineSeparator());
	                }
	            }
	        }
	    }
	    FileWriter fw = new FileWriter(csvFile);
	    fw.write("Blanking batch number, part number, fabric number, coordinate of part outline"+System.lineSeparator());
	    fw.write(sb.toString());
	    fw.close();
	}
}
