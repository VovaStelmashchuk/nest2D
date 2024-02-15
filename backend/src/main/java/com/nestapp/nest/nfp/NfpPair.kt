package com.nestapp.nest.nfp

import com.nestapp.nest.data.NestPath

data class NfpPair(
    val a: NestPath,
    val b: NestPath,
    @JvmField val key: NfpKey
)
