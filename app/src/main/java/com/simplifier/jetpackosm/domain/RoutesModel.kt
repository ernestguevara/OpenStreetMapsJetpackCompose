package com.simplifier.jetpackosm.domain

data class RoutesModel(
    val coordinates: List<List<Double>>,
    val duration: Double,
    val distance: Double
)