package com.example.animationrenderer.modal

import kotlinx.serialization.SerialName

data class Layer(
    val ao: Int,
    @SerialName("bm")
    val blendMode: Int,
    @SerialName("cl")
    val assestType: String,
    @SerialName("ddd")
    val is3d: Int,
    val ind: Int,
    @SerialName("ip")
    val inPoint: Int,
    @SerialName("ks")
    val KeyFrameData: KeyFrameData,
    @SerialName("nm")
    val LayerName: String,
    @SerialName("op")
    val outPoint: Int,
    @SerialName("refId")
    val assetId: String,
    val sr: Int,
    val st: Int,
    @SerialName("ty")
    val LayerType: Int
)