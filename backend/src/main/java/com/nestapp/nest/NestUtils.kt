package com.nestapp.nest

import com.nestapp.nest.data.NestPath
import com.nestapp.nest.data.PathPlacement

object NestUtils {

    fun findNestPartById(tree: List<NestPath>, pathPlacement: PathPlacement): NestPath? {
        return tree.find { it.id == pathPlacement.id }
    }

}
