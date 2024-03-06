package com.simplifier.jetpackosm

import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.simplifier.jetpackosm.ui.theme.JetpackOSMTheme
import org.osmdroid.config.Configuration.*
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.OverlayItem
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import kotlin.math.abs


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))

        setContent {
            JetpackOSMTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current

                    val gumamelaCoordinate = GeoPoint(14.6792085, 120.9889662)
                    val bgcCoordinate = GeoPoint(14.546075468854076, 121.05393451469227)

//                    val gumamelaCoordinate = GeoPoint(14.6183922, 120.9783989)
//                    val bgcCoordinate = GeoPoint(13.0882396, 123.6723769)

                    /**
                     * Init map
                     */
                    val map = MapView(context).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                        setMultiTouchControls(true)
                    }

                    /**
                     * Init default zoom
                     */
                    val mapController = map.controller
                    mapController.setZoom(18.5)
                    mapController.setCenter(gumamelaCoordinate)

                    /**
                     * Add compass
                     */
                    val compassOverlay =
                        CompassOverlay(context, InternalCompassOrientationProvider(context), map)
                    compassOverlay.enableCompass()
                    map.overlays.add(compassOverlay)


                    /**
                     * Add pin overlay
                     */
                    //your items
//                    val items = ArrayList<OverlayItem>()
//                    items.add(OverlayItem("Start", "Valenzuela", gumamelaCoordinate))
//                    items.add(OverlayItem("Finish", "BGC", bgcCoordinate))
//
//                    zoomOutToFitCoordinates(map, gumamelaCoordinate, bgcCoordinate)
//
//                    val pinOverlay = ItemizedOverlayWithFocus(items, object :
//                        ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
//                        override fun onItemSingleTapUp(index: Int, item: OverlayItem): Boolean {
//                            Log.i("ernesthor24", "onItemSingleTapUp: ")
//                            //do something
//                            return true
//                        }
//
//                        override fun onItemLongPress(index: Int, item: OverlayItem): Boolean {
//                            Log.i("ernesthor24", "onItemLongPress: ")
//                            return true
//                        }
//                    }, context)
//                    pinOverlay.setFocusItemsOnTap(true)
//
//                    map.overlays.add(pinOverlay)

                    //new
                    addMarkerToMap(map, "Start", "Valenzuela", gumamelaCoordinate)
                    addMarkerToMap(map, "Finish", "BGC", bgcCoordinate)
                    zoomOutToFitCoordinates(map, gumamelaCoordinate, bgcCoordinate)

                    /**
                     * Add line overlay
                     */
                    val line = Polyline(map)
                    line.addPoint(gumamelaCoordinate)
                    line.addPoint(bgcCoordinate)
                    map.overlays.add(line)

                    /**
                     * Add detailed route
                     */
                    val route = Polyline(map)
                    getWaypoints().forEach {
                        line.addPoint(it)
                    }
                    map.overlays.add(route)

                    /**
                     * Get coordinate
                     */

                    val mapEventsReceiver: MapEventsReceiver = object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(geoPoint: GeoPoint): Boolean {
                            // Handle tap event
                            val latitude = geoPoint.latitude
                            val longitude = geoPoint.longitude
//                            clearMarkersFromMap(map)
                            addMarkerToMap(map, "Tap", "Coordinate", geoPoint)
                            Log.d("TapCoordinate", "Latitude: $latitude, Longitude: $longitude")
                            return true
                        }

                        override fun longPressHelper(geoPoint: GeoPoint): Boolean {
                            // Long press event
                            return true
                        }
                    }

                    // Add map events overlay to the map view
                    val mapEventsOverlay = MapEventsOverlay(mapEventsReceiver)
                    map.overlays.add(mapEventsOverlay)


                    Box(modifier = Modifier.fillMaxSize()) {
                        AndroidView(
                            modifier = Modifier.fillMaxSize(), factory = {
                                map
                            }
                        )

                        Column(
                            modifier = Modifier.wrapContentSize()
                        ) {

                        }
                    }
                }
            }
        }
    }
}

private fun getWaypoints(): ArrayList<GeoPoint> {
    val geoPoints: ArrayList<GeoPoint> = arrayListOf()

    // Hardcoded array of coordinates
    val coordinates = arrayOf(
        doubleArrayOf(14.679103, 120.98906),
        doubleArrayOf(14.676959, 120.98872),
        doubleArrayOf(14.673748, 120.981743),
        doubleArrayOf(14.661711, 120.984098),
        doubleArrayOf(14.638781, 120.983308),
        doubleArrayOf(14.632196, 120.980614),
        doubleArrayOf(14.623724, 120.988979),
        doubleArrayOf(14.613978, 120.988626),
        doubleArrayOf(14.604906, 120.997285),
        doubleArrayOf(14.597418, 121.001188),
        doubleArrayOf(14.583511, 121.002083),
        doubleArrayOf(14.577072, 120.996957),
        doubleArrayOf(14.55766, 121.007383),
        doubleArrayOf(14.561068, 121.015243),
        doubleArrayOf(14.549633, 121.029854),
        doubleArrayOf(14.547238, 121.035665),
        doubleArrayOf(14.547615, 121.040638),
        doubleArrayOf(14.545502, 121.0426),
        doubleArrayOf(14.544814, 121.045706),
        doubleArrayOf(14.542394, 121.04663)
    )

    // Create GeoPoints from the coordinates and add them to the ArrayList

    // Create GeoPoints from the coordinates and add them to the ArrayList
    for (coordinate in coordinates) {
        val location = GeoPoint(coordinate[0], coordinate[1])
        geoPoints.add(location)
    }

    return geoPoints
}

fun addMarkerToMap(mapView: MapView, title: String, snippet: String, coordinate: GeoPoint) {
    val items = ArrayList<OverlayItem>()
    items.add(OverlayItem(title, snippet, coordinate))

    val pinOverlay = ItemizedOverlayWithFocus(items, object :
        ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
        override fun onItemSingleTapUp(index: Int, item: OverlayItem): Boolean {
            // Handle single tap on the marker
            Log.i("ernesthor24", "onItemSingleTapUp: addMarkerToMap")
            return true
        }

        override fun onItemLongPress(index: Int, item: OverlayItem): Boolean {
            // Handle long press on the marker
            Log.i("ernesthor24", "onItemLongPress: addMarkerToMap")
            return true
        }
    }, mapView.context)
    pinOverlay.setFocusItemsOnTap(true)

    mapView.overlays.add(pinOverlay)
}

fun clearMarkersFromMap(mapView: MapView) {
    val overlaysToRemove = mutableListOf<ItemizedOverlayWithFocus<*>?>()
    for (overlay in mapView.overlays) {
        if (overlay is ItemizedOverlayWithFocus<*>) {
            overlaysToRemove.add(overlay)
        }
    }
    for (overlay in overlaysToRemove) {
        mapView.overlays.remove(overlay)
    }
}

/**
 * Zoom depends on the bounds
 */
fun zoomOutToFitCoordinates(mapView: MapView, coordinate1: GeoPoint, coordinate2: GeoPoint) {
    val boundingBox = BoundingBox(
        coordinate1.latitude,
        coordinate2.longitude,
        coordinate2.latitude,
        coordinate1.longitude
    )

    mapView.post {
        mapView.zoomToBoundingBox(boundingBox, true, 100)
    }
}