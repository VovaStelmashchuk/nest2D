package com.nestapp.nest.util

import com.nestapp.nest.data.NestPath

object NewCommonUtils {

    fun copyNestPathsAndSetIds(parts: List<NestPath>): List<NestPath> {
        return buildList {
            parts.forEachIndexed { index, nestPath ->
                val path = NestPath(nestPath)
                path.id = index
                this.add(path)
            }
        }
    }
}
