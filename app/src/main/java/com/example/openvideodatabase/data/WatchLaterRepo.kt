package com.example.openvideodatabase.data

import com.example.openvideodatabase.data.local.WatchLaterDao
import com.example.openvideodatabase.data.local.WatchLaterMovie
import com.example.openvideodatabase.data.local.Review

class WatchLaterRepo (private val dao: WatchLaterDao) {
    suspend fun getAllMovies() = dao.getAll()
    suspend fun addMovie(movie: WatchLaterMovie) = dao.insert(movie)
    suspend fun removeMovie(movie: WatchLaterMovie) = dao.delete(movie)

    suspend fun moveToFavorites(movie: WatchLaterMovie, reviewRepo: ReviewRepository): Boolean {
        val exists = reviewRepo.existsByExternalId(movie.externalId)
        if (!exists) {
            reviewRepo.insert(
                Review(
                    title = movie.title,
                    externalId = movie.externalId,
                    rating = if (movie.rating > 0f) movie.rating else 0f
                )
            )
        }
        removeMovie(movie)
        return !exists
    }
}