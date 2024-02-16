package com.nestapp.nest.util

import com.nestapp.nest.data.NestPath

object NewCommonUtils {

    fun copyNestPaths(parts: List<NestPath>): List<NestPath> {
        return buildList {
            parts.forEach { nestPath ->
                this.add(NestPath(nestPath))
            }
        }
    }
}
