package com.nestapp.nest.contest;


import static com.nestapp.nest.util.IOUtils.log;
import static com.nestapp.nest.util.IOUtils.saveSvgFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.nestapp.nest.contest.ContestData;
import com.nestapp.nest.contest.InputConfig;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.nestapp.nest.Nest;
import com.nestapp.nest.config.Config;
import com.nestapp.nest.data.NestPath;
import com.nestapp.nest.data.Placement;
import com.nestapp.nest.util.IOUtils;
import com.nestapp.nest.util.SvgUtil;

public class Play {

    @Test
    public void play() throws Exception {
        //TODO change here to switch input file
        String lotId = "L0003";
        // Config
        Config config = new Config();
        config.SPACING = 5; //TODO change to 5
        config.POPULATION_SIZE = 5;
        config.MUTATION_RATE = 10;
        config.USE_HOLE = false;
        //config.CONCAVE=true;

        Config.CLIIPER_SCALE = 10000;
        Config.CURVE_TOLERANCE = 0.3; // Used to simplify polygons
        Config.IS_DEBUG = true;
        Config.BIN_WIDTH = 20000;
        Config.BIN_HEIGHT = 1000;
        Config.NB_ITERATIONS = 2;
        Config.BOUND_SPACING = 0;
        Config.ASSUME_NO_INNER_PARTS = true;
        Config.ASSUME_ALL_PARTS_PLACABLE = true;
        String INPUT_FILE = "./data/"+lotId+"_lingjian.csv";
        //inputFile = "./data/debug";
        String OUTPUT_DIR = "./data/";
        String OUTPUT_FILE = OUTPUT_DIR+lotId+".csv";
        Config.LIMIT = 10;
        // TODO enable this line to load nfp from file
        //Config.NFP_CACHE_PATH=Config.OUTPUT_DIR+"nfp"+lotId+".txt";

        // Start ------------
        //List<NestPath> polygons = IOUtils.readFromContestFile(inputFile);
        IOUtils.log("Loading data from "+ INPUT_FILE);
        List<ContestData> datas = ContestData.readFromFile(INPUT_FILE);
        List<NestPath> polygons = datas.stream().map(ContestData::getPolygon).collect(Collectors.toList());
        IOUtils.log(polygons.size()+" parts loaded.");
        InputConfig.INPUT = datas;
        InputConfig.INPUT_POLY = polygons;
        if(Config.LIMIT>0){
            polygons = polygons.subList(0,Config.LIMIT);
        }

        NestPath bin = new NestPath();
        bin.add(0, 0);
        bin.add(Config.BIN_WIDTH, 0);
        bin.add(Config.BIN_WIDTH, Config.BIN_HEIGHT);
        bin.add(0, Config.BIN_HEIGHT);
        bin.setBid(-1);

        Nest nest = new Nest(bin, polygons, config, Config.NB_ITERATIONS);
        List<List<Placement>> appliedPlacement = nest.startNest();
        IOUtils.log("Save to svg...");
        List<String> strings = SvgUtil.svgGenerator(polygons, appliedPlacement, Config.BIN_WIDTH, Config.BIN_HEIGHT);
        saveSvgFile(strings,OUTPUT_FILE+".html");
        IOUtils.log("Save to 'submit' folder...");
        IOUtils.writeToFile(OUTPUT_FILE, appliedPlacement, datas);
    }

    @Test
    public void testSeri(){
        Map<String, Integer> m = new HashMap<>();
        m.put("test",5);
        Gson g =new Gson();
        String res = g.toJson(m, m.getClass());
        log(res);
        Gson g2 = new Gson();
        Map<String, Integer> m2 = g2.fromJson(res, m.getClass());
        log("Done");
    }



}
