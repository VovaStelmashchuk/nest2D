package com.nestapp.files.dxf.reader

import com.nestapp.files.dxf.writter.parts.DXFEntity
import com.nestapp.nest.NotMovedPlacement
import com.nestapp.nest.Placement
import java.awt.geom.Path2D

open class Entity internal constructor(@JvmField var type: String) {
    open fun addParam(gCode: Int, value: String) {
    }

    open fun addChild(child: Entity?) {
    }

    open fun close() {
    }

    open fun toPath2D(): Path2D.Double {
        throw UnsupportedOperationException("Not implemented in ${this.javaClass.simpleName}")
    }

    open fun isClose(): Boolean {
        throw UnsupportedOperationException("Not implemented in ${this.javaClass.simpleName}")
    }

    open fun toWriterEntity(placement: Placement = NotMovedPlacement): DXFEntity {
        throw UnsupportedOperationException("Not implemented in ${this.javaClass.simpleName}")
    }

    open fun translate(x: Double, y: Double): Entity {
        throw UnsupportedOperationException("Not implemented in ${this.javaClass.simpleName}")
    }
}
