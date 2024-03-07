package com.simplifier.jetpackosm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simplifier.jetpackosm.data.MapsRepositoryImpl
import com.simplifier.jetpackosm.domain.RoutesModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

class MainViewModel : ViewModel() {

    private val mapsRepository = MapsRepositoryImpl()

    private val _routesModel = MutableStateFlow(RoutesModel(emptyList(), 0.0, 0.0))
    val routesModel: StateFlow<RoutesModel> = _routesModel

    fun getRoutes(
        startCoordinates: GeoPoint,
        endCoordinates: GeoPoint
    ) {
        viewModelScope.launch {
            //trigger loading

            val finalStartCoordinate =
                mapsRepository.getNearest("${startCoordinates.longitude},${startCoordinates.latitude}")
            val finalEndCoordinate =
                mapsRepository.getNearest("${endCoordinates.longitude},${endCoordinates.latitude}")

            if (finalStartCoordinate.nearestGpsData.isNotEmpty() || finalEndCoordinate.nearestGpsData.isNotEmpty()) {

                val finalCoordinates =
                    "${finalStartCoordinate.nearestGpsData.first().second[0]},${finalStartCoordinate.nearestGpsData.first().second[1]};" +
                            "${finalEndCoordinate.nearestGpsData.first().second[0]},${finalEndCoordinate.nearestGpsData.first().second[1]}"

                _routesModel.value = mapsRepository.getRoutes(finalCoordinates)
            }
        }
    }
}