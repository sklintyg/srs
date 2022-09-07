package se.inera.intyg.srs.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import se.inera.intyg.clinicalprocess.healthcond.srs.getconsent.v1.GetConsentRequestType
import se.inera.intyg.clinicalprocess.healthcond.srs.getconsent.v1.GetConsentResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.getconsent.v1.GetConsentResponseType
import se.inera.intyg.srs.persistence.repository.DiagnosisRepository
import se.inera.intyg.srs.persistence.entity.PredictionDiagnosis
import se.inera.intyg.srs.vo.ConsentModule
import java.util.*

@Service
class DiagnosisServiceImpl(val diagnosisRepository: DiagnosisRepository) {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Fetches the Prediction diagnosis entity with the longest matching diagnosisId between length 3 and 5.
     * The incoming diagnosis string will be cleaned from periods and made to uppercase before
     * checking for matches in the database.
     * E.g. if the incoming diagnosisId is 'f43.8a' and we have a PredictionDiagnosis for
     * F43 and F438 but not F438A in the database, this routine will translate 'f43.8a' to 'F438A'
     * and return the PredictionDiagnosis for F438 since it is the longest match on diagnosis id in relation
     * to the incoming diagnosis id.
     * @param forPrevalence true if we need a model that has a prevalence value
     * @param diagnosisId The diagnosisId to look for
     * @return The PredictionDiagnosis entity with the longest match on diagnosisId
     */
    fun getModelForDiagnosis(diagnosisId: String, modelVersion: String, forPrevalence: Boolean=false): PredictionDiagnosis? {
        val log = LoggerFactory.getLogger(javaClass)
        val MAX_ID_POSITIONS = 5
        val MIN_ID_POSITIONS = 3
        var currentId = this.cleanDiagnosisCode(diagnosisId)
        if (forPrevalence) {
            // TODO: check the validity of the statement below before updating from prediction model version 3.0
            // We know that only 3-character diagnosis codes have prevalence at this point
            currentId = currentId.substring(0,3);
        }

        log.debug("getModelForDiagnosis will look for model for $diagnosisId")
        if (currentId.length > MAX_ID_POSITIONS) {
            return null
        }

        while (currentId.length > MIN_ID_POSITIONS) {
            val diagnosis = diagnosisRepository.findOneByDiagnosisIdAndModelVersionAndForSubdiagnosis(currentId, modelVersion, true)
            if (diagnosis != null) {
                log.debug("getModelForDiagnosis found model for $currentId with subdiagnosis")
                return diagnosis
            }
            // remove one trailing character at a time until we find the model or reach the minimum length
            currentId = currentId.substring(0, currentId.length - 1)
        }
        // We didn't find any model and we are at the minimum length
        val diagnosis = diagnosisRepository.findOneByDiagnosisIdAndModelVersionAndForSubdiagnosis(currentId, modelVersion, true)
        return if (diagnosis != null) {
            diagnosis
        } else {
            // If we didn't find any model with support for subdiagnosis try to find a model without support for subdiagnosis
            diagnosisRepository.findOneByDiagnosisIdAndModelVersionAndForSubdiagnosis(currentId, modelVersion, false)
        }
    }

    private fun cleanDiagnosisCode(diagnosisId: String): String = diagnosisId.toUpperCase(Locale.ENGLISH).replace(".", "")

}

