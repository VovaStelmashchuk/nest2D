package com.nestapp.nest.jaguar

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Response(
    @SerialName("Solution")
    val solution: Solution,
) {
    @Serializable
    data class Solution(
        @SerialName("Layouts")
        val layouts: List<Layout>,
    ) {
        @Serializable
        data class Layout(
            @SerialName("PlacedItems")
            val placedItems: List<PlacedItem>,
        ) {
            @Serializable
            data class PlacedItem(
                @SerialName("Index")
                val index: Int,
                @SerialName("Transformation")
                val transformation: Transformation,
            ) {
                @Serializable
                data class Transformation(
                    @SerialName("Rotation")
                    val rotation: Double,
                    @SerialName("Translation")
                    val translation: List<Double>
                )
            }
        }
    }
}
