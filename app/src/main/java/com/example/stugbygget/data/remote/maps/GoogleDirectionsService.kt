package com.example.stugbygget.data.remote.maps

import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleDirectionsService {
    @GET("maps/api/directions/json")
    suspend fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("key") apiKey: String
    ): DirectionsResponse
}
