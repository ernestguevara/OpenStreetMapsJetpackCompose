package com.simplifier.jetpackosm.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class GeometryDTO(
    val coordinates: List<List<Double>>,
    val type: String
)