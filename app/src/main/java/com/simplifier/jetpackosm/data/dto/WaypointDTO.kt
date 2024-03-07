package com.simplifier.jetpackosm.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class WaypointDTO(
    val distance: Double,
    val hint: String,
    val location: List<Double>,
    val name: String,
)