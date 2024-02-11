package com.nestapp.nest.util;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.nestapp.nest.config.Config;
import com.nestapp.nest.data.NestPath;
import com.nestapp.nest.data.Segment;


public class CommonUtilTest {

    @Test
    public void offsetTreeTest() throws Exception {
        NestPath nestPath = new NestPath();
        nestPath.add(new Segment(50, 50));
        nestPath.add(new Segment(100, 50));
        nestPath.add(new Segment(100, 100));
        nestPath.add(new Segment(50, 100));
        List<NestPath> nestPaths = new ArrayList<>();
        nestPaths.add(nestPath);
        CommonUtil.offsetTree(nestPaths, 0.5 * nestPath.config.SPACING);
        nestPaths.get(0).toString();
    }

    @Test
    public void polygonOffsetTest() throws Exception {
        Config config = new Config();
        Segment i1 = new Segment(20, 320);
        Segment i2 = new Segment(20, 370);
        Segment i3 = new Segment(70, 370);
        Segment i4 = new Segment(70, 320);
        NestPath in = new NestPath(config);
        in.add(i1);
        in.add(i2);
        in.add(i3);
        in.add(i4);

        List<NestPath> result = CommonUtil.polygonOffset(in, 0.5 * config.SPACING);
        for (Segment s : result.get(0).getSegments()) {
            System.out.println(s);
        }
    }


    @Test
    public void FloatTest() {
        Float f1 = 14f;
        Float f2 = 12f;
        System.out.println(Float.compare(f1, f2));
    }

}
