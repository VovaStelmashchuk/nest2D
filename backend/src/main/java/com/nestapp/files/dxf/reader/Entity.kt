package com.nestapp.files.dxf.reader

import java.awt.geom.Path2D

open class Entity internal constructor(@JvmField var type: String) {
    open fun addParam(gCode: Int, value: String) {
    }

    open fun addChild(child: Entity?) {
    }

    open fun close() {
    }

    open fun toPath2D(): Path2D.Double {
        throw UnsupportedOperationException("Not implemented")
    }
}
