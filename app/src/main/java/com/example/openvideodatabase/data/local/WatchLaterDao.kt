package com.example.openvideodatabase.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WatchLaterDao {


    @Query("SELECT * FROM watch_later ORDER BY title ASC")
    suspend fun getAllByTitleAsc(): List<WatchLaterMovie>

    @Query("SELECT * FROM watch_later ORDER BY title DESC")
    suspend fun getAllByTitleDesc(): List<WatchLaterMovie>

    @Query("SELECT * FROM watch_later ORDER BY id ASC")
    suspend fun getAllINV(): List<WatchLaterMovie>

    @Query("SELECT * FROM watch_later ORDER BY id DESC")
    suspend fun getAll(): List<WatchLaterMovie>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(movie: WatchLaterMovie)

    @Delete
    suspend fun delete(movie: WatchLaterMovie)

    @Query("DELETE FROM watch_later")
    suspend fun clearAll()

    @Query("SELECT * FROM watch_later WHERE external_id = :externalId LIMIT 1")
    suspend fun getMovieByExternalId(externalId: String): WatchLaterMovie?
}
