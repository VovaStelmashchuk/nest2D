package com.nestapp.nest.jaguar

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Request(
    @SerialName("uuid")
    val uuid: String = "uuid-2",
    @SerialName("input")
    val input: Input,
    @SerialName("config")
    val config: Config,
) {
    @Serializable
    data class Input(
        @SerialName("Name")
        val name: String,
        @SerialName("Items")
        val items: List<Item>,
        @SerialName("Objects")
        val bins: List<Bin>,
    ) {
        @Serializable
        data class Item(
            @SerialName("Demand")
            val demand: Int,
            @SerialName("AllowedOrientations")
            val allowedOrientations: List<Int> = listOf(0, 90, 180, 270),
            @SerialName("Shape")
            val shape: Shape,
        ) {
            @Serializable
            data class Shape(
                @SerialName("Type")
                val type: String = "SimplePolygon",
                @SerialName("Data")
                val points: List<List<Double>>,
            )
        }

        @Serializable
        data class Bin(
            @SerialName("Cost")
            val cost: Int,
            @SerialName("Stock")
            val stock: Int,
            @SerialName("Zones")
            val zones: List<Unit> = emptyList(),
            @SerialName("Shape")
            val shapes: Shape,
        ) {
            @Serializable
            data class Shape(
                @SerialName("Type")
                val type: String = "Polygon",
                @SerialName("Data")
                val data: Data
            ) {
                @Serializable
                data class Data(
                    @SerialName("Outer")
                    val outer: List<List<Double>>,
                    @SerialName("Inner")
                    val inner: List<List<Double>> = listOf(),
                )
            }
        }
    }

    @Serializable
    data class Config(
        @SerialName("cde_config")
        val cdeConfig: CdeConfig,
        @SerialName("poly_simpl_tolerance")
        val polySimplTolerance: Double = 0.001,
        @SerialName("prng_seed")
        val prngSeed: Int = 0,
        @SerialName("n_samples")
        val nSamples: Int = 50000,
        @SerialName("ls_frac")
        val lsFrac: Double = 0.2,
    ) {
        @Serializable
        data class CdeConfig(
            @SerialName("quadtree_depth")
            val quadtreeDepth: Int = 5,
            @SerialName("hpg_n_cells")
            val hpgNCells: Int = 2000,
            @SerialName("item_surrogate_config")
            val itemSurrogateConfig: ItemSurrogateConfig,
        ) {

            @Serializable
            data class ItemSurrogateConfig(
                @SerialName("pole_coverage_goal")
                val poleCoverageGoal: Double = 0.9,
                @SerialName("max_poles")
                val maxPoles: Int = 10,
                @SerialName("n_ff_poles")
                val nFfPoles: Int = 2,
                @SerialName("n_ff_piers")
                val nFfPiers: Int = 0,
            )
        }
    }
}
