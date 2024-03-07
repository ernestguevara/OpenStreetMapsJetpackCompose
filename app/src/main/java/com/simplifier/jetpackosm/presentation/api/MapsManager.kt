package com.simplifier.jetpackosm.presentation.api

import org.osmdroid.util.GeoPoint

interface MapsManager {
    fun addCompassOverlay()

    fun addEventReceiver(onSingleTap: (GeoPoint) -> Unit)

    fun addMarkerToMap(
        coordinate: GeoPoint,
        type: MarkerType
    )

    fun clearMarkersFromMap(shouldClearAll: Boolean = false)

    fun clearPolyline()

    fun zoomOutToFitCoordinates(coordinate1: GeoPoint, coordinate2: GeoPoint)
}

enum class MarkerType {
    START,
    END,
    STATIONS
}