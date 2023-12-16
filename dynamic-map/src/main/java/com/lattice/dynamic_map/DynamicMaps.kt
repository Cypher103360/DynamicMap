package com.lattice.dynamic_map

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionsRequired
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.google.maps.model.DirectionsResult

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(originIcon: Int, destinationIcon: Int,destination: LatLng, mapApiKey: String) {
    val viewModel: MainViewModel = viewModel()

    // Office 28.52785085785891, 77.28170674108607
    // Lotus temple 28.55389154632741, 77.25876663304068
    // PG 28.535502, 77.267124
    //val destination = LatLng(28.527976656441318, 77.28173217799474)
    val builder = LatLngBounds.builder()
    //val cameraPositionState = rememberCameraPositionState(null)
    viewModel.getCurrentLocation()
    viewModel.startLocationUpdates()


    viewModel.origin?.let {
        builder.include(it)
    }
    builder.include(destination)

    val directionsResult = remember {
        mutableStateOf<DirectionsResult?>(null)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(builder.build().center, 14f)
    }


    val multiplePermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    )

    LaunchedEffect(key1 = viewModel.origin) {
        if (multiplePermissionState.allPermissionsGranted) {
            val result = viewModel.origin?.let {

                // Moving camera
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newCameraPosition(
                        CameraPosition(LatLng(it.latitude, it.longitude), 18f, 0f, 0f)
                    )
                )

                // Live directions
                viewModel.getDirections(
                    mapApiKey,
                    it, destination
                )
            }
            directionsResult.value = result
        }
        // Check if the user has reached the destination
        if (!viewModel.destinationReached && viewModel.isDestinationReached(destination)) {
            viewModel.showToast("You have reached the destination!")
            // Set the flag to prevent showing the toast multiple times
            viewModel.destinationReached = true
        }
    }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Text(
                text = "Dynamic Maps",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(32.dp))
            PermissionsRequired(
                multiplePermissionsState = multiplePermissionState,
                permissionsNotGrantedContent = { /* ... */ },
                permissionsNotAvailableContent = { /* ... */ }
            ) {
                GoogleMap(
                    cameraPositionState = cameraPositionState,
                    modifier = Modifier.weight(1f),
                    properties = MapProperties(isMyLocationEnabled = true),
                    uiSettings = MapUiSettings(
                        compassEnabled = true,
                        indoorLevelPickerEnabled = false,
                        myLocationButtonEnabled = true
                    )
                ) {
                    viewModel.origin?.let { userLocation ->
                        GoogleMarkers(origin = userLocation, destination = destination, viewModel,originIcon,destinationIcon)
                    }
                    directionsResult.value?.routes?.forEach { route ->
                        Polyline(
                            points = route.overviewPolyline.decodePath().map {
                                LatLng(it.lat, it.lng)
                            },
                            color = Color(0xFF2196F3),
                            jointType = JointType.DEFAULT,
                            width = 12f
                        )
                    }
                }
            }
        }
}

@Composable
fun GoogleMarkers(origin: LatLng, destination: LatLng, viewModel: MainViewModel,originIcon: Int, destinationIcon: Int) {
    val originMarkerState = rememberMarkerState(position = origin)
    originMarkerState.position = origin

    Marker(
        flat = true,
        state = originMarkerState,
        title = "Origin",
        snippet = "Origin of the location",
        icon = viewModel.bitmapDescriptorFromVector(
            LocalContext.current,
            originIcon,
            160,
            160
        ),
        anchor = Offset(0.5f, 0.5f),
        rotation = viewModel.bearing
    )
    Marker(
        state = rememberMarkerState(position = destination),
        title = "Lotus Temple",
        snippet = "The Lotus temple",
        icon = viewModel.bitmapDescriptorFromVector(
            LocalContext.current,
            destinationIcon,
            140,
            140
        )
    )
}