package se.inera.intyg.srs.vo

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Atgard
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Atgardsrekommendation
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Atgardsrekommendationstatus
import se.inera.intyg.srs.persistence.entity.Measure
import se.inera.intyg.srs.persistence.repository.MeasureRepository
import se.riv.clinicalprocess.healthcond.certificate.types.v2.Diagnos
import java.lang.RuntimeException
import java.math.BigInteger
import java.util.Locale
import kotlin.collections.HashMap

@Service
class MeasureInformationModule(val measureRepo: MeasureRepository) : InformationModule<Atgardsrekommendation> {

    private val MIN_ID_POSITIONS = 3

    private val log = LoggerFactory.getLogger(javaClass)

    override fun getInfoForDiagnosis(diagnosisId: String): Atgardsrekommendation =
            createRecommendation(Diagnosis(diagnosisId))

    override fun getInfo(persons: List<Person>, extraParams: Map<String, Map<String, String>>,
                         careUnitHsaId: String, calculateIndividual: Boolean): Map<Person, List<Atgardsrekommendation>> {
        log.debug("Persons: $persons")
        if (calculateIndividual) {
            throw RuntimeException("calculateIndividual not supported")
        }
        val measures = HashMap<Person, List<Atgardsrekommendation>>()
        persons.forEach { person ->
            measures.put(person, createInfo(person))
        }
        return measures
    }

    private fun createInfo(person: Person): List<Atgardsrekommendation> =
            person.diagnoses.map(this::createRecommendation)

    private fun createRecommendation(incomingDiagnosis: Diagnosis): Atgardsrekommendation {
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
                atgard.atgardsrubrik = it.recommendation.recommendationTitle
                atgard.atgardsforslag = it.recommendation.recommendationText
                atgard.prioritet = BigInteger.valueOf(it.priority.toLong())
                // Temp version
                atgard.version = "1.1"
                recommendation.atgard.add(atgard)
            }
        }

        recommendation.atgardsrekommendationstatus = status
        return recommendation
    }

    private fun getMeasuresForDiagnosis(diagnosisId: String): Pair<Measure?, Atgardsrekommendationstatus> {
        // remove dots end spaces e.g. "f 438.a" -> "f438a"
        var currentId = cleanDiagnosisCode(diagnosisId)

        // search based on the the 3 first characters
        val possibleMeasures = measureRepo.findByDiagnosisIdStartingWith(currentId.substring(0, MIN_ID_POSITIONS))

        log.debug("Found possible measures for $currentId: {}", possibleMeasures)
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
