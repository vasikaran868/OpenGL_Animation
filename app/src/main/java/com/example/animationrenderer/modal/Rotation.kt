package com.example.animationrenderer.modal

import kotlinx.serialization.SerialName

data class Rotation(
    @SerialName("a")
    val isAnimated: Int,
    val ix: Int,
    @SerialName("k")
    val keyFrames: Any
)