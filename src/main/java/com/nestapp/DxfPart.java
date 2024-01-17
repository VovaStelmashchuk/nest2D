package com.nestapp;

import com.nestapp.dxf.DXFReader;
import com.nestapp.nest.data.NestPath;

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
