package com.sw.gurumemo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import java.io.IOException
import java.util.Locale

class LocationProvider(private val context: Context) {
    private var location: Location? = null
    private var locationManager: LocationManager? = null

    init {
        getLocation()
    }

    private fun getLocation(): Location? {

        try {

            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            var gpsLocation: Location? = null
            var networkLocation: Location? = null

            val isGPSEnabled: Boolean =
                locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled: Boolean =
                locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (!isGPSEnabled && !isNetworkEnabled) {
                return null
            } else {
                val hasFineLocationPermission =
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )

                if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED || hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED)
                    return null

                if (isNetworkEnabled) {
                    networkLocation =
                        locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                }

                if (isGPSEnabled) {
                    gpsLocation =
                        locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                }

                // 2つの位置情報が両方存在する場合、高い精度のものを選択
                if (gpsLocation != null && networkLocation != null) {
                    location = if (gpsLocation.accuracy > networkLocation.accuracy) {
                        gpsLocation
                    } else {
                        networkLocation
                    }
                } else {
                    if (gpsLocation != null) {
                        location = gpsLocation
                    }
                    if (networkLocation != null) {
                        location = networkLocation
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()

        }
        return location
    }

    fun getLocationLatitude(): Double {
        return location?.latitude ?: 0.0
    }

    fun getLocationLongitude(): Double {
        return location?.longitude ?: 0.0
    }

    fun getCurrentAddress(latitude: Double, longitude: Double): Address? {
        val geocoder = Geocoder(context, Locale.getDefault())
        return try {
            geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            null
        }

    }
}