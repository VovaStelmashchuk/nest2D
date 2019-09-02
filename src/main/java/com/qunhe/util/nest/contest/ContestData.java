package com.qunhe.util.nest.contest;

import com.qunhe.util.nest.data.NestPath;
import com.qunhe.util.nest.data.Placement;
import com.qunhe.util.nest.data.Segment;
import com.qunhe.util.nest.util.GeometryUtil;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Represent the input date
 */
public class ContestData {
    public String lotId;
    public String partId;
    public String binId;
    private int bid;
    private NestPath polygon;
    static final Pattern p = Pattern.compile("\\G\\[+([\\d\\.]+),\\s*([\\d\\.]+)[\\],\\]\\s]+");

    public ContestData(String lotId, String partId, String binId){
        this.lotId = lotId;
        this.partId = partId;
        this.binId = binId;
    }


    public static List<ContestData> readFromFile(String csvFile) throws Exception{
        List<ContestData> res = new ArrayList<>();
        List<NestPath> nestPaths = new ArrayList<>();
        BufferedReader reader = Files.newBufferedReader(Paths.get(csvFile));
        String line = reader.readLine();
        int count = 0;
        while((line=reader.readLine())!=null){
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
            Matcher m = p.matcher(fields[1]);
            while(m.find()){
                //log(m.group(1));
                //log(m.group(2));
                double x = Double.parseDouble(m.group(1));
                double y = Double.parseDouble(m.group(2));
                polygon.add(x, y);
            }
            polygon.bid = count++;
            //TODO temply set to 0
            polygon.setRotation(0);
            polygon.setPossibleRotations(Arrays.stream(rotDegrees).mapToInt(Integer::parseInt).toArray());
            nestPaths.add(polygon);

            // Create data
            ContestData item = new ContestData(lotId, partId,matId);
            item.setBid(polygon.bid);
            item.setPolygon(polygon);
            res.add(item);

            while(--nbPart > 0) {
                NestPath polyCopy = new NestPath(polygon);
                ContestData itemCopy = new ContestData(lotId, partId,matId);
                polyCopy.bid = count++;
                itemCopy.setBid(polyCopy.bid);
                itemCopy.setPolygon(polyCopy);
                nestPaths.add(polyCopy);
                res.add(itemCopy);
            }
        }

        return res;
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
                ContestData data = getContestDataByBid(bid, list);
                sb.append(data.lotId).append(",").append(data.partId).append(",").append(data.binId).append(",[");

                NestPath nestPath = data.getPolygon();
                double ox = placement.translate.x;
                double oy = placement.translate.y;
                double rotate = placement.rotate;
                nestPath.translate(ox,oy);
                nestPath = GeometryUtil.rotatePolygon2Polygon(nestPath, (int)rotate);
                for (int i = 0; i < nestPath.getSegments().size(); i++) {
                    Segment segment = nestPath.get(i);
                    sb.append("["+segment.x + "," + segment.y + "],");
                }
                sb.append("\b]"+System.lineSeparator());
            }
        }
        FileWriter fw = new FileWriter(csvFile);
        fw.write(sb.toString());

    }

    public static ContestData getContestDataByBid(int id, List<ContestData> list){
        for(ContestData cd : list){
            if(cd.getBid() == id){
                return cd;
            }
        }
        return null;
    }

    public NestPath getPolygon() {
        return polygon;
    }

    public void setPolygon(NestPath polygon) {
        this.polygon = polygon;
    }

    public int getBid() {
        return bid;
    }

    public void setBid(int bid) {
        this.bid = bid;
    }
}
