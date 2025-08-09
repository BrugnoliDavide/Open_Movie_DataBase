package com.example.openvideodatabase.data

import com.example.openvideodatabase.data.local.Review
import com.example.openvideodatabase.data.local.ReviewDao
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ReviewRepository @Inject constructor(
    private val dao: ReviewDao
) {
    fun getAll(): Flow<List<Review>> = dao.getAll()
    fun getById(id: Long): Flow<Review?> = dao.getById(id)

    suspend fun insert(review: Review): Long = dao.insert(review)
    suspend fun update(review: Review) = dao.update(review)
    suspend fun delete(review: Review) = dao.delete(review)

    // utility per registrare la prima visualizzazione
    suspend fun markFirstViewedIfNull(id: Long, time: Date) = dao.markFirstViewedIfNull(id, time)
}
