package se.inera.intyg.srs.persistence.repository

import org.springframework.data.repository.CrudRepository
import se.inera.intyg.srs.persistence.entity.PredictionQuestion

interface QuestionRepository : CrudRepository<PredictionQuestion, Long> {

    fun findByPredictionId(predictionId: String): PredictionQuestion?

}
