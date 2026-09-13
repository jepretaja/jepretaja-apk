package com.jepretaja.app.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.jepretaja.app.data.repository.LiveLocationRepository
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
fun LiveLocationTracker(
    bookingId: String,
    userId: String,
    role: String,
    enabled: Boolean,
) {
    val context = LocalContext.current
    val fused = remember { LocationServices.getFusedLocationProviderClient(context) }
    val repository = remember { LiveLocationRepository(com.google.firebase.firestore.FirebaseFirestore.getInstance()) }
    val scope = rememberCoroutineScope()
    var permissionGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }
    val requestPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { permissionGranted = it }

    LaunchedEffect(enabled, permissionGranted) {
        if (enabled && !permissionGranted) requestPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    DisposableEffect(enabled, permissionGranted, bookingId, userId) {
        if (!enabled || !permissionGranted) return@DisposableEffect onDispose { }
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 15_000L)
            .setMinUpdateIntervalMillis(8_000L)
            .build()
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location: Location ->
                    scope.launch {
                        runCatching { repository.publish(bookingId, userId, role, LatLng(location.latitude, location.longitude)) }
                    }
                }
            }
        }
        fused.requestLocationUpdates(request, callback, Looper.getMainLooper())
        onDispose { fused.removeLocationUpdates(callback) }
    }
}