package com.simplifier.jetpackosm.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun MainScreen(onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Button(onClick = { onNavigate("plotting_screen") }) {
            Text(text = "Go to Route Plotter")
        }

        Button(onClick = { onNavigate("stations_screen") }) {
            Text(text = "Go to Stations Finder")
        }
    }
}