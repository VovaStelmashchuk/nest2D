package com.app;

import com.qunhe.util.DXFReader;
import com.qunhe.util.nest.data.NestPath;

public class DxfPart {

    public NestPath nestPath;

    public DXFReader.Entity entity;

    public DxfPart(DXFReader.Entity entity, NestPath nestPath) {
        this.entity = entity;
        this.nestPath = nestPath;
    }

    public int getBid() {
        return nestPath.getBid();
    }

    @Override
    public String toString() {
        return "DxfPart{" +
                "nestPath=" + nestPath +
                ", entity=" + entity +
                '}';
    }
}
