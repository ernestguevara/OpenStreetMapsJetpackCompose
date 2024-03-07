package com.simplifier.jetpackosm.data

import com.simplifier.jetpackosm.data.dto.NearestDTO
import com.simplifier.jetpackosm.data.dto.RoutesDTO
import com.simplifier.jetpackosm.data.dto.TableDTO
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.url

class MapsApiImpl : MapsApi {
    private val baseUrl = "https://router.project-osrm.org/"
    private val nearestUrl = "${baseUrl}nearest/v1/driving/"
    private val routeUrl = "${baseUrl}route/v1/driving/"
    private val tableUrl = "${baseUrl}table/v1/driving/"

    override suspend fun getNearest(coordinate: String): NearestDTO? {
        return try {
            KtorClient.client.get {
                url("$nearestUrl$coordinate").apply {
                    parameter("number", "2")
                }
            }
        } catch (e: Exception) {
            return null
        }
    }

    override suspend fun getRoute(combinedCoordinates: String): RoutesDTO? {
        return try {
            KtorClient.client.get {
                url("$routeUrl$combinedCoordinates").apply {
                    parameter("geometries", "geojson")
                }
            }
        } catch (e: Exception) {
            return null
        }
    }

    override suspend fun getNearestStation(combinedCoordinates: String): TableDTO? {
        return try {
            KtorClient.client.get {
                url("$tableUrl$combinedCoordinates").apply {
                    parameter("sources", "0")
                    parameter("destinations", "1;2;3;4;5;6;7;8;9;10;11;12;13;14;15;16;17;18;19;20;21")
                    parameter("annotations", "distance")
                }
            }
        } catch (e: Exception) {
            return null
        }
    }
}