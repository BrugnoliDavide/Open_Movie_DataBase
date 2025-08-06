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

data class OmdbMovieDetails(
    val Title: String?,
    val Year: String?,
    val Rated: String?,
    val Released: String?,
    val Runtime: String?,
    val Genre: String?,
    val Director: String?,
    val Writer: String?,
    val Actors: String?,
    val Plot: String?,
    val Language: String?,
    val Country: String?,
    val Awards: String?,
    val Poster: String?,
    val Ratings: List<Rating>?,
    val Metascore: String?,
    val imdbRating: String?,
    val imdbVotes: String?,
    val imdbID: String?,
    val Type: String?,
    val DVD: String?,
    val BoxOffice: String?,
    val Production: String?,
    val Website: String?,
    val Response: String,
    val Error: String? = null
)

data class Rating(
    val Source: String,
    val Value: String
)