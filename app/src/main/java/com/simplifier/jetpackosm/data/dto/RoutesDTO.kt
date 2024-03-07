package com.simplifier.jetpackosm.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class RoutesDTO(
    val code: String? = null,
    val routes: List<RouteDTO>? = null,
    val waypoints: List<WaypointDTO>? = null,
    val message: String? = null
)