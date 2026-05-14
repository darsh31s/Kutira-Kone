package com.kutirakone.app.utils

import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.core.GeoHashQuery
import kotlin.math.*
import kotlin.random.Random

object GeoHashUtils {

    fun encodeGeoHash(lat: Double, lng: Double, precision: Int = Constants.GEOHASH_PRECISION): String {
        return GeoFireUtils.getGeoHashForLocation(GeoLocation(lat, lng))
    }

    // Since we need to return ranges for Firestore query
    // Instead of using custom GeoHashRange we can use GeoFireUtils which provides bounds
    fun getGeoHashBounds(lat: Double, lng: Double, radiusKm: Double): List<com.firebase.geofire.GeoQueryBounds> {
        val radiusInMeters = radiusKm * 1000.0
        val center = GeoLocation(lat, lng)
        val bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInMeters)
        // convert to simple ranges conceptually
        return bounds
    }

    fun distanceBetween(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val startPoint = android.location.Location("start").apply {
            latitude = lat1
            longitude = lng1
        }
        val endPoint = android.location.Location("end").apply {
            latitude = lat2
            longitude = lng2
        }
        return startPoint.distanceTo(endPoint) / 1000.0 // returns km
    }

    fun fuzzLocation(lat: Double, lng: Double, radiusMetres: Double = Constants.LOCATION_FUZZ_METRES): Pair<Double, Double> {
        val radiusInDegrees = radiusMetres / 111000f
        val u = Random.nextDouble()
        val v = Random.nextDouble()
        val w = radiusInDegrees * sqrt(u)
        val t = 2 * Math.PI * v
        val x = w * cos(t)
        val y = w * sin(t)

        val newX = x / cos(Math.toRadians(lat))
        val fuzzedLng = newX + lng
        val fuzzedLat = y + lat

        return Pair(fuzzedLat, fuzzedLng)
    }
}
