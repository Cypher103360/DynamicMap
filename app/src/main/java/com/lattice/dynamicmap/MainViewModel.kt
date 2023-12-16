package com.lattice.dynamicmap

import android.Manifest
import android.app.Application
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.activity.result.IntentSenderRequest
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.navigation.NavHostController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task

class MainViewModel(private val application: Application) : AndroidViewModel(application) {
    //A callback for receiving notifications from the FusedLocationProviderClient.
    private var locationCallback: LocationCallback? = null

    //The main entry point for interacting with the Fused Location Provider
    val visiblePermissionDialogQueue = mutableStateListOf<String>()
    private var fusedLocationClient: FusedLocationProviderClient? = null

    // Property to store the user's current bearing
    private var _bearing by mutableFloatStateOf(0f)

    // Update bearing function
    fun updateBearing(bearing: Float) {
        _bearing = bearing
    }

    fun dismissDialog() {
        visiblePermissionDialogQueue.removeFirst()
    }

    fun onPermissionResult(
        permission: String,
        isGranted: Boolean
    ) {
        if (!isGranted && !visiblePermissionDialogQueue.contains(permission)) {
            visiblePermissionDialogQueue.add(permission)
        }
    }

    fun checkLocationSetting(
        onDisabled: (IntentSenderRequest) -> Unit,
        onEnabled: () -> Unit
    ) {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)

        val client: SettingsClient = LocationServices.getSettingsClient(application)
        val builder: LocationSettingsRequest.Builder =
            LocationSettingsRequest.Builder().addLocationRequest(locationRequest.build())

        val gpsSettingTask: Task<LocationSettingsResponse> =
            client.checkLocationSettings(builder.build())

        gpsSettingTask.addOnSuccessListener { onEnabled() }
        gpsSettingTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    val intentSenderRequest =
                        IntentSenderRequest.Builder(exception.resolution).build()
                    onDisabled(intentSenderRequest)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // ignore here
                }
            }
        }
    }

    fun navigate(
        navController: NavHostController,
        viewModel: MainViewModel
    ) {
        viewModel.getCurrentLocation()
        navController.navigate("mapScreen")
        viewModel.startLocationUpdates()
    }

    private fun getCurrentLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

        locationCallback = object : LocationCallback() {
            //1
            override fun onLocationResult(result: LocationResult) {
                /**
                 * Option 1
                 * This option returns the locations computed, ordered from oldest to newest.
                 * */
                for (location in result.locations) {
                    // Update data class with location data
                    Log.d(
                        "Location callback",
                        "Lat: ${location.latitude} - Long: ${location.longitude} Bearing: ${location.bearing}"
                    )
                    // Update bearing for marker rotation
                    updateBearing(location.bearing)
                }
            }
        }
    }

    private fun startLocationUpdates() {
        locationCallback?.let {
            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 100)
                .setWaitForAccurateLocation(true)
                .setMinUpdateIntervalMillis(500)
                .setMaxUpdateDelayMillis(100)
                .build()
            if (ActivityCompat.checkSelfPermission(
                    application,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    application,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            fusedLocationClient?.requestLocationUpdates(
                request,
                it,
                Looper.getMainLooper()
            )
        }
    }

    private fun stopLocationUpdate() {
        try {
            //Removes all location updates for the given callback.
            val removeTask =
                locationCallback?.let { fusedLocationClient?.removeLocationUpdates(it) }
            removeTask?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("LOCATION_TAG", "Location Callback removed.")
                } else {
                    Log.d("LOCATION_TAG", "Failed to remove Location Callback.")
                }
            }
        } catch (se: SecurityException) {
            Log.e("LOCATION_TAG", "Failed to remove Location Callback.. $se")
        }
    }

    override fun onCleared() {
        stopLocationUpdate()
        super.onCleared()
    }
}