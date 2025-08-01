//modelloOmdb
package com.example.openvideodatabase

data class OmdbSearchResponse(
    val Search: List<OmdbItem>?,
    val totalResults: String?,
    val Response: String,
    val Error: String? = null
)

data class OmdbItem(
    val Title: String,
    val Year: String,
    val imdbID: String,
    val Type: String,
    val Poster: String
)
