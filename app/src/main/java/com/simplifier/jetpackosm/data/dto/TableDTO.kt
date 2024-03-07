package com.simplifier.jetpackosm.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class TableDTO(
    val code: String? = null,
    val destinations: List<WaypointDTO>? = emptyList(),
    val distances: List<List<Double>>? = emptyList(),
    val sources: List<WaypointDTO>? = emptyList(),
    val message: String? = null
)