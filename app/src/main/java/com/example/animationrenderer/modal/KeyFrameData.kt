package com.example.animationrenderer.modal

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class KeyFrameData(
    @SerialName("a")
    val anchor: AnchorData,
    @SerialName("o")
    val opacity: Opacity,
    @SerialName("p")
    val position: Position,
    @SerialName("r")
    val rotation: Rotation,
    @SerialName("s")
    val scale: Scale
)