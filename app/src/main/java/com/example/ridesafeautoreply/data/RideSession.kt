package com.example.ridesafeautoreply.data

import kotlinx.serialization.Serializable

@Serializable
data class RideSession(
    val id: String,
    val timestamp: Long,
    val durationSeconds: Long,
    val distanceKm: Double,
    val avgSpeedKmh: Double,
    val endLatitude: Double,
    val endLongitude: Double
)
