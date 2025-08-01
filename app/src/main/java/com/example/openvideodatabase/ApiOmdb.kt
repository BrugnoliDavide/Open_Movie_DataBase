package com.example.openvideodatabase

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
//import com.example.openvideodatabase.OmdbSearchResponse
//import OmdbSearchResponse

interface ApiOmdb {
    @GET("/")
    fun searchByTitle(
        @Query("s") title: String,
        @Query("apikey") apiKey: String
    ): Call<OmdbSearchResponse>
}
