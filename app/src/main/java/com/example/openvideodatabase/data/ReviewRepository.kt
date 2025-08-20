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


    suspend fun existsByExternalId(externalId: String): Boolean =
        dao.countByExternalId(externalId) > 0

    suspend fun getByExternalId(externalId: String): Review? =
        dao.getByExternalId(externalId)

    suspend fun getAllReviews(): List<Review> {
        return dao.getAllReviews()}
        suspend fun getReviewById(id: Long): Review? {
            return dao.getReviewById(id)}


    /*
    suspend fun getReviewsByFirstViewedAsc(): List<Review> {
        return dao.getAllByFirstViewedAsc()}

    suspend fun getReviewsByFirstViewedDesc(): List<Review> = dao.getAllByFirstViewedDesc()
    suspend fun getReviewsByRatingAsc(): List<Review> = dao.getAllByRatingAsc()
    suspend fun getReviewsByRatingDesc(): List<Review> = dao.getAllByRatingDesc()
    suspend fun getReviewsByTitleAsc(): List<Review> = dao.getAllByTitleAsc()
    suspend fun getReviewsByTitleDesc(): List<Review> = dao.getAllByTitleDesc()
*/



    fun getReviewsByTitleFlow(title: String): Flow<List<Review>> {
        return dao.getReviewsByTitleFlow(title)}


}
