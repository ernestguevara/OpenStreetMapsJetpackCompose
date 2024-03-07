package com.simplifier.jetpackosm.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class LegDTO(
    val distance: Double,
    val duration: Double,
    val summary: String,
    val weight: Double
)