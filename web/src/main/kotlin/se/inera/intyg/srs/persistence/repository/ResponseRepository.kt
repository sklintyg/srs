package se.inera.intyg.srs.persistence.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import se.inera.intyg.srs.persistence.entity.PredictionResponse

interface ResponseRepository : CrudRepository<PredictionResponse, Long> {
    @Query(value = "SELECT r FROM PredictionResponse r, PredictionQuestion q " +
            "WHERE q.predictionId = :questionPredictionId " +
            "AND r.predictionId = :responsePredictionId " +
            "AND r.question = q")
    fun findPredictionResponseByQuestionAndResponse (
            @Param("questionPredictionId") questionPredictionId: String,
            @Param("responsePredictionId") responsePredictionId: String
    ): PredictionResponse?

}