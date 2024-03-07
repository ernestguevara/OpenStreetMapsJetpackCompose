package com.simplifier.jetpackosm.presentation.screens

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.gson.Gson
import com.simplifier.jetpackosm.presentation.api.MapsManagerImpl
import com.simplifier.jetpackosm.presentation.api.MarkerType
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView

@Composable
fun StationScreen() {
    val context = LocalContext.current

    val map = remember { MapView(context) }
    val mapsManager = MapsManagerImpl(context, map)

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
                        Log.i("ernesthor24", "StationScreen: ${Gson().toJson(geoPoint)}")
                    }
                    getBusStationsList().forEach {
                        addMarkerToMap(it, MarkerType.STATIONS)
                    }
                }

                val mapController = map.controller
                mapController.setZoom(18.5)
                mapController.setCenter(mapsManager.tower)

                map
            }
        )
    }
}

fun getBusStationsList(): ArrayList<GeoPoint> {
    return arrayListOf<GeoPoint>(
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

