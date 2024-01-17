package com.nestapp.nest.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.nestapp.nest.data.NestPath;
import com.nestapp.nest.data.Segment;
import org.junit.jupiter.api.Test;

import com.nestapp.nest.util.GeometryUtil;

public class NestPathTest {

    @Test
    public void reverseTest() throws Exception{
        NestPath nestPath = new NestPath();
        nestPath.add(new Segment(10,10));
        nestPath.add(new Segment(20,0));
        nestPath.add(new Segment(0, 0 ));
        nestPath.toString();
        nestPath.reverse();
        nestPath.toString();
    }

    @Test
    public void CloneTest() throws Exception{
        NestPath nestPath = new NestPath();
        nestPath.setSource(1);
        nestPath.setId(2);
        nestPath.setRotation(3);
        Segment s1 = new Segment(1,2);
        nestPath.add(s1);

        NestPath chPath = new NestPath();
        chPath.setSource(11);
        chPath.setId(12);
        chPath.setRotation(13);
        Segment s2 = new Segment(3,4);
        chPath.add(s2);
        nestPath.addChildren(chPath);

        nestPath.toString();
        NestPath clone = new NestPath(nestPath);
        clone.toString();
        clone.getSegments().get(0).setX(11);
        clone.getSegments().get(0).setY(14);
        clone.getChildren().get(0).setRotation(15);
        clone.setRotation(222);
        clone.getChildren().get(0).getSegments().get(0).setX(55);
        nestPath.toString();
        clone.toString();
    }

    @Test
    public void reMoveTest() throws Exception{
        List<NestPath> list = new ArrayList<>();
        NestPath nestPath = new NestPath();
        nestPath.add(new Segment(0,0));
        nestPath.add(new Segment(10,10));
        nestPath.add(new Segment(20, 0 ));

        NestPath nestPath1 = new NestPath();
        nestPath1.add(new Segment(5,5));
        nestPath1.add(new Segment(15,15));
        nestPath1.add(new Segment(25, 5 ));


        NestPath nestPath2 = new NestPath();
        nestPath2.add(new Segment(4,4));
        nestPath2.add(new Segment(14,14));
        nestPath2.add(new Segment(24, 4));

        list.add(nestPath);
        list.add(nestPath1);
        list.add(nestPath2);

        for(int i = 0; i <list.size() ; i ++){
            if(list.indexOf(nestPath1) == i ){
                list.remove(i);
                i--;
            }
        }
        for (NestPath element : list) {
            element.toString();
        }
    }


    @Test
    public void equalTest() throws Exception{
        NestPath nestPath = new NestPath();
        nestPath.add(new Segment(0,0));
        nestPath.add(new Segment(10,10));
        nestPath.add(new Segment(20, 0 ));

        NestPath nestPath1 = new NestPath();
        nestPath1.add(new Segment(5,5));
        nestPath1.add(new Segment(15,15));
        nestPath1.add(new Segment(25, 5 ));
        nestPath.addChildren(nestPath1);
        NestPath clone = new NestPath(nestPath);
        System.out.println(clone.equals(nestPath) +" , "+ nestPath.equals(clone));
    }

    @Test
    public void sortTest() throws Exception{
        NestPath nestPath = new NestPath();
        nestPath.add(new Segment(0,0));
        nestPath.add(new Segment(10,10));
        nestPath.add(new Segment(20, 0 ));

        Segment t1 = new Segment(40 ,0);
        Segment t2 = new Segment(40 ,40);
        Segment t3 = new Segment(80 ,40);
        Segment t4 = new Segment(80 ,0);
        NestPath rect = new NestPath();
        rect.add(t1);rect.add(t2);rect.add(t3);rect.add(t4);
        List<NestPath> list = new ArrayList<>();
        list.add(nestPath);list.add(rect);

        Collections.sort(list);
        double area0 = Math.abs(GeometryUtil.polygonArea(list.get(0)));
        double area1 = Math.abs(GeometryUtil.polygonArea(list.get(1)));
        System.out.println("area0 = "+area0 +" , area1 = "+area1);


    }

}
