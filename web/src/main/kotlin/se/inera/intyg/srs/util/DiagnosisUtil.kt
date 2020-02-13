package se.inera.intyg.srs.util

import se.inera.intyg.srs.persistence.repository.DiagnosisRepository
import se.inera.intyg.srs.persistence.entity.PredictionDiagnosis
import java.util.*

/**
 * Fetches the Prediction diagnosis entity with the longest matching diagnosisId between length 3 and 5.
 * The incoming diagnosis string will be cleaned from periods and made to uppercase before
 * checking for matches in the database.
 * E.g. if the incoming diagnosisId is 'f43.8a' and we have a PredictionDiagnosis for
 * F43 and F438 but not F438A in the database, this routine will translate 'f43.8a' to 'F438A'
 * and return the PredictionDiagnosis for F438 since it is the longest match on diagnosis id in relation
 * to the incoming diagnosis id.
 * @param diagnosisId The diagnosisId to look for
 * @return The PredictionDiagnosis entity with the longest match on diagnosisId
 */
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

