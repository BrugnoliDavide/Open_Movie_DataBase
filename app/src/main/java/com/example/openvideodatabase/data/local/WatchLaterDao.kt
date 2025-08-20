package com.example.openvideodatabase.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WatchLaterDao {

    @Query("SELECT * FROM watch_later ORDER BY id DESC") // usa il nome corretto della tabella
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
