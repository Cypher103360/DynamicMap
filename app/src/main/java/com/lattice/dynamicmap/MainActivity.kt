package com.lattice.dynamicmap

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.maps.model.LatLng
import com.lattice.dynamic_map.MapScreen
import com.lattice.dynamicmap.ui.theme.DynamicMapTheme

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: MainViewModel

    private val permissionToRequest = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DynamicMapTheme {
                viewModel = viewModel()
                Navigation(viewModel = viewModel)
            }
        }
        Log.d("Maps_api_key", BuildConfig.MAPS_API_KEY)
    }


    @Composable
    fun Navigation(viewModel: MainViewModel) {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = "permissionScreen"
        ) {
            composable("permissionScreen") {
                HandlePermissions(navController, viewModel = viewModel)
            }
            composable("mapScreen") {
                //val origin = LatLng(28.52785085785891, 77.28170674108607)
                val destination = LatLng(28.52785085785891, 77.28170674108607)
                val originIcon = R.drawable.origin_icon
                val desIcon = R.drawable.destination
                val key = BuildConfig.MAPS_API_KEY
                MapScreen(originIcon, desIcon, destination, key)
            }
        }
    }

    @Composable
    fun HandlePermissions(
        navController: NavHostController,
        viewModel: MainViewModel
    ) {
        val dialogQueue = viewModel.visiblePermissionDialogQueue
        val settingResultRequest = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartIntentSenderForResult()
        ) { activityResult ->
            if (activityResult.resultCode == RESULT_OK) {
                viewModel.navigate(navController, viewModel)
                Log.d("Granted", "Location Granted")
            } else {
                Log.d("Not Granted", "Location not Granted")
            }
        }

        val multiplePermissionResultLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
            onResult = { perms ->
                val allPermissionsGranted = permissionToRequest.all { perms[it] == true }

                // Do something based on the result
                if (allPermissionsGranted) {
                    // All permissions are granted
                    Log.d("AllGranted", "All permissions are granted.")

                    viewModel.checkLocationSetting(
                        onDisabled = { intentSenderRequest ->
                            settingResultRequest.launch(intentSenderRequest)
                        },
                        onEnabled = {
                            Log.d("Location enabled", "Done")
                            viewModel.navigate(navController, viewModel)

                        })
                } else {
                    // Not all permissions are granted
                    Log.d("AllGranted", "Not all permissions are granted.")
                }

                permissionToRequest.forEach { permission ->
                    viewModel.onPermissionResult(
                        permission = permission,
                        isGranted = perms[permission] == true
                    )
                }
            }
        )

        dialogQueue.reversed().forEach { permission ->
            PermissionDialog(
                permissionTextProvider = getPermissionTextProvider(permission),
                isPermanentlyDeclined = !shouldShowRequestPermissionRationale(permission),
                onDismiss = viewModel::dismissDialog,
                onOkClick = {
                    viewModel.dismissDialog()
                    if (shouldShowRequestPermissionRationale(permission)) {
                        multiplePermissionResultLauncher.launch(
                            arrayOf(permission)
                        )
                    }
                    // The launcher has already been launched, no need to launch it again
                },
                onGoToAppSettingsClick = ::openAppSettings
            )
        }

        // Launch the permission request
        DisposableEffect(Unit) {
            Log.d("PermissionDebug", "Launching permission request.")
            multiplePermissionResultLauncher.launch(permissionToRequest)
            onDispose {
                Log.d("PermissionDebug", "DisposableEffect disposed.")
            }
        }
    }


    @Composable
    fun getPermissionTextProvider(permission: String): PermissionTextProvider {
        return when (permission) {
            Manifest.permission.ACCESS_FINE_LOCATION -> FineLocationPermissionTextProvider()
            else -> throw IllegalArgumentException("Unknown permission: $permission")
        }
    }
}

fun Activity.openAppSettings() {
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).also(::startActivity)
}
