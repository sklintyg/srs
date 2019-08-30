package se.inera.intyg.srs.persistence.repository

import org.springframework.data.repository.CrudRepository
import se.inera.intyg.srs.persistence.entity.PatientAnswer
import se.inera.intyg.srs.persistence.entity.PredictionResponse
import se.inera.intyg.srs.persistence.entity.Probability

interface PatientAnswerRepository : CrudRepository<PatientAnswer, Long> {
    fun findByProbabilityAndPredictionResponse(probability: Probability, predictionResponse: PredictionResponse): PatientAnswer?
}