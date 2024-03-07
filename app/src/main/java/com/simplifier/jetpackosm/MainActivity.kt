package com.simplifier.jetpackosm

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.simplifier.jetpackosm.ui.theme.JetpackOSMTheme
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration.*
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.library.R
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider


class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
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

                    val startLocation = remember { mutableStateOf("") }
                    val endLocation = remember { mutableStateOf("") }

                    val clearButtonEnabled = remember {
                        mutableStateOf(false)
                    }
                    val searchButtonEnabled =
                        remember { mutableStateOf(false) }

                    val buttonText = remember { mutableStateOf("") }

                    val tower = GeoPoint(14.5862583, 121.0595029, 17.0)

                    val scope = rememberCoroutineScope()

                    LaunchedEffect(key1 = startLocation.value, key2 = endLocation.value) {
                        clearButtonEnabled.value =
                            startLocation.value.isNotBlank() || endLocation.value.isNotBlank()
                        searchButtonEnabled.value =
                            startLocation.value.isNotBlank() && endLocation.value.isNotBlank()
                        buttonText.value =
                            if (startLocation.value.isBlank() && endLocation.value.isBlank()) {
                                "Add Coordinates"
                            } else {
                                "Check Route"
                            }
                    }


                    val map = remember { MapView(context) }

                    Box(modifier = Modifier.fillMaxSize()) {
                        AndroidView(
                            factory = {
                                map.apply {
                                    setTileSource(TileSourceFactory.MAPNIK)
                                    zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                                    setMultiTouchControls(true)
                                }


                                /**
                                 * Init default zoom
                                 */
                                val mapController = map.controller
                                mapController.setZoom(18.5)
                                mapController.setCenter(tower)

                                /**
                                 * Add compass
                                 */
                                val compassOverlay =
                                    CompassOverlay(
                                        context,
                                        InternalCompassOrientationProvider(context),
                                        map
                                    )
                                compassOverlay.enableCompass()
                                map.overlays.add(compassOverlay)

                                /**
                                 * Get coordinate upon map tap
                                 */
                                val mapEventsReceiver: MapEventsReceiver =
                                    object : MapEventsReceiver {
                                        override fun singleTapConfirmedHelper(geoPoint: GeoPoint): Boolean {
                                            val type: MarkerType
                                            if (startLocation.value.isBlank()) {
                                                type = MarkerType.START
                                                startLocation.value =
                                                    "${geoPoint.latitude},${geoPoint.longitude}"
                                            } else {
                                                type = MarkerType.END
                                                endLocation.value =
                                                    "${geoPoint.latitude},${geoPoint.longitude}"
                                            }

                                            scope.launch {
                                                clearMarkersFromMap(map)
                                                addMarkerToMap(context, map, geoPoint, type)
                                            }

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

                                map
                            }
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(12.dp)
                                .align(Alignment.BottomCenter),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            OutlinedTextField(
                                enabled = false,
                                maxLines = 1,
                                value = startLocation.value,
                                onValueChange = {
                                    startLocation.value = it
                                })

                            OutlinedTextField(
                                enabled = false,
                                maxLines = 1,
                                value = endLocation.value,
                                onValueChange = {
                                    endLocation.value = it
                                })

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Button(
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        scope.launch {
                                            //clear all overlays
                                            startLocation.value = ""
                                            endLocation.value = ""

                                            clearMarkersFromMap(map, true)
                                        }

                                    }, enabled = clearButtonEnabled.value
                                ) {
                                    Text(text = "Clear")
                                }

                                Button(modifier = Modifier.weight(1f), onClick = {
                                    //api call
                                }, enabled = searchButtonEnabled.value) {
                                    Text(text = buttonText.value)
                                }
                            }

                        }
                    }
                }
            }
        }
    }
}

enum class MarkerType {
    START,
    END
}

fun addMarkerToMap(
    context: Context,
    mapView: MapView,
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

    val markerOverlay = mutableListOf<Marker>()

    for (overlay in mapView.overlays) {
        if (overlay is Marker) {
            markerOverlay.add(overlay)
        }
    }

    mapView.invalidate()
}

fun clearMarkersFromMap(mapView: MapView, shouldClearAll: Boolean = false) {
    val markerOverlay = mutableListOf<Marker>()

    for (overlay in mapView.overlays) {
        if (overlay is Marker) {
            markerOverlay.add(overlay)
        }
    }

    for (marker in markerOverlay) {
        if (shouldClearAll || marker.relatedObject == MarkerType.END) {
            marker.infoWindow.close()
            mapView.overlays.remove(marker)
        }
    }

    mapView.invalidate()
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
        mapView.zoomToBoundingBox(boundingBox, true, 150)
    }
}

/**
 * Add detailed route
 */
fun drawLine() {
//    val route = Polyline(map)
//    getWaypoints().forEach {
//        line.addPoint(it)
//    }
//    map.overlays.add(route)
}

/**
 * Sample waypoint
 */

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
