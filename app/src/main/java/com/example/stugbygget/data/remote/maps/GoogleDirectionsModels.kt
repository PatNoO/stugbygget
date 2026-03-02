package com.example.stugbygget.data.remote.maps

import com.google.gson.annotations.SerializedName

data class DirectionsResponse(
    val routes: List<DirectionsRoute> = emptyList()
)

data class DirectionsRoute(
    val legs: List<DirectionsLeg> = emptyList()
)

data class DirectionsLeg(
    val distance: DirectionsValue = DirectionsValue(),
    val duration: DirectionsValue = DirectionsValue()
)

data class DirectionsValue(
    @SerializedName("value")
    val value: Int = 0
)
