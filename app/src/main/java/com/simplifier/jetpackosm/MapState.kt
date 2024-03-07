package com.simplifier.jetpackosm

import com.simplifier.jetpackosm.domain.RoutesModel

data class MapState(
    var loading: Boolean = false,
    var isError: Boolean = false,
    var errorMessage: String = "",
    var routesModel: RoutesModel = RoutesModel(emptyList(), 0.0, 0.0)
)