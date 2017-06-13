package se.inera.intyg.srs.vo

import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Atgard
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Diagnos
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Atgardsrekommendation
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Atgardsrekommendationer
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Atgardsrekommendationstatus
import se.inera.intyg.srs.persistence.Measure
import se.inera.intyg.srs.persistence.MeasureRepository
import java.math.BigInteger
import java.util.*

@Service
class MeasureInformationModule(@Autowired val measureRepo: MeasureRepository) : InformationModule<Atgardsrekommendationer> {

    private val MIN_ID_POSITIONS = 3

    private val log = LogManager.getLogger()

    override fun getInfo(persons: List<Person>): Map<Person, Atgardsrekommendationer> {
        log.info(persons)
        val measures = HashMap<Person, Atgardsrekommendationer>()
        persons.forEach { person ->
            measures.put(person, createInfo(person))
        }
        return measures
    }

    private fun createInfo(person: Person): Atgardsrekommendationer {
        val recommendations = Atgardsrekommendationer()
        person.diagnoses.forEach { diagnose ->
            val recommendation = Atgardsrekommendation()
            val (measure, status) = getMeasuresForDiagnose(diagnose.code)

            val diagnos = Diagnos()
            diagnos.codeSystem = diagnose.codeSystem

            if (measure == null) {
                diagnos.code = diagnose.code
            } else {
                diagnos.code = measure.diagnoseId
                diagnos.displayName = measure.diagnoseText

                // TODO: recommendation.version = measure.version
                measure.priorities.forEach {
                    val atgard = Atgard()
                    atgard.atgardsforslag = it.recommendation.recommendationText
                    atgard.prioritet = BigInteger.valueOf(it.priority.toLong())
                    recommendation.atgard.add(atgard)
                }
            }

            recommendation.diagnos = diagnos
            recommendation.atgardsrekommendationstatus = status
            recommendations.rekommendation.add(recommendation)
        }
        return recommendations
    }

    private fun getMeasuresForDiagnose(diagnoseId: String): Pair<Measure?, Atgardsrekommendationstatus> {
        val possibleMeasures = measureRepo.findByDiagnoseIdStartingWith(diagnoseId.substring(0, MIN_ID_POSITIONS))
        var status: Atgardsrekommendationstatus = Atgardsrekommendationstatus.OK
        var currentId = cleanDiagnoseCode(diagnoseId)
        while (currentId.length >= MIN_ID_POSITIONS) {
            val measure = measureForCode(possibleMeasures, currentId)
            if (measure != null) {
                return Pair(measure, status)
            }
            // Make the icd10-code one position shorter, and thus more general.
            currentId = currentId.substring(0, currentId.length - 1)
            // Once we have shortened the code, we need to indicate that the info is not on the original level
            status = Atgardsrekommendationstatus.DIAGNOSKOD_PA_HOGRE_NIVA
        }
        return Pair(null, Atgardsrekommendationstatus.INFORMATION_SAKNAS)
    }

    private fun measureForCode(measures: List<Measure>, diagnoseId: String): Measure? =
            measures.find { cleanDiagnoseCode(it.diagnoseId) == diagnoseId }

    private fun cleanDiagnoseCode(diagnoseId: String): String = diagnoseId.toUpperCase(Locale.ENGLISH).replace(".", "")

}
