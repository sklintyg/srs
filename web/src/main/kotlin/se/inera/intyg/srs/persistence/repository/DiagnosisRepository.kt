package se.inera.intyg.srs.persistence.repository

import org.springframework.data.repository.CrudRepository
import se.inera.intyg.srs.persistence.entity.PredictionDiagnosis

interface DiagnosisRepository : CrudRepository<PredictionDiagnosis, Long> {

    fun findOneByDiagnosisIdAndModelVersion(diagnosisId: String, modelVersion: String): PredictionDiagnosis?

}
