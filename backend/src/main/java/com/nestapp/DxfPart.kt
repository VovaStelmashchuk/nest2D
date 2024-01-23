package com.nestapp

import com.nestapp.dxf.DXFReader
import com.nestapp.nest.data.NestPath
import com.nestapp.nest.data.Placement

data class DxfPart(
    val entity: DXFReader.Entity,
    val nestPath: NestPath,
) {

    val bid: Int
        get() = nestPath.bid
}

data class DxfPartPlacement(
    val entity: DXFReader.Entity,
    val nestPath: NestPath,
    val placement: Placement,
)
