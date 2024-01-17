package com.qunhe.util;

import com.app.DxfPart;
import com.jsevy.jdxf.DXFDocument;
import com.jsevy.jdxf.parts.DXFLWPolyline;
import com.jsevy.jdxf.parts.RealPoint;
import com.qunhe.util.nest.Nest;
import com.qunhe.util.nest.config.Config;
import com.qunhe.util.nest.data.NestPath;
import com.qunhe.util.nest.data.Placement;
import com.qunhe.util.nest.util.SvgUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static com.qunhe.util.nest.util.IOUtils.saveSvgFile;

class Main {

    public static void main(String[] args) throws Exception {
        List<DxfPart> listOfDxfParts = new ArrayList<>();
        listOfDxfParts.addAll(
                getEntitiesFromFile("/Users/vovastelmashchuk/Desktop/dxf_app/Nest4J/input/1x1.dxf")
        );

        listOfDxfParts.addAll(
                getEntitiesFromFile("/Users/vovastelmashchuk/Desktop/dxf_app/Nest4J/input/1x4.dxf")
        );

        NestPath binPolygon = getBinPolygon();

        List<NestPath> list = new ArrayList<>();

        listOfDxfParts.forEach(dxfPart -> {
            list.add(dxfPart.nestPath);
        });

        Config config = new Config();
        config.USE_HOLE = false;
        config.SPACING = 1.5;

        Nest nest = new Nest(binPolygon, list, config, 10);
        List<List<Placement>> appliedPlacement = nest.startNest();

        writeToDxf(appliedPlacement, listOfDxfParts, "test.dxf");

        List<String> strings = SvgUtil.svgGenerator(list, appliedPlacement, 300, 300);
        saveSvgFile(strings, Config.OUTPUT_DIR + "test.svg");
    }

    private static void writeToDxf(
            List<List<Placement>> appliedPlacement,
            List<DxfPart> listOfDxfParts,
            String fileName
    ) throws IOException {
        DXFDocument document = new DXFDocument();
        final List<Placement> firstPlacement = appliedPlacement.get(0);

        firstPlacement.forEach(placement -> {
            final DxfPart dxfPart = getNestPathByBid(placement.bid, listOfDxfParts);

            assert dxfPart != null;
            DXFReader.LwPolyline part = (DXFReader.LwPolyline) dxfPart.entity;

            Vector<RealPoint> vertices = new Vector<>();

            System.out.println("id: " + placement.bid + " translate: " + placement.translate + "rotation " + placement.rotate);

            part.segments.forEach(segment -> {
                vertices.add(
                        new RealPoint(
                                segment.dx + (placement.translate.x * 0.039370078740157),
                                segment.dy + (placement.translate.y * 0.039370078740157),
                                0.0)
                );
            });

            DXFLWPolyline translated = new DXFLWPolyline(vertices.size(), vertices, true);
            document.addEntity(translated);
        });

        String dxfText = document.toDXFString();
        String filePath = Config.OUTPUT_DIR + fileName;
        FileWriter fileWriter = new FileWriter(filePath);
        fileWriter.write(dxfText);
        fileWriter.flush();
        fileWriter.close();
    }

    private static DxfPart getNestPathByBid(int bid, List<DxfPart> list) {
        for (DxfPart nestPath : list) {
            if (nestPath.getBid() == bid) {
                return nestPath;
            }
        }
        return null;
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
                polyline.close();

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
