package com.simplifier.jetpackosm.presentation

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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.gson.Gson
import com.simplifier.jetpackosm.presentation.api.MapsManagerImpl
import com.simplifier.jetpackosm.presentation.api.MarkerType
import com.simplifier.jetpackosm.presentation.ui.theme.JetpackOSMTheme
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration.*
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline

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
                    val mapsManager = MapsManagerImpl(this, map)

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
                                mapsManager.apply {
                                    addCompassOverlay()
                                    addEventReceiver { geoPoint ->
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
                                            mapsManager.clearMarkersFromMap()
                                            mapsManager.addMarkerToMap(geoPoint, type)
                                        }
                                    }
                                }

                                /**
                                 * Init default zoom
                                 */
                                val mapController = map.controller
                                mapController.setZoom(18.5)
                                mapController.setCenter(tower)

                                map
                            }
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(12.dp)
                                .align(Alignment.BottomCenter),
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                text = "Tap inside the map to input coordinates"
                            )

                            OutlinedTextField(modifier = Modifier
                                .fillMaxWidth(),
                                supportingText = {
                                    Text(text = "Start Coordinates")
                                },
                                readOnly = true,
                                maxLines = 1,
                                value = startLocation.value,
                                onValueChange = {
                                    startLocation.value = it
                                })

                            OutlinedTextField(modifier = Modifier
                                .fillMaxWidth(),
                                supportingText = {
                                    Text(text = "End Coordinates")
                                },
                                readOnly = true,
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
                                            mapsManager.clearMarkersFromMap(true)

                                            mainViewModel.clearPolyline()
                                            mapsManager.clearPolyline()
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
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .pointerInput(Unit) {
                                        awaitPointerEventScope {
                                            while (true) {
                                                awaitPointerEvent(pass = PointerEventPass.Initial)
                                                    .changes
                                                    .forEach(PointerInputChange::consume)
                                            }
                                        }
                                    },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Column(
                                    modifier = Modifier
                                        .wrapContentSize()
                                        .background(Color.White)
                                        .padding(16.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .size(64.dp),
                                        strokeWidth = 8.dp,
                                        color = Color.Black
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        fontSize = 16.sp,
                                        text = "Loading..."
                                    )
                                }

                            }
                        }
                    }
                }
            }
        }
    }
}