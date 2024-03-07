package com.simplifier.jetpackosm.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class NearestDTO(
    val code: String? = null,
    val waypoints: List<WaypointDTO>? = emptyList(),
    val message: String? = null
)