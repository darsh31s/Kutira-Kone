package com.kutirakone.app.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await
import java.util.Locale

object LocationUtils {
    suspend fun getCurrentLocation(context: Context): Pair<Double, Double>? {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null
        }
        
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        return try {
            val location = fusedLocationClient.lastLocation.await()
            if (location != null) Pair(location.latitude, location.longitude) else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAddressFromCoordinates(context: Context, lat: Double, lng: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                addresses[0].locality ?: addresses[0].subLocality ?: "Unknown Area"
            } else {
                "Unknown Area"
            }
        } catch (e: Exception) {
            "Unknown Area"
        }
    }

    suspend fun getCoordinatesFromAddress(context: Context, addressStr: String): List<Pair<String, Pair<Double, Double>>> {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocationName(addressStr, 5)
            addresses?.map { address ->
                val name = address.getAddressLine(0) ?: address.featureName ?: "Unknown Location"
                Pair(name, Pair(address.latitude, address.longitude))
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun calculateDistanceInKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Radius of the earth in km
        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        val a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }

    fun getDeliveryEstimateText(distanceKm: Double): String {
        val totalMinutes = (distanceKm * 2.0).toInt() + 4
        return when {
            totalMinutes < 60 -> "$totalMinutes mins"
            else -> {
                val hours = totalMinutes / 60
                val mins = totalMinutes % 60
                if (mins == 0) "$hours hr" else "${hours}h ${mins}m"
            }
        }
    }

    fun getRealisticDeliveryTimeWindow(distanceKm: Double): String {
        val calendar = java.util.Calendar.getInstance()
        val timeFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
        
        val totalMinutes = (distanceKm * 2.0).toInt() + 4
        val startMinutes = (totalMinutes * 0.90).toInt().coerceAtLeast(3)
        val endMinutes = (totalMinutes * 1.15).toInt().coerceAtLeast(startMinutes + 3)
        
        val calStart = calendar.clone() as java.util.Calendar
        calStart.add(java.util.Calendar.MINUTE, startMinutes)
        val start = timeFormat.format(calStart.time)
        
        val calEnd = calendar.clone() as java.util.Calendar
        calEnd.add(java.util.Calendar.MINUTE, endMinutes)
        val end = timeFormat.format(calEnd.time)
        
        return "$start - $end"
    }
}
