package com.lattice.dynamic_map

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.android.SphericalUtil
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainViewModel(private val application: Application) : AndroidViewModel(application) {
    //A callback for receiving notifications from the FusedLocationProviderClient.
    private var locationCallback: LocationCallback? = null

    private var fusedLocationClient: FusedLocationProviderClient? = null

    // Property to store the user's current location
    var origin by mutableStateOf<LatLng?>(null)

    fun updateOrigin(lat: Double, long: Double) {
        origin = LatLng(lat, long)
    }

    // Property to store the user's current bearing
    private var _bearing by mutableFloatStateOf(0f)
    val bearing: Float get() = _bearing
    var destinationReached by mutableStateOf(false)

    // Update bearing function
    fun updateBearing(bearing: Float) {
        _bearing = bearing
    }

    fun bitmapDescriptorFromVector(
        context: Context,
        @DrawableRes vectorResId: Int,
        height: Int,
        width: Int
    ): BitmapDescriptor? {
        // retrieve the actual drawable
        val drawable = ContextCompat.getDrawable(context, vectorResId) ?: return null
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        val bitmap = BitmapFactory.decodeResource(context.resources, vectorResId)

        val scaledBitmap = Bitmap.createScaledBitmap(
            bitmap,
            width,
            height,
            false
        )
        val canvas = Canvas(scaledBitmap)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(scaledBitmap)
    }

    fun getCurrentLocation() {
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
                    updateOrigin(location.latitude, location.longitude)
                    // Update bearing for marker rotation
                    updateBearing(location.bearing)
                }
            }
        }
    }

    internal fun startLocationUpdates() {
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

    suspend fun getDirections(
        apiKey: String,
        origin: LatLng,
        destination: LatLng
    ): DirectionsResult? = withContext(Dispatchers.IO) {
        val geoApiContext = GeoApiContext.Builder().apiKey(apiKey).build()
        try {
            DirectionsApi.newRequest(geoApiContext)
                .mode(TravelMode.DRIVING)
                .origin(com.google.maps.model.LatLng(origin.latitude, origin.longitude))
                .destination(
                    com.google.maps.model.LatLng(
                        destination.latitude,
                        destination.longitude
                    )
                )
                .await()
        } catch (e: Exception) {
            null
        }
    }

    fun isDestinationReached(destination: LatLng, radius: Double = 10.0): Boolean {
        val reached =
            origin != null && SphericalUtil.computeDistanceBetween(origin, destination) < radius
        if (reached) {
            destinationReached = true
        }
        return reached
    }

    fun showToast(message: String) {
        Toast.makeText(application, message, Toast.LENGTH_SHORT).show()
    }

    override fun onCleared() {
        stopLocationUpdate()
        super.onCleared()
    }
}