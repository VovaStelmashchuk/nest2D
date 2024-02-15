package com.nestapp.nest.data

import com.nestapp.nest.nfp.NfpKey

class ParallelData {
    @JvmField
    var key: NfpKey? = null

    @JvmField
    var value: List<NestPath>

    constructor() {
        value = ArrayList()
    }

    constructor(key: NfpKey?, value: List<NestPath>) {
        this.key = key
        this.value = value
    }
}
