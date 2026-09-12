package com.pointpointer.nav.data.location

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
    val accuracy: Float,
    val speedMetersPerSec: Float,
    val bearing: Float?,
    val hasBearing: Boolean,
    val timeEpochMs: Long
)

class GpsLocationSource(private val context: Context) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    @SuppressLint("MissingPermission")
    val locationFlow: Flow<GpsUpdate?> = callbackFlow {
        val listener = object : LocationListener {
            override fun onLocationChanged(loc: Location) {
                trySend(
                    GpsUpdate(
                        latitude = loc.latitude,
                        longitude = loc.longitude,
                        accuracy = loc.accuracy,
                        speedMetersPerSec = if (loc.hasSpeed()) loc.speed else 0f,
                        bearing = if (loc.hasBearing()) loc.bearing else null,
                        hasBearing = loc.hasBearing(),
                        timeEpochMs = loc.time
                    )
                )
            }
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,
                1.0f,
                listener,
                Looper.getMainLooper()
            )
        } catch (_: Exception) {
            close()
        }

        awaitClose {
            locationManager.removeUpdates(listener)
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
                            accuracy = last.accuracy,
                            speedMetersPerSec = if (last.hasSpeed()) last.speed else 0f,
                            bearing = if (last.hasBearing()) last.bearing else null,
                            hasBearing = last.hasBearing(),
                            timeEpochMs = last.time
                        )
                    )
                }
            } catch (_: Exception) {
                // Помилка або відсутність даних ігнорується
            }
        }
    }
}
