package com.example.openvideodatabase.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ReviewDao {

    @Query("SELECT * FROM reviews ORDER BY first_viewed DESC")
    fun getAll(): Flow<List<Review>>

    @Query("SELECT * FROM reviews WHERE id = :id")
    fun getById(id: Long): Flow<Review?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(review: Review): Long

    @Update
    suspend fun update(review: Review)

    @Delete
    suspend fun delete(review: Review)

    @Query("SELECT * FROM REVIEWS")
    suspend fun getAllReviews(): List<Review>

    @Query("SELECT COUNT(*) FROM reviews WHERE title = :title")
    suspend fun countByTitle(title: String): Int

    @Query("SELECT * FROM reviews WHERE title = :title ORDER BY id DESC")
    fun getReviewsByTitleFlow(title: String): Flow<List<Review>>

    @Query("SELECT * FROM reviews WHERE title = :title ORDER BY id DESC LIMIT 1")
    suspend fun getLastReviewByTitle(title: String): Review?

    @Query("SELECT * FROM reviews WHERE id = :id")
    suspend fun getReviewById(id: Long): Review?


    // Imposta first_viewed soltanto se è null (prima visualizzazione)
    @Query("UPDATE reviews SET first_viewed = :firstViewed WHERE id = :id AND first_viewed IS NULL")
    suspend fun markFirstViewedIfNull(id: Long, firstViewed: Date)
}
