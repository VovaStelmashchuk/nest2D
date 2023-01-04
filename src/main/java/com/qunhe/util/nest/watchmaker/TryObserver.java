package com.qunhe.util.nest.watchmaker;

import java.util.ArrayList;
import java.util.List;

import com.qunhe.util.nest.Nest;
import com.qunhe.util.nest.config.Config;
import com.qunhe.util.nest.data.NestPath;
import com.qunhe.util.nest.data.Placement;
import com.qunhe.util.nest.util.SvgUtil;

public class TryObserver {
	
	public static void main(String[] args) throws Exception {

		System.out.println("Proviamo");
	// @Test
	   // public void HoleTest() throws Exception {
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

}
