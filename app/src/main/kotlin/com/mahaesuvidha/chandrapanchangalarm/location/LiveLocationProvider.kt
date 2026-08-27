package com.mahaesuvidha.chandrapanchangalarm.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*

class LiveLocationProvider(
    private val context: Context
) {

    private val client =
        LocationServices.getFusedLocationProviderClient(context)

    private var callback: LocationCallback? = null

    fun start(
        onLocation: (Location) -> Unit,
        onError: (String) -> Unit
    ) {
        if (
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            onError("स्थानाची परवानगी दिलेली नाही")
            return
        }

        getLastLocation(onLocation, onError)
        startUpdates(onLocation, onError)
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation(
        onLocation: (Location) -> Unit,
        onError: (String) -> Unit
    ) {
        client.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    onLocation(location)
                }
            }
            .addOnFailureListener {
                onError("स्थान मिळू शकले नाही")
            }
    }

    @SuppressLint("MissingPermission")
    private fun startUpdates(
        onLocation: (Location) -> Unit,
        onError: (String) -> Unit
    ) {
        val request =
            LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                60_000L
            )
                .setMinUpdateIntervalMillis(15_000L)
                .setWaitForAccurateLocation(false)
                .build()

        callback =
            object : LocationCallback() {
                override fun onLocationResult(
                    result: LocationResult
                ) {
                    result.lastLocation?.let(onLocation)
                }
            }

        client.requestLocationUpdates(
            request,
            callback!!,
            context.mainLooper
        ).addOnFailureListener {
            onError("Live Location सुरू करता आली नाही")
        }
    }

    fun stop() {
        callback?.let {
            client.removeLocationUpdates(it)
        }
        callback = null
    }
}
