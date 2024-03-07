package com.simplifier.jetpackosm

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.gson.Gson
import com.simplifier.jetpackosm.domain.RoutesModel
import com.simplifier.jetpackosm.ui.theme.JetpackOSMTheme
import io.ktor.util.reflect.instanceOf
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
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider


const val TAG = "ernesthor24"

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        val mainViewModel by viewModels<MainViewModel>()

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

                    val startCoordinates = remember { mutableStateOf(GeoPoint(0.0, 0.0)) }
                    val endCoordinates = remember { mutableStateOf(GeoPoint(0.0, 0.0)) }

                    val clearButtonEnabled = remember {
                        mutableStateOf(false)
                    }
                    val searchButtonEnabled =
                        remember { mutableStateOf(false) }

                    val buttonText = remember { mutableStateOf("") }

                    val tower = GeoPoint(14.5862583, 121.0595029, 17.0)

                    val scope = rememberCoroutineScope()

                    val map = remember { MapView(context) }

                    val mapStates: MapState by mainViewModel.mapStates.collectAsState()

                    LaunchedEffect(key1 = startLocation.value, key2 = endLocation.value) {

                        clearButtonEnabled.value =
                            startLocation.value.isNotBlank() || endLocation.value.isNotBlank()

                        searchButtonEnabled.value =
                            startLocation.value.isNotBlank() && endLocation.value.isNotBlank()

                        buttonText.value =
                            if (startLocation.value.isBlank() || endLocation.value.isBlank()) {
                                "Add Coordinates"
                            } else {
                                "Check Route"
                            }
                    }


                    LaunchedEffect(mapStates) {
                        Log.i("ernesthor24", "LaunchedEffect ${Gson().toJson(mapStates)}")

                        when {
                            mapStates.isError -> {
                                Log.i("ernesthor24", "went here toast")
                                Toast.makeText(context, mapStates.errorMessage, Toast.LENGTH_SHORT)
                                    .show()
                            }

                            mapStates.routesModel.coordinates.isNotEmpty() -> {
                                /**
                                 * Add detailed route
                                 */
                                scope.launch {
                                    val route = Polyline(map)
                                    mapStates.routesModel.coordinates.forEach { coordinates ->
                                        route.addPoint(GeoPoint(coordinates[1], coordinates[0]))
                                    }
                                    map.overlays.add(route)
                                    map.invalidate()
                                }
                            }

                            else -> {
                                //do nothing
                            }
                        }
                    }

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
                                                startCoordinates.value = geoPoint
                                            } else {
                                                type = MarkerType.END
                                                endLocation.value =
                                                    "${geoPoint.latitude},${geoPoint.longitude}"
                                                endCoordinates.value = geoPoint
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
                                            startLocation.value = ""
                                            endLocation.value = ""

                                            //clear all overlays
                                            clearMarkersFromMap(map, true)

                                            mainViewModel.clearPolyline()
                                            clearPolyline(map)
                                        }

                                    }, enabled = clearButtonEnabled.value
                                ) {
                                    Text(text = "Clear")
                                }

                                Button(modifier = Modifier.weight(1f), onClick = {
                                    mainViewModel.getRoutes(
                                        startCoordinates.value,
                                        endCoordinates.value
                                    )
                                }, enabled = searchButtonEnabled.value) {
                                    Text(text = buttonText.value)
                                }
                            }

                        }

                        if (mapStates.loading) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.5f)),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(48.dp),
                                    strokeWidth = 2.dp,
                                )
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

fun clearPolyline(mapView: MapView) {
    val polylineOverlay = mutableListOf<Polyline>()

    for (overlay in mapView.overlays) {
        if (overlay is Polyline) {
            polylineOverlay.add(overlay)
        }
    }

    for (line in polylineOverlay) {
        mapView.overlays.remove(line)
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