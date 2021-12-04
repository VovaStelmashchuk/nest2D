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
     * å��æ ‡è½¬æ�¢
     * @param x
     * @param y
     * @return
     */
    static ClipperCoor toClipperCoor(double x , double y ){
        return new ClipperCoor((long)(x*Config.CLIIPER_SCALE) , (long) (y * Config.CLIIPER_SCALE));
    }

    /**
     * å��æ ‡è½¬æ�¢
     * @param x
     * @param y
     * @return
     */
    private static NestCoor toNestCoor(long x , long y ){
        return new NestCoor(((double)x/Config.CLIIPER_SCALE) , ((double)y/Config.CLIIPER_SCALE));
    }


    /**
     * ä¸ºClipperä¸‹çš„Pathæ·»åŠ ç‚¹
     * @param x
     * @param y
     * @param path
     */
    private static void addPoint(long x, long y , Path path ){
        Point.LongPoint ip = new Point.LongPoint(x,y);
        path.add(ip);
    }


    /**
     * binPathæ˜¯ä½œä¸ºåº•æ�¿çš„NestPath , polysåˆ™ä¸ºæ�¿ä»¶çš„Pathåˆ—è¡¨
     * è¿™ä¸ªæ–¹æ³•æ˜¯ä¸ºäº†å°†binPathå’Œpolysåœ¨ä¸�æ”¹å�˜è‡ªèº«å½¢çŠ¶ï¼Œè§’åº¦çš„æƒ…å†µä¸‹æ”¾ç½®åœ¨ä¸€ä¸ªå��æ ‡ç³»å†…ï¼Œä¿�è¯�ä¸¤ä¸¤ä¹‹é—´ä¸�äº¤å�‰
     * @param binPath
     * @param polys
     */
    public static void ChangePosition(NestPath binPath , List<NestPath> polys){

    }

    /**
     *  å°†NestPathåˆ—è¡¨è½¬æ�¢æˆ�çˆ¶å­�å…³ç³»çš„æ ‘
     * @param list
     * @param idstart
     * @return
     */
    static int toTree(List<NestPath> list , int idstart){
        List<NestPath> parents = new ArrayList<>();
        int id = idstart;
        /**
         * æ‰¾å‡ºæ‰€æœ‰çš„å†…å›žçŽ¯
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
         *  å°†å†…çŽ¯ä»Žliståˆ—è¡¨ä¸­åŽ»é™¤
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
         * è¿™é‡Œçš„lengthæ˜¯1çš„è¯�å°±æ˜¯æˆ‘ä»¬æƒ³è¦�çš„
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
     * å¯¹åº”äºŽJSé¡¹ç›®ä¸­çš„getParts
     */
    public static List<NestPath> BuildTree(List<NestPath> parts ,double curve_tolerance){
        List<NestPath> polygons = new ArrayList<>();
        for(int i =0 ; i<parts.size();i++){
            // Do cleaning with Clipper: self intersecting, redundant vertices...
            NestPath cleanPoly = CommonUtil.cleanNestPath(parts.get(i));
            cleanPoly.bid = parts.get(i).bid;
            // Some parts are too small to keep. TODO remove this for the match
            if(cleanPoly.size() > 2 &&  Math.abs(GeometryUtil.polygonArea(cleanPoly)) > curve_tolerance * curve_tolerance){
                cleanPoly.setSource(i);

                polygons.add(cleanPoly);
            }
        }

        CommonUtil.toTree(polygons,0);
        return polygons;
    }

	public static NestPath cleanNestPath(NestPath srcPath){
	    /**
	     * Convert NestPath 2 Clipper
	     */
	    Path path = NestPath2Path(srcPath);
	    // Convert self interacting polygons to simple ones
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
	    cleanPath.bid = srcPath.bid;
	    cleanPath.setRotation(srcPath.getRotation());
	    cleanPath.setPossibleRotations(srcPath.getPossibleRotations());
	    return cleanPath;
	}
}
