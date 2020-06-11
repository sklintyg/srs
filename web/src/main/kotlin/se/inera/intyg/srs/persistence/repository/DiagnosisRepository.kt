package se.inera.intyg.srs.persistence.repository

import org.springframework.data.repository.CrudRepository
import se.inera.intyg.srs.persistence.entity.PredictionDiagnosis

interface DiagnosisRepository : CrudRepository<PredictionDiagnosis, Long> {

    fun findOneByDiagnosisIdAndModelVersionAndForSubdiagnosis(diagnosisId: String, modelVersion: String, forSubdiagnosis: Boolean): PredictionDiagnosis?
    fun findByDiagnosisIdAndModelVersion(diagnosisId: String, modelVersion: String): List<PredictionDiagnosis>
    fun findByModelVersion(modelVersion: String): List<PredictionDiagnosis>

}
