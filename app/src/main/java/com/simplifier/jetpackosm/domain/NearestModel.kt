package com.simplifier.jetpackosm.domain

data class NearestModel(
    val nearestGpsData: List<Pair<Double, List<Double>>>,
)