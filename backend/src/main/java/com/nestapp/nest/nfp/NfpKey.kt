package com.nestapp.nest.nfp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NfpKey(
    @SerialName("aBid")
    val aBid: String,
    @SerialName("bBid")
    val bBid: String,
    @SerialName("isInside")
    val isInside: Boolean,
    @SerialName("arotation")
    val arotation: Int,
    @SerialName("brotation")
    val brotation: Int
)
