package com.qunhe.util.nest.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;


public class SegmentTest {

    @Test
    public void segmentTest(){
        Segment s = new Segment(123.456576 , 432.432432);
        System.out.println(s.getX() +","+ s.getY());

    }

    @Test
    public void ListTest() throws Exception{
        List<Segment> s1 = new ArrayList<>();
        s1.add(new Segment(1,2));
        List<Segment> s2 = new ArrayList<>();
        s2.add(s1.get(0));
        s1.get(0).setX(5);
        System.out.println(s1.get(0).getX());
        System.out.println(s2.get(0).getX());

    }

    @Test
    public void swapTest() throws  Exception{
        List<Segment> s1 = new ArrayList<>();
        s1.add(new Segment(1,2));
        s1.add(new Segment(3,4));
        Collections.swap(s1 , 0,1);
        for(Segment s : s1 ){
            System.out.println(s.getX()+" , "+ s.getY());
        }
    }


}
