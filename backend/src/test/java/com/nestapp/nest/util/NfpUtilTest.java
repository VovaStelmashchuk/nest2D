package com.nestapp.nest.util;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.nestapp.nest.config.Config;
import com.nestapp.nest.data.NestPath;
import com.nestapp.nest.nfp.NfpKey;
import com.nestapp.nest.nfp.NfpPair;
import com.nestapp.nest.data.ParallelData;

public class NfpUtilTest {


    @Test
    public void nfpGenerator() {
        NestPath outer = new NestPath();
        outer.add(600,0);outer.add(600,200);
        outer.add(800,200);outer.add(800,0);
        outer.setRotation(0);
        outer.setBid(1);
        NestPath inner = new NestPath();
        inner.add(650,50);
        inner.add(650,150);
        inner.add(750,150);
        inner.add(750,50);
        inner.setBid(2);
        NestPath little = new NestPath();
        little.add(900,0);
        little.add(870,20);
        little.add(930,20);
        little.setBid(3);

        List<NestPath> list = new ArrayList<>();
        list.add(inner);list.add(outer);list.add(little);
        Config config = new Config();
        List<NestPath> tree = NewCommonUtils.INSTANCE.copyNestPathsAndSetIds(list);
        CommonUtil.offsetTree(tree , 0.5 * config.SPACING);
        NestPath A = tree.get(0);
        NestPath B = tree.get(1);
        NfpKey nfpKey = new NfpKey(A.getId(),B.getId(),false,0 ,0 );
        NfpPair nfpPair = new NfpPair(A,B, nfpKey);
        ParallelData parallelData = NfpUtil.nfpGenerator(nfpPair);
    }
}
