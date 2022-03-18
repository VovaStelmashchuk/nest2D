package com.qunhe.util.nest.util;

import java.util.ArrayList;
import java.util.List;

import com.qunhe.util.nest.config.Config;
import com.qunhe.util.nest.data.NestPath;
import com.qunhe.util.nest.data.Segment;
import com.qunhe.util.nest.util.coor.ClipperCoor;
import com.qunhe.util.nest.util.coor.NestCoor;

import de.lighti.clipper.Clipper;
import de.lighti.clipper.ClipperOffset;
import de.lighti.clipper.DefaultClipper;
import de.lighti.clipper.Path;
import de.lighti.clipper.Paths;
import de.lighti.clipper.Point;
import de.lighti.clipper.Point.LongPoint;

/**
 * @author  yisa
 */
public class CommonUtil {


    private static NestPath Path2NestPath (Path path){
        NestPath nestPath = new NestPath();
        for (LongPoint lp : path) {
            NestCoor coor = CommonUtil.toNestCoor(lp.getX(),lp.getY());
            nestPath.add(new Segment(coor.getX() , coor.getY()));
        }
        return nestPath;
    }

    private static Path NestPath2Path (NestPath nestPath ){
        Path path = new Path();
        for(Segment s : nestPath.getSegments()){
            ClipperCoor coor = CommonUtil.toClipperCoor(s.getX() , s.getY());
            Point.LongPoint lp = new Point.LongPoint(coor.getX() , coor.getY());
            path.add(lp);
        }
        return path;
    }

    /**
     * Ã¥ï¿½ï¿½Ã¦Â â€¡Ã¨Â½Â¬Ã¦ï¿½Â¢
     * @param x
     * @param y
     * @return
     */
    static ClipperCoor toClipperCoor(double x , double y ){
        return new ClipperCoor((long)(x*Config.CLIIPER_SCALE) , (long) (y * Config.CLIIPER_SCALE));
    }

    /**
     * Ã¥ï¿½ï¿½Ã¦Â â€¡Ã¨Â½Â¬Ã¦ï¿½Â¢
     * @param x
     * @param y
     * @return
     */
    private static NestCoor toNestCoor(long x , long y ){
        return new NestCoor(((double)x/Config.CLIIPER_SCALE) , ((double)y/Config.CLIIPER_SCALE));
    }


    /**
     * Ã¤Â¸ÂºClipperÃ¤Â¸â€¹Ã§Å¡â€žPathÃ¦Â·Â»Ã¥Å Â Ã§â€šÂ¹
     * @param x
     * @param y
     * @param path
     */
    private static void addPoint(long x, long y , Path path ){
        Point.LongPoint ip = new Point.LongPoint(x,y);
        path.add(ip);
    }


    /**
     * binPathÃ¦ËœÂ¯Ã¤Â½Å“Ã¤Â¸ÂºÃ¥Âºâ€¢Ã¦ï¿½Â¿Ã§Å¡â€žNestPath , polysÃ¥Ë†â„¢Ã¤Â¸ÂºÃ¦ï¿½Â¿Ã¤Â»Â¶Ã§Å¡â€žPathÃ¥Ë†â€”Ã¨Â¡Â¨
     * Ã¨Â¿â„¢Ã¤Â¸ÂªÃ¦â€“Â¹Ã¦Â³â€¢Ã¦ËœÂ¯Ã¤Â¸ÂºÃ¤Âºâ€ Ã¥Â°â€ binPathÃ¥â€™Å’polysÃ¥Å“Â¨Ã¤Â¸ï¿½Ã¦â€�Â¹Ã¥ï¿½ËœÃ¨â€¡ÂªÃ¨ÂºÂ«Ã¥Â½Â¢Ã§Å Â¶Ã¯Â¼Å’Ã¨Â§â€™Ã¥ÂºÂ¦Ã§Å¡â€žÃ¦Æ’â€¦Ã¥â€ ÂµÃ¤Â¸â€¹Ã¦â€�Â¾Ã§Â½Â®Ã¥Å“Â¨Ã¤Â¸â‚¬Ã¤Â¸ÂªÃ¥ï¿½ï¿½Ã¦Â â€¡Ã§Â³Â»Ã¥â€ â€¦Ã¯Â¼Å’Ã¤Â¿ï¿½Ã¨Â¯ï¿½Ã¤Â¸Â¤Ã¤Â¸Â¤Ã¤Â¹â€¹Ã©â€”Â´Ã¤Â¸ï¿½Ã¤ÂºÂ¤Ã¥ï¿½â€°
     * @param binPath
     * @param polys
     */
    public static void ChangePosition(NestPath binPath , List<NestPath> polys){

    }

    /**
     *  Ã¥Â°â€ NestPathÃ¥Ë†â€”Ã¨Â¡Â¨Ã¨Â½Â¬Ã¦ï¿½Â¢Ã¦Ë†ï¿½Ã§Ë†Â¶Ã¥Â­ï¿½Ã¥â€¦Â³Ã§Â³Â»Ã§Å¡â€žÃ¦Â â€˜
     * @param list
     * @param idstart
     * @return
     */
    static int toTree(List<NestPath> list , int idstart){
        List<NestPath> parents = new ArrayList<>();
        int id = idstart;
        /**
         * Ã¦â€°Â¾Ã¥â€¡ÂºÃ¦â€°â‚¬Ã¦Å“â€°Ã§Å¡â€žÃ¥â€ â€¦Ã¥â€ºÅ¾Ã§Å½Â¯
         */
        for(int i = 0 ; i<list.size() ; i ++){
            NestPath p = list.get(i);
            boolean isChild = false;
            for(int j = 0; !Config.ASSUME_NO_INNER_PARTS && j<list.size(); j++){
                if(j == i ){
                    continue;
                }
                Boolean b = GeometryUtil.pointInPolygon(p.getSegments().get(0) , list.get(j));
                if(b!=null && b==true ){
                    list.get(j).getChildren().add(p);
                    p.setParent(list.get(j));
                    isChild = true;
                    break;
                }
            }
            if(!isChild){
                parents.add(p);
            }
        }
        /**
         *  Ã¥Â°â€ Ã¥â€ â€¦Ã§Å½Â¯Ã¤Â»Å½listÃ¥Ë†â€”Ã¨Â¡Â¨Ã¤Â¸Â­Ã¥Å½Â»Ã©â„¢Â¤
         */
        for(int i = 0; !Config.ASSUME_NO_INNER_PARTS && i <list.size() ; i ++){
            if(parents.indexOf(list.get(i)) < 0 ){
                list.remove(i);
                i--;
            }
        }

        for (NestPath parent : parents) {
            parent.setId(id);
            id ++;
        }

        for(int i = 0 ;!Config.ASSUME_NO_INNER_PARTS &&  i<parents.size() ; i ++){
            if(parents.get(i).getChildren().size() > 0 ){
                id = toTree(parents.get(i).getChildren(),id);
            }
        }
        return id;
    }

    private static NestPath clipperToNestPath(Path polygon){
        NestPath normal = new NestPath();
        for (LongPoint element : polygon) {
            NestCoor nestCoor = toNestCoor(element.getX() , element.getY());
            normal.add(new Segment(nestCoor.getX() , nestCoor.getY()));
        }
        return normal;
    }

    public static void offsetTree(List<NestPath> t , double offset ){
        for (NestPath element : t) {
            List<NestPath> offsetPaths = polygonOffset(element , offset);
            if(offsetPaths.size() == 1 ){
                element.clear();
                NestPath from = offsetPaths.get(0);

                for(Segment s : from.getSegments()){
                    element.add(s);
                }
            }
            if(element.getChildren().size() > 0 ){

                offsetTree(element.getChildren() , -offset);
            }
        }
    }

    public static List<NestPath> polygonOffset(NestPath polygon , double offset){
        List<NestPath> result = new ArrayList<>();
        if(offset == 0 || GeometryUtil.almostEqual(offset,0)){
            /**
             * return EmptyResult
             */
            return result;
        }
        Path p = new Path();
        for(Segment s : polygon.getSegments()){
            ClipperCoor cc = CommonUtil.toClipperCoor(s.getX(),s.getY());
            p.add(new Point.LongPoint( cc.getX() ,cc.getY()));
        }

        int miterLimit = 2;
        ClipperOffset co = new ClipperOffset(miterLimit , Config.CURVE_TOLERANCE * Config.CLIIPER_SCALE);
        co.addPath(p, Clipper.JoinType.ROUND , Clipper.EndType.CLOSED_POLYGON);

        Paths newpaths = new Paths();
        co.execute(newpaths , offset * Config.CLIIPER_SCALE);

        /**
         * Ã¨Â¿â„¢Ã©â€¡Å’Ã§Å¡â€žlengthÃ¦ËœÂ¯1Ã§Å¡â€žÃ¨Â¯ï¿½Ã¥Â°Â±Ã¦ËœÂ¯Ã¦Ë†â€˜Ã¤Â»Â¬Ã¦Æ’Â³Ã¨Â¦ï¿½Ã§Å¡â€ž
         */
        for (Path newpath : newpaths) {
            result.add(CommonUtil.clipperToNestPath(newpath));
        }

        if(offset > 0 ){
            NestPath from = result.get(0);
            if(GeometryUtil.polygonArea(from) > 0 ){
                from.reverse();
            }
            from.add(from.get(0));from.getSegments().remove(0);
        }


        return result;
    }


    /**
     * Ã¥Â¯Â¹Ã¥Âºâ€�Ã¤ÂºÅ½JSÃ©Â¡Â¹Ã§â€ºÂ®Ã¤Â¸Â­Ã§Å¡â€žgetParts
     * @param parts (all polygons)
     * @param curve_tolerance 
     * @return polygons without self intersecting parts
     */
    public static List<NestPath> BuildTree(List<NestPath> parts ,double curve_tolerance){
        List<NestPath> polygons = new ArrayList<>();
        for(int i =0 ; i<parts.size();i++){
            // Do cleaning with Clipper: self intersecting, redundant vertices...
            NestPath cleanPoly = CommonUtil.cleanNestPath(parts.get(i));
            cleanPoly.setBid(parts.get(i).getBid());
            // Some parts are too small to keep. TODO remove this for the match
            if(cleanPoly.size() > 2 &&  Math.abs(GeometryUtil.polygonArea(cleanPoly)) > curve_tolerance * curve_tolerance){
                cleanPoly.setSource(i);

                polygons.add(cleanPoly);
            }
        }

        CommonUtil.toTree(polygons,0);
        return polygons;
    }

    
    /**
     * @param scrPath (all polygons)
     * @return polygons without self intersecting parts
     */
	public static NestPath cleanNestPath(NestPath srcPath){
	    /**
	     * Convert NestPath 2 Clipper
	     */
	    Path path = NestPath2Path(srcPath);	//scrPath è il binPath passato come argomento, la superficie su cui disporre le figure
	    // Convert self intersecting polygons to simple ones
	    Paths simple = DefaultClipper.simplifyPolygon(path, Clipper.PolyFillType.NON_ZERO);
	    if(simple.size() == 0 ){
	        return null;
	    }
	    Path biggest = simple.get(0);
	    double biggestArea = Math.abs(biggest.area());
	    for(int i = 1; i <simple.size();i++){
	        double area = Math.abs(simple.get(i).area());
	        if(area > biggestArea ){
	            biggest = simple.get(i);
	            biggestArea = area;
	        }
	    }
	    // Remove vertices under tolerance specification
	    Path clean = biggest.cleanPolygon(Config.CURVE_TOLERANCE * Config.CLIIPER_SCALE);
	    if(clean.size() == 0 ){
	        return null ;
	    }

	    /**
	     *  Convert Clipper 2 NestPath
	     */
	    NestPath cleanPath = Path2NestPath(clean);
	    cleanPath.setBid(srcPath.getBid());
	    cleanPath.setRotation(srcPath.getRotation());
	    cleanPath.setPossibleRotations(srcPath.getPossibleRotations());
	    return cleanPath;
	}
}
