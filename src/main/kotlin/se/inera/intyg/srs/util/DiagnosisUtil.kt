package se.inera.intyg.srs.util

import se.inera.intyg.srs.persistence.DiagnosisRepository
import se.inera.intyg.srs.persistence.PredictionDiagnosis
import java.util.Locale

fun DiagnosisRepository.getModelForDiagnosis(diagnosisId: String): PredictionDiagnosis? {
    val MAX_ID_POSITIONS = 5
    val MIN_ID_POSITIONS = 3
    var currentId = cleanDiagnosisCode(diagnosisId)

    if (currentId.length > MAX_ID_POSITIONS) {
        return null
    }

    while (currentId.length >= MIN_ID_POSITIONS) {
        val diagnosis = findOneByDiagnosisId(currentId)
        if (diagnosis != null) {
            return diagnosis
        }
        currentId = currentId.substring(0, currentId.length - 1)
    }
    return null
}

private fun cleanDiagnosisCode(diagnosisId: String): String = diagnosisId.toUpperCase(Locale.ENGLISH).replace(".", "")

