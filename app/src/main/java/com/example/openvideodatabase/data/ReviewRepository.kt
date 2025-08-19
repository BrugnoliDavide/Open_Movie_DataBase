package com.example.openvideodatabase.data

import com.example.openvideodatabase.data.local.Review
import com.example.openvideodatabase.data.local.ReviewDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ReviewRepository @Inject constructor(
    private val dao: ReviewDao
) {
    //fun getAll(): Flow<List<Review>> = dao.getAll()
    //fun getById(id: Long): Flow<Review?> = dao.getById(id)

    suspend fun insert(review: Review): Long = dao.insert(review)

    suspend fun existsByTitle(title: String): Boolean {
        return dao.countByTitle(title) > 0
    }

    suspend fun update(review: Review) = dao.update(review)
    suspend fun delete(review: Review) = dao.delete(review)


    suspend fun getAllReviews(): List<Review> {
        return dao.getAllReviews()
    }
        suspend fun getReviewById(id: Long): Review? {
            return dao.getReviewById(id)

    }

    fun getReviewsByTitleFlow(title: String): Flow<List<Review>> {
        return dao.getReviewsByTitleFlow(title)}


}
