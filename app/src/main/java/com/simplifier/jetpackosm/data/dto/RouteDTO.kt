package com.simplifier.jetpackosm.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RouteDTO(
    val distance: Double,
    val duration: Double,
    val geometry: GeometryDTO,
    val legs: List<LegDTO>,
    val weight: Double,
    @SerialName("weight_name")
    val weightName: String
)