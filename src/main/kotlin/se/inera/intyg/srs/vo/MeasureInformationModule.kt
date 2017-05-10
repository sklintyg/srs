package se.inera.intyg.srs.vo

import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Atgard
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Diagnos
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Insatsrekommendation
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Insatsrekommendationer
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Insatsrekommendationstatus
import se.inera.intyg.srs.db.Measure
import se.inera.intyg.srs.db.MeasureRepository
import java.math.BigInteger
import java.util.*

@Service
class MeasureInformationModule : InformationModule<Insatsrekommendationer> {

    private val MIN_ID_POSITIONS = 3

    @Autowired
    lateinit var measureRepo: MeasureRepository

    private val log = LogManager.getLogger()

    override fun getInfo(persons: List<Person>): Map<Person, Insatsrekommendationer> {
        log.info(persons)
        val measures = HashMap<Person, Insatsrekommendationer>()
        persons.forEach { person ->
            measures.put(person, createInfo(person))
        }
        return measures
    }

    private fun createInfo(person: Person): Insatsrekommendationer {
        val recommendations = Insatsrekommendationer()
        person.diagnoses.forEach { diagnose ->
            val recommendation = Insatsrekommendation()
            val (measures, status) = getMeasuresForDiagnose(diagnose.code)

            val diagnos = Diagnos()
            diagnos.codeSystem = diagnose.codeSystem

            if (status == Insatsrekommendationstatus.INFORMATION_SAKNAS) {
                diagnos.code = diagnose.code
            } else {
                measures.forEach {
                    diagnos.code = it.diagnoseId
                    diagnos.displayName = it.diagnoseText
                    val atgard = Atgard()
                    atgard.atgardsforslag = it.measureText
                    atgard.prioritet = BigInteger.valueOf(it.priority.toLong())
                    atgard.version = it.version
                    recommendation.atgard.add(atgard)
                }

            }

            recommendation.diagnos = diagnos
            recommendation.insatsrekommendationstatus = status
            recommendations.rekommendation.add(recommendation)
        }
        return recommendations
    }

    private fun getMeasuresForDiagnose(diagnoseId: String): Pair<List<Measure>, Insatsrekommendationstatus> {
        val possibleMeasures = measureRepo.findByDiagnoseIdStartingWith(diagnoseId.substring(0, MIN_ID_POSITIONS))
        var status: Insatsrekommendationstatus = Insatsrekommendationstatus.OK
        var currentId = cleanDiagnoseCode(diagnoseId)
        while (currentId.length >= MIN_ID_POSITIONS) {
            val measures = measuresForCode(possibleMeasures, currentId)
            if (measures.size > 0) {
                return Pair(measures, status)
            }
            // Make the icd10-code one position shorter, and thus more general.
            currentId = currentId.substring(0, currentId.length - 1)
            // Once we have shortened the code, we need to indicate that the info is not on the original level
            status = Insatsrekommendationstatus.DIAGNOSKOD_PA_HOGRE_NIVA
        }
        return Pair(emptyList(), Insatsrekommendationstatus.INFORMATION_SAKNAS)
    }

    private fun measuresForCode(measures: List<Measure>, diagnoseId: String): List<Measure> =
            measures.filter { cleanDiagnoseCode(it.diagnoseId) == diagnoseId }

    private fun cleanDiagnoseCode(diagnoseId: String): String = diagnoseId.toUpperCase(Locale.ENGLISH).replace(".", "")

}
