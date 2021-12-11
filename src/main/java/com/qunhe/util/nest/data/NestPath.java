package com.qunhe.util.nest.data;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.List;

import org.apache.batik.ext.awt.geom.Polygon2D;

import com.qunhe.util.nest.config.Config;

/**
 * @author yisa
 */
public class NestPath implements Comparable<NestPath>{
    private List<Segment> segments;
    private List<NestPath> children;
    private NestPath parent;
    public double offsetX;
    public double offsetY;
    
    private int id;
    private int source ;
    private int rotation;	// angolo rotazione
    
    public int[] rotations;
    public Config config ;	// ???????
    public double area ;

    // assgnied incrementally or cloned
    private int bid;	// Identificativo??

    static private int bid_counter = 1; 
    
    public NestPath(){
    	this(new Config());
    }


    public NestPath(Config config) {
        offsetX = 0;
        offsetY = 0;
        children = new ArrayList<>();
        segments = new ArrayList<>();
        area = 0;
        this.config = config;
        //
        bid = bid_counter++;
    }

    public NestPath(NestPath srcNestPath){
        segments = new ArrayList<>();
        for(Segment segment : srcNestPath.getSegments() ){
            segments.add(new Segment(segment));
        }

        this.id  = srcNestPath.id;
        this.rotation = srcNestPath.rotation;
        this.rotations =  srcNestPath.rotations;//TODO not clone.
        this.source = srcNestPath.source;
        this.offsetX = srcNestPath.offsetX;
        this.offsetY = srcNestPath.offsetY;
        this.bid = srcNestPath.bid;
        this.area = srcNestPath.area;
        children = new ArrayList<>();

        for(NestPath nestPath: srcNestPath.getChildren()){
            NestPath child = new NestPath(nestPath);
            child.setParent(this);
            children.add(child);
        }
    }

    
    
    public void add(double x , double y ){
        this.add(new Segment(x,y));
    }

    @Override
    public boolean equals(Object obj) {
        NestPath nestPath = (NestPath) obj;
        if(segments.size() != nestPath.size()){
            return false;
        }
        for(int  i =0 ; i <segments.size(); i ++){
            if(!segments.get(i).equals(nestPath.get(i))){
                return false;
            }
        }
        if(children.size() != nestPath.getChildren().size()){
            return false;
        }
        for(int i = 0 ; i<children.size(); i ++){
            if(!children.get(i).equals(nestPath.getChildren().get(i))){
                return false;
            }
        }
        return true;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    /**
     * Discard the last segment
     */
    public void pop(){
        segments.remove(segments.size()-1);
    }

    public void reverse(){
        List<Segment> rever = new ArrayList<>();
        for(int i = segments.size()-1; i >=0; i -- ){
            rever.add(segments.get(i));
        }
        segments.clear();
        for(Segment s : rever ){
            segments.add(s);
        }
    }

    public Segment get(int i){
        return segments.get(i);
    }

    public NestPath getParent() {
        return parent;
    }

    public void setParent(NestPath parent) {
        this.parent = parent;
    }

    
    public void addChildren(NestPath nestPath){
        children.add(nestPath);
        nestPath.setParent(this);
    }

    @Override
    public String toString() {
        String res = "";
        res += "id = "+ id+" , source = "+ source +" , rotation = "+rotation +"\n";
        int count = 0;
        for(Segment s :segments){
            res += "Segment " + count +"\n";
            count++;
            res+= s.toString() +"\n";
        }
        count = 0 ;
        for(NestPath nestPath: children){
            res += "children "+ count +"\n";
            count++;
            res += nestPath.toString();
        }
        return res;
    }

    public List<NestPath> getChildren() {
        return children;
    }

    public void setChildren(List<NestPath> children) {
        this.children = children;
    }

    public int getRotation() {
        return rotation;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public void setSegments(List<Segment> segments) {
        this.segments = segments;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }


    /**
     * The lowest x coordinate and y coordinate value of NestPath must be 0 by translation,
     */
    public void Zerolize(){
        ZeroX();ZeroY();
    }

    private void ZeroX(){
        double xMin = Double.MAX_VALUE;
        for(Segment s : segments){
            if(xMin > s.getX() ){
                xMin = s.getX();
            }
        }
        for(Segment s :segments ){
            s.setX(s.getX() - xMin );
        }
    }

    private void ZeroY(){
        double yMin = Double.MAX_VALUE;
        for(Segment s : segments){
            if(yMin > s.getY() ){
                yMin = s.getY();
            }
        }
        for(Segment s : segments ){
            s.setY(s.getY() - yMin);
        }
    }

    public void clear(){
        segments.clear();
    }

    public int size(){
        return segments.size();
    }

    public void add(Segment s){
        segments.add(s);
    }

    public List<Segment> getSegments() {
        return segments;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(double offsetX) {
        this.offsetX = offsetX;
    }

    public double getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(double offsetY) {
        this.offsetY = offsetY;
    }

    @Override
    public int compareTo(NestPath o) {
        double area0  = this.area;
        double area1 = o.area;
        if(area0 > area1 ){
            return 1;
        }
        else if(area0 == area1){
            return 0;
        }
        return -1;
    }

    public double getMaxY(){
        double MaxY = Double.MIN_VALUE;
        for(Segment s : segments){
            if(MaxY < s.getY()){
                MaxY = s.getY();
            }
        }
        return MaxY;
    }

    public void translate(double x,  double y ){
        for(Segment s : segments){
            s.setX(s.getX() + x );
            s.setY(s.getY() + y);
        }
    }

    public void setPossibleRotations(int[] rotations) {
        this.rotations = rotations;
    }

    public int[] getPossibleRotations() {
        return this.rotations;
    }


	public int getBid() {
		return bid;
	}
	@Deprecated
	public void setBid(int bid) {
		this.bid = bid;
	}
	
	
	public Polygon2D toPolygon2D() {
		Polygon2D newp;
		
		List<Float> xp = new ArrayList<Float>();
		List<Float> yp = new ArrayList<Float>();
		for(Segment s : segments){
           xp.add((float)s.getX());
           yp.add((float)s.getY());
        }
				
		float[] xparray = new float[xp.size()];
		float[] yparray = new float[yp.size()];
		int i = 0;

		for (Float f : xp) {
		    xparray[i++] = (f != null ? f : Float.NaN); 
		}
		i=0;
		for (Float f : yp) {
		    yparray[i++] = (f != null ? f : Float.NaN);
		}
		
		
		newp = new Polygon2D(xparray, yparray, segments.size());
		return newp;
	}

}
