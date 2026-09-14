package com.pointarrow.nav.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
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
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    // Тригер для миттєвого перезапуску слухача при наданні дозволів користувачем
    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1).apply {
        tryEmit(Unit)
    }

    fun refresh() {
        refreshTrigger.tryEmit(Unit)
    }

    @SuppressLint("MissingPermission")
    val locationFlow: Flow<GpsUpdate?> = refreshTrigger.flatMapLatest {
        createLocationFlow()
    }

    @SuppressLint("MissingPermission")
    private fun createLocationFlow(): Flow<GpsUpdate?> = kotlinx.coroutines.flow.callbackFlow {
        val lm = locationManager
        if (lm == null) {
            trySend(null)
            awaitClose {}
            return@callbackFlow
        }

        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            trySend(null)
            awaitClose {}
            return@callbackFlow
        }

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
                // При вимкненні провайдера не вбиваємо сесію негайно, якщо інший провайдер активний
            }
        }

        val minTimeMs = 500L
        val minDistanceM = 0f

        try {
            // На Android 12 (API 31+): GPS_PROVIDER вимагає виключно FINE_LOCATION
            if (hasFine && lm.allProviders.contains(LocationManager.GPS_PROVIDER) && lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                lm.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    minTimeMs,
                    minDistanceM,
                    listener,
                    Looper.getMainLooper()
                )
            }

            // Додатково підключаємо NETWORK_PROVIDER (працює і з FINE, і з COARSE, дає швидкий initial fix)
            if (lm.allProviders.contains(LocationManager.NETWORK_PROVIDER) && lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                lm.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    minTimeMs,
                    minDistanceM,
                    listener,
                    Looper.getMainLooper()
                )
            }
        } catch (_: Exception) {
            // Захист від будь-яких SecurityException / IllegalArgumentException на специфічних прошивках
        }

        awaitClose {
            try {
                lm.removeUpdates(listener)
            } catch (_: Exception) {}
        }
    }.onStart {
        val lm = locationManager
        if (lm != null) {
            try {
                val hasFine = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                val lastGps = if (hasFine) lm.getLastKnownLocation(LocationManager.GPS_PROVIDER) else null
                val lastNetwork = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                val lastPassive = if (hasFine) lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER) else null

                // Обираємо найсвіжішу відому локацію
                val bestLast = listOfNotNull(lastGps, lastNetwork, lastPassive)
                    .maxByOrNull { it.time }

                if (bestLast != null) {
                    emit(
                        GpsUpdate(
                            latitude = bestLast.latitude,
                            longitude = bestLast.longitude,
                            altitude = if (bestLast.hasAltitude()) bestLast.altitude else null,
                            accuracy = bestLast.accuracy,
                            speedMetersPerSec = if (bestLast.hasSpeed()) bestLast.speed else 0f,
                            bearing = if (bestLast.hasBearing()) bestLast.bearing else null,
                            hasBearing = bestLast.hasBearing(),
                            timeEpochMs = bestLast.time
                        )
                    )
                } else {
                    emit(null)
                }
            } catch (_: Exception) {
                emit(null)
            }
        } else {
            emit(null)
        }
    }
}
