package com.nestapp.nest

import com.nestapp.nest.data.NestPath
import com.nestapp.nest.util.GeometryUtil

fun generateNestListVariants(tree: List<NestPath>): List<List<NestPath>> {
    val copyOfTree = generateNestListRotation(tree)

    val variants: MutableList<List<NestPath>> = ArrayList()
    generateCombinations(
        paths = copyOfTree,
        angles = listOf(0, 90, 180),
        result = variants
    )

    return variants
}

private fun generateCombinations(
    paths: List<NestPath>,
    angles: List<Int>,
    index: Int = 0,
    currentCombination: MutableList<NestPath> = mutableListOf(),
    result: MutableList<List<NestPath>> = mutableListOf()
) {
    if (result.size > 1000)  {
        return
    }
    if (index == paths.size) {
        result.add(currentCombination.toList())
        return
    }

    for (angle in angles) {
        val newPath = NestPath(paths[index])
        newPath.rotation = angle
        val updatedCombination = currentCombination.toMutableList()
        updatedCombination.add(newPath)
        generateCombinations(paths, angles, index + 1, updatedCombination, result)
    }
}

private fun generateNestListRotation(tree: List<NestPath>): List<NestPath> {
    for (nestPath in tree) {
        nestPath.area = GeometryUtil.polygonArea(nestPath)
    }
    return tree.sorted()
}
