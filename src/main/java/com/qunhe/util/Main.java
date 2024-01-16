package com.qunhe.util;

import com.app.DxfPart;
import com.qunhe.util.nest.Nest;
import com.qunhe.util.nest.config.Config;
import com.qunhe.util.nest.data.NestPath;
import com.qunhe.util.nest.data.Placement;
import com.qunhe.util.nest.util.SvgUtil;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.qunhe.util.nest.util.IOUtils.saveSvgFile;

class Main {

    private List<List<Point>> points = new ArrayList<>();


    public static void main(String[] args) throws Exception {
        List<DxfPart> listOfListOfPoints = new ArrayList<>();
        listOfListOfPoints.addAll(
                getEntitiesFromFile("/Users/vovastelmashchuk/Desktop/dxf_app/Nest4J/input/1x1.dxf")
        );
        listOfListOfPoints.addAll(
                getEntitiesFromFile("/Users/vovastelmashchuk/Desktop/dxf_app/Nest4J/input/1x1.dxf")
        );
        listOfListOfPoints.addAll(
                getEntitiesFromFile("/Users/vovastelmashchuk/Desktop/dxf_app/Nest4J/input/1x4.dxf")
        );

        NestPath binPolygon = getBinPolygon();

        List<NestPath> list = new ArrayList<>();

        listOfListOfPoints.forEach(dxfPart -> {
            list.add(dxfPart.nestPath);
        });

        Config config = new Config();
        config.USE_HOLE = false;
        config.SPACING = 1.5;
        config.CONCAVE = false;

        Nest nest = new Nest(binPolygon, list, config, 10);
        List<List<Placement>> appliedPlacement = nest.startNest();
        List<String> strings = SvgUtil.svgGenerator(list, appliedPlacement, 300, 300);

        System.out.println(appliedPlacement);

        saveSvgFile(strings, Config.OUTPUT_DIR + "test5.svg");
    }

    private static List<DxfPart> getEntitiesFromFile(String fileName) {
        DXFReader dxfReader = new DXFReader();
        try {
            dxfReader.parseFile(new File(fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return getEntities(dxfReader);
    }

    private static List<DxfPart> getEntities(DXFReader dxfReader14) {
        List<DxfPart> listOfListOfPoints = new ArrayList<>();
        for (DXFReader.DrawItem entity : dxfReader14.entities) {
            System.out.println(entity);

            if (entity instanceof DXFReader.LwPolyline) {
                DXFReader.LwPolyline polyline = (DXFReader.LwPolyline) entity;

                NestPath nestPath = new NestPath();
                polyline.segments.forEach(segment -> {
                    nestPath.add(segment.dx / dxfReader14.uScale, segment.dy / dxfReader14.uScale);
                });

                listOfListOfPoints.add(new DxfPart(entity, nestPath));
            }
        }

        return listOfListOfPoints;
    }

    private static NestPath getBinPolygon() {
        NestPath binPolygon = new NestPath();
        double width = 300;
        double height = 300;
        binPolygon.add(0, 0);
        binPolygon.add(0, height);
        binPolygon.add(width, height);
        binPolygon.add(width, 0);
        return binPolygon;
    }
}
