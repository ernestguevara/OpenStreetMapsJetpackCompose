package com.simplifier.jetpackosm.presentation.screens

import android.util.Log
import android.widget.Toast
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.gson.Gson
import com.simplifier.jetpackosm.presentation.MainViewModel
import com.simplifier.jetpackosm.presentation.MapState
import com.simplifier.jetpackosm.presentation.api.MapsManagerImpl
import com.simplifier.jetpackosm.presentation.api.MarkerType
import com.simplifier.jetpackosm.presentation.composables.LoadingOverlay
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationScreen(mainViewModel: MainViewModel) {
    val context = LocalContext.current

    val map = remember { MapView(context) }
    val mapsManager = MapsManagerImpl(context, map)

    val mapStates: MapState by mainViewModel.mapStates.collectAsState()

    val scope = rememberCoroutineScope()

    val buttonsEnabled = remember {
        mutableStateOf(false)
    }

    val buttonText = remember {
        mutableStateOf("")
    }


    val userLocation = remember {
        mutableStateOf("")
    }

    val userCoordinate = remember {
        mutableStateOf(GeoPoint(0.0, 0.0))
    }

    LaunchedEffect(key1 = userLocation.value) {
        buttonsEnabled.value = userLocation.value.isNotBlank()

        buttonText.value =
            if (userLocation.value.isBlank()) {
                "Select Location"
            } else {
                "Check Nearest"
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
                 * Add detailed route to the nearest station
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
                        userLocation.value = "${geoPoint.latitude},${geoPoint.longitude}"
                        userCoordinate.value = geoPoint
                        mapsManager.clearMarkersFromMap(fromBus = true)
                        mapsManager.addMarkerToMap(geoPoint, MarkerType.START)
                        Log.i("ernesthor24", "StationScreen: ${Gson().toJson(geoPoint)}")
                    }
                    getBusStationsList().forEach {
                        addMarkerToMap(it, MarkerType.STATIONS)
                    }
                }

                val mapController = map.controller
                val boundingBox = BoundingBox(
                    getBusStationsList().first().latitude,
                    getBusStationsList().last().longitude,
                    getBusStationsList().last().latitude,
                    getBusStationsList().first().longitude
                )

                mapController.setZoom(13.0)
                mapController.setCenter(boundingBox.centerWithDateLine)

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
                text = "Tap inside the map to input your location"
            )

            OutlinedTextField(modifier = Modifier
                .fillMaxWidth(),
                supportingText = {
                    Text(text = "User Location")
                },
                readOnly = true,
                maxLines = 1,
                value = userLocation.value,
                onValueChange = {
                    userLocation.value = it
                })

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        scope.launch {
                            userLocation.value = ""

                            //clear all overlays
                            mapsManager.clearMarkersFromMap(fromBus = true)

                            mainViewModel.clearPolyline()
                            mapsManager.clearPolyline()
                        }

                    }, enabled = buttonsEnabled.value
                ) {
                    Text(text = "Clear")
                }

                Button(modifier = Modifier.weight(1f), onClick = {
                    mainViewModel.findNearestStation(userCoordinate.value)
                }, enabled = buttonsEnabled.value) {
                    Text(text = buttonText.value)
                }
            }
        }

        if (mapStates.loading) {
            LoadingOverlay()
        }
    }
}

fun getBusStationsList(): ArrayList<GeoPoint> {
    return arrayListOf(
        GeoPoint(14.6574236, 120.9862885), //mento
        GeoPoint(14.6571744, 120.9951861), //bagong barrio
        GeoPoint(14.6571821, 121.0098922), //kaingin (landers)
        GeoPoint(14.6574221, 121.0189602), //munoz
        GeoPoint(14.6506453, 121.0329072), //trinoma
        GeoPoint(14.64741608161117, 121.03542056665356), //vertis
        GeoPoint(14.641563633282004, 121.03941776121053), //centris
        GeoPoint(14.628609212672401, 121.04695179164474), //qmart
        GeoPoint(14.614231536283498, 121.05356415934557), //main ave
        GeoPoint(14.608530166471681, 121.05621300412832), //santolan
        GeoPoint(14.586757888484954, 121.05641996637138), //ortigas
        GeoPoint(14.568380580342335, 121.04599121647362), //guada
        GeoPoint(14.555165900567118, 121.03530655514061), //buendia
        GeoPoint(14.5491716, 121.028119), //ayala
        GeoPoint(14.537921267636023, 121.0038937196656), //tramo
        GeoPoint(14.537535206038484, 120.9997614732278), //taft
        GeoPoint(14.536970280625454, 120.99190323019172), //roxas blvd
        GeoPoint(14.53479905858049, 120.98356068138531), //moa
        GeoPoint(14.529273208271718, 120.98978867571685), //macapagal
        GeoPoint(14.522758767215635, 120.99007898334003), //COD/Manila Bay
        GeoPoint(14.509520456454126, 120.99071061940504) //PITX
    )
}

