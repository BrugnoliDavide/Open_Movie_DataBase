package com.example.openvideodatabase

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiOmdb {
    @GET("/")
    fun searchByTitle(
        @Query("s") title: String,
        @Query("apikey") apiKey: String
    ): Call<OmdbSearchResponse>

    @GET("/")
    fun getMovieDetails(
        @Query("i") imdbId: String,
        @Query("apikey") apiKey: String
    ): Call<OmdbMovieDetails>
}