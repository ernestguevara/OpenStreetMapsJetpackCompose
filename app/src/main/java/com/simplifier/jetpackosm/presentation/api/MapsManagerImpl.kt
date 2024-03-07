package com.simplifier.jetpackosm.presentation.api

import android.content.Context
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.library.R
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider

class MapsManagerImpl(private val context: Context, private val mapView: MapView) : MapsManager {

    /**
     * Add compass
     */
    override fun addCompassOverlay() {
        val compassOverlay =
            CompassOverlay(
                context,
                InternalCompassOrientationProvider(context),
                mapView
            )
        compassOverlay.enableCompass()
        mapView.overlays.add(compassOverlay)
    }
    /**
     * Get coordinate upon map tap
     */
    override fun addEventReceiver(onSingleTap: (GeoPoint) -> Unit) {
        val mapEventsReceiver: MapEventsReceiver =
            object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(geoPoint: GeoPoint): Boolean {
                    onSingleTap(geoPoint)
                    return true
                }

                override fun longPressHelper(geoPoint: GeoPoint): Boolean {
                    // Long press event
                    return true
                }
            }

        // Add map events overlay to the map view
        val mapEventsOverlay = MapEventsOverlay(mapEventsReceiver)
        mapView.overlays.add(mapEventsOverlay)
    }

    /**
     * Add markers
     */
    override fun addMarkerToMap(
        coordinate: GeoPoint,
        type: MarkerType
    ) {
        val marker = Marker(mapView)
        marker.position = coordinate
        marker.title = if (type == MarkerType.START) {
            "Start Destination"
        } else {
            "End Destination"
        }
        marker.snippet = "${coordinate.latitude},${coordinate.longitude}"
        marker.relatedObject = type
        marker.icon = if (type == MarkerType.START) {
            context.getDrawable(R.drawable.person)
        } else {
            context.getDrawable(R.drawable.marker_default)
        }
        marker.setOnMarkerClickListener { marker, mapView ->
            marker.showInfoWindow()
            true
        }

        mapView.overlays.add(marker)
        mapView.invalidate()
    }


    /**
     * Clear markers
     */
    override fun clearMarkersFromMap(shouldClearAll: Boolean) {
        mapView.overlays.forEach {
            if (it is Marker) {
                if (shouldClearAll || it.relatedObject == MarkerType.END) {
                    it.infoWindow.close()
                    mapView.overlays.remove(it)
                }
            }
        }

        mapView.invalidate()
    }


    /**
     * Clear polyline
     */
    override fun clearPolyline() {
        mapView.overlays.forEach {
            if (it is Polyline) {
                mapView.overlays.remove(it)
            }
        }

        mapView.invalidate()
    }

    /**
     * Zoom depends on the bounds
     */
    override fun zoomOutToFitCoordinates(coordinate1: GeoPoint, coordinate2: GeoPoint) {
        val boundingBox = BoundingBox(
            coordinate1.latitude,
            coordinate2.longitude,
            coordinate2.latitude,
            coordinate1.longitude
        )

        mapView.post {
            mapView.zoomToBoundingBox(boundingBox, true, 150)
        }
    }
}