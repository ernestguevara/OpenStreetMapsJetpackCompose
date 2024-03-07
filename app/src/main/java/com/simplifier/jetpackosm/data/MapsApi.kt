package com.simplifier.jetpackosm.data

import com.simplifier.jetpackosm.data.dto.NearestDTO
import com.simplifier.jetpackosm.data.dto.RoutesDTO

interface MapsApi {
    suspend fun getNearest(coordinate: String): NearestDTO?
    suspend fun getRoute(combinedCoordinates: String): RoutesDTO?
}