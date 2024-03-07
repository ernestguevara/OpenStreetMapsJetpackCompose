package com.simplifier.jetpackosm.domain

interface MapsRepository {
    suspend fun getNearest(coordinate: String): NearestModel
    suspend fun getRoutes(combinedCoordinates: String): RoutesModel
}