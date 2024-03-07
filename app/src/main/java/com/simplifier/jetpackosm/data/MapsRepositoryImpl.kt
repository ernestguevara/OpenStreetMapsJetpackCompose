package com.simplifier.jetpackosm.data

import com.simplifier.jetpackosm.domain.MapsRepository
import com.simplifier.jetpackosm.domain.NearestModel
import com.simplifier.jetpackosm.domain.NearestStationModel
import com.simplifier.jetpackosm.domain.RoutesModel

class MapsRepositoryImpl : MapsRepository {

    private val mapsApi = MapsApiImpl()

    override suspend fun getNearest(coordinate: String): NearestModel {
        return NearestModel(nearestGpsData = mapsApi.getNearest(coordinate)?.waypoints?.map {
            Pair(it.distance, it.location)
        } ?: run {
            emptyList()
        })
    }

    override suspend fun getRoutes(combinedCoordinates: String): RoutesModel {
        return mapsApi.getRoute(combinedCoordinates)?.routes?.first()?.run {
            RoutesModel(
                coordinates = geometry.coordinates,
                duration = duration,
                distance = distance
            )
        } ?: run {
            RoutesModel(
                coordinates = emptyList(),
                duration = 0.0,
                distance = 0.0
            )
        }
    }

    override suspend fun getNearestBusStation(combinedCoordinates: String): NearestStationModel {
        return mapsApi.getNearestStation(combinedCoordinates)?.distances?.run {
            NearestStationModel(this)
        } ?: run {
            NearestStationModel(emptyList())
        }
    }
}