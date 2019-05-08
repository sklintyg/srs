package se.inera.intyg.srs.persistence

import org.springframework.data.repository.CrudRepository

interface PatientAnswerRepository : CrudRepository<PatientAnswer, Long> {
    fun findByProbabilityAndPredictionResponse(probability: Probability, predictionResponse: PredictionResponse): PatientAnswer?
}