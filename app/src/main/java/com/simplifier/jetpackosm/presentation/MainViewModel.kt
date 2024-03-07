package com.simplifier.jetpackosm.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simplifier.jetpackosm.data.MapsRepositoryImpl
import com.simplifier.jetpackosm.domain.RoutesModel
import com.simplifier.jetpackosm.presentation.screens.getBusStationsList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

class MainViewModel : ViewModel() {

    private val mapsRepository = MapsRepositoryImpl()

    private val _mapStates = MutableStateFlow(MapState())
    val mapStates: StateFlow<MapState> = _mapStates

    fun getRoutes(
        startCoordinates: GeoPoint,
        endCoordinates: GeoPoint
    ) {
        viewModelScope.launch {
            _mapStates.value = _mapStates.value.copy(
                loading = true,
                isError = false,
                errorMessage = ""
            )

            val finalStartCoordinate =
                mapsRepository.getNearest("${startCoordinates.longitude},${startCoordinates.latitude}")
            val finalEndCoordinate =
                mapsRepository.getNearest("${endCoordinates.longitude},${endCoordinates.latitude}")

            if (finalStartCoordinate.nearestGpsData.isNotEmpty() || finalEndCoordinate.nearestGpsData.isNotEmpty()) {

                val finalCoordinates =
                    "${finalStartCoordinate.nearestGpsData.first().second[0]},${finalStartCoordinate.nearestGpsData.first().second[1]};" +
                            "${finalEndCoordinate.nearestGpsData.first().second[0]},${finalEndCoordinate.nearestGpsData.first().second[1]}"

                _mapStates.value = _mapStates.value.copy(
                    routesModel = mapsRepository.getRoutes(finalCoordinates),
                    isError = false,
                    loading = false,
                    errorMessage = ""
                )
            } else {
                _mapStates.value = _mapStates.value.copy(
                    loading = false,
                    isError = true,
                    errorMessage = "Cant parse anything"
                )
            }
        }
    }

    fun findNearestStation(userCoordinate: GeoPoint) {
        viewModelScope.launch {
            _mapStates.value = _mapStates.value.copy(
                loading = true,
                isError = false,
                errorMessage = ""
            )

            val nearestModel = mapsRepository.getNearestBusStation(iterateCoordinateList(userCoordinate))

            if (nearestModel.distances.isEmpty()) {
                _mapStates.value = _mapStates.value.copy(
                    loading = false,
                    isError = true,
                    errorMessage = "No Data Found!"
                )
            } else {
                val index = findIndexOfMinValue(nearestModel.distances.first())

                val busStation = getBusStationsList()[index]

                val finalCoordinates =
                    "${userCoordinate.longitude},${userCoordinate.latitude};" +
                            "${busStation.longitude},${busStation.latitude}"

                _mapStates.value = _mapStates.value.copy(
                    routesModel = mapsRepository.getRoutes(finalCoordinates),
                    isError = false,
                    loading = false,
                    errorMessage = ""
                )
            }
        }
    }

    fun clearPolyline() {
        _mapStates.value = _mapStates.value.copy(
            routesModel = RoutesModel(emptyList(), 0.0, 0.0)
        )
    }

    private fun iterateCoordinateList(geoPoint: GeoPoint): String {
        val geoPointList = arrayListOf<GeoPoint>()
        geoPointList.add(geoPoint)
        geoPointList.addAll(getBusStationsList())

        var finalCoordinateString = ""

        geoPointList.forEachIndexed { index, coordinate ->
            finalCoordinateString += if (index == geoPointList.size - 1) {
                "${coordinate.longitude},${coordinate.latitude}"
            } else {
                "${coordinate.longitude},${coordinate.latitude};"
            }
        }

        return finalCoordinateString
    }

    fun findIndexOfMinValue(distances: List<Double>): Int {
        var minValue = Double.MAX_VALUE
        var minIndex = -1

        for (i in distances.indices) {
            if (distances[i] < minValue) {
                minValue = distances[i]
                minIndex = i
            }
        }

        return minIndex
    }
}