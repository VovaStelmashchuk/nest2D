package com.nestapp.nest.nfp

import com.nestapp.nest.data.NestPath

object NfpUtils {

    fun createNfpPairs(placelist: List<NestPath>, binPolygon: NestPath, rotations: List<Int>): MutableList<NfpPair> {
        val nfpPairs: MutableList<NfpPair> = ArrayList()
        for (i in placelist.indices) {
            // Add bin polygon to pair of each path

            val part: NestPath = placelist.get(i)
            val key = NfpKey(binPolygon.getId(), part.id, true, 0, part.rotation)
            nfpPairs.add(NfpPair(binPolygon, part, key))

            // Fill all combination of nest paths
            for (j in 0 until i) {
                val placed: NestPath = placelist.get(j)
                val keyed = NfpKey(placed.id, part.id, false, rotations.get(j), rotations.get(i))
                nfpPairs.add(NfpPair(placed, part, keyed))
            }
        }

        return nfpPairs
    }

}
