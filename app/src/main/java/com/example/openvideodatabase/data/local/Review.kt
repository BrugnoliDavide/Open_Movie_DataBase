package com.example.openvideodatabase.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "external_id")
    val externalId: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "rating")
    val rating: Float,

    @ColumnInfo(name = "first_viewed")
    val firstViewed: Date? = null,

    @ColumnInfo(name = "comment")
    val comment: String? = null
)