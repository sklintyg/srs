package se.inera.intyg.srs.persistence

import org.springframework.data.repository.CrudRepository

interface DiagnosisRepository: CrudRepository<PredictionDiagnosis, Long> {

    fun findOneByDiagnosisId(diagnosisId: String): PredictionDiagnosis?

}
