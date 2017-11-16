package se.inera.intyg.srs.vo

import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Service
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Atgard
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Atgardsrekommendation
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Atgardsrekommendationer
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Atgardsrekommendationstatus
import se.inera.intyg.srs.persistence.Measure
import se.inera.intyg.srs.persistence.MeasureRepository
import se.riv.clinicalprocess.healthcond.certificate.types.v2.Diagnos
import java.math.BigInteger
import java.util.Locale
import kotlin.collections.HashMap
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.find
import kotlin.collections.forEach

@Service
class MeasureInformationModule(val measureRepo: MeasureRepository) : InformationModule<Atgardsrekommendationer> {

    private val MIN_ID_POSITIONS = 3

    private val log = LogManager.getLogger()

    override fun getInfo(persons: List<Person>, extraParams: Map<String, String>): Map<Person, Atgardsrekommendationer> {
        log.info(persons)
        val measures = HashMap<Person, Atgardsrekommendationer>()
        persons.forEach { person ->
            measures.put(person, createInfo(person))
        }
        return measures
    }

    private fun createInfo(person: Person): Atgardsrekommendationer {
        val recommendations = Atgardsrekommendationer()
        person.diagnoses.forEach { incomingDiagnosis ->
            val recommendation = Atgardsrekommendation()
            recommendation.inkommandediagnos = originalDiagnosis(incomingDiagnosis)

            val (measure, status) = getMeasuresForDiagnosis(incomingDiagnosis.code)

            if (measure != null) {
                val outgoingDiagnosis = Diagnos()
                outgoingDiagnosis.codeSystem = incomingDiagnosis.codeSystem
                outgoingDiagnosis.code = measure.diagnosisId
                outgoingDiagnosis.displayName = measure.diagnosisText
                recommendation.diagnos = outgoingDiagnosis

                measure.priorities.forEach {
                    val atgard = Atgard()
                    atgard.atgardId = BigInteger.ONE
                    atgard.atgardstyp = it.recommendation.type
                    atgard.atgardsforslag = it.recommendation.recommendationText
                    atgard.prioritet = BigInteger.valueOf(it.priority.toLong())
                    // Temp version
                    atgard.version = "1.1"
                    recommendation.atgard.add(atgard)
                }
            }

            recommendation.atgardsrekommendationstatus = status
            recommendations.rekommendation.add(recommendation)
        }
        return recommendations
    }

    private fun getMeasuresForDiagnosis(diagnosisId: String): Pair<Measure?, Atgardsrekommendationstatus> {
        var currentId = cleanDiagnosisCode(diagnosisId)
        val possibleMeasures = measureRepo.findByDiagnosisIdStartingWith(currentId.substring(0, MIN_ID_POSITIONS))
        var status: Atgardsrekommendationstatus = Atgardsrekommendationstatus.OK
        while (currentId.length >= MIN_ID_POSITIONS) {
            val measure = measureForCode(possibleMeasures, currentId)
            if (measure != null) {
                return Pair(measure, status)
            }
            currentId = currentId.substring(0, currentId.length - 1)
            // Once we have shortened the code, we need to indicate that the info is not on the original level
            status = Atgardsrekommendationstatus.DIAGNOSKOD_PA_HOGRE_NIVA
        }
        return Pair(null, Atgardsrekommendationstatus.INFORMATION_SAKNAS)
    }

    private fun measureForCode(measures: List<Measure>, diagnosisId: String): Measure? =
            measures.find { cleanDiagnosisCode(it.diagnosisId) == diagnosisId }

    private fun cleanDiagnosisCode(diagnosisId: String): String = diagnosisId.toUpperCase(Locale.ENGLISH).replace(".", "")

}
