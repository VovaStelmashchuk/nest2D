package com.app;

import com.qunhe.util.DXFReader;
import com.qunhe.util.nest.data.NestPath;

import java.awt.geom.Point2D;
import java.util.List;

public class DxfPart {

    public NestPath nestPath;

    public DXFReader.Entity entity;

    public DxfPart(DXFReader.Entity entity, NestPath nestPath) {
        this.entity = entity;
        this.nestPath = nestPath;
    }

    public int getId() {
        return nestPath.getId();
    }

}
