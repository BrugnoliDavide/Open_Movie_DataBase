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

    // Imposta first_viewed soltanto se è null (prima visualizzazione)
    @Query("UPDATE reviews SET first_viewed = :firstViewed WHERE id = :id AND first_viewed IS NULL")
    suspend fun markFirstViewedIfNull(id: Long, firstViewed: Date)
}
