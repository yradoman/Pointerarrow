package com.pointarrow.nav.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart

data class GpsUpdate(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double?,
    val accuracy: Float,
    val speedMetersPerSec: Float,
    val bearing: Float?,
    val hasBearing: Boolean,
    val timeEpochMs: Long
)

class GpsLocationSource(private val context: Context) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    @SuppressLint("MissingPermission")
    val locationFlow: Flow<GpsUpdate?> = callbackFlow<GpsUpdate?> {
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val update = GpsUpdate(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    altitude = if (location.hasAltitude()) location.altitude else null,
                    accuracy = location.accuracy,
                    speedMetersPerSec = if (location.hasSpeed()) location.speed else 0f,
                    bearing = if (location.hasBearing()) location.bearing else null,
                    hasBearing = location.hasBearing(),
                    timeEpochMs = location.time
                )
                trySend(update)
            }

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {
                trySend(null)
            }
        }

        val minTimeMs = 500L
        val minDistanceM = 0f

        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    minTimeMs,
                    minDistanceM,
                    listener,
                    Looper.getMainLooper()
                )
            }
        } catch (_: SecurityException) {}

        awaitClose {
            try {
                locationManager.removeUpdates(listener)
            } catch (_: Exception) {}
        }
    }.onStart {
        try {
            val last = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
            if (last != null) {
                emit(
                    GpsUpdate(
                        latitude = last.latitude,
                        longitude = last.longitude,
                        altitude = if (last.hasAltitude()) last.altitude else null,
                        accuracy = last.accuracy,
                        speedMetersPerSec = if (last.hasSpeed()) last.speed else 0f,
                        bearing = if (last.hasBearing()) last.bearing else null,
                        hasBearing = last.hasBearing(),
                        timeEpochMs = last.time
                    )
                )
            } else {
                emit(null)
            }
        } catch (_: Exception) {
            emit(null)
        }
    }
}
