package se.inera.intyg.srs.vo

import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Diagnos
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Insatsrekommendation
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Insatsrekommendationer
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Insatsrekommendationstatus
import se.inera.intyg.srs.db.Measure
import se.inera.intyg.srs.db.MeasureRepository
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
        val measures = Insatsrekommendationer()
        person.diagnoses.forEach { diagnose ->
            val recommendation = Insatsrekommendation()
            val (measure, status) = diagnoseInfo(diagnose.code)

            val diagnos = Diagnos()
            diagnos.codeSystem = diagnose.codeSystem

            if (measure == null) {
                diagnos.code = diagnose.code
            } else {
                diagnos.code = measure.diagnoseId
                diagnos.displayName = measure.diagnoseText

            }

            recommendation.diagnos = diagnos
            recommendation.insatsrekommendationstatus = status
            measures.rekommendation.add(recommendation)
        }
        return measures
    }

    private fun diagnoseInfo(diagnoseId: String): Pair<Measure?, Insatsrekommendationstatus> {
        val possibleMeasures = measureRepo.findByDiagnoseIdStartingWith(diagnoseId.substring(0, MIN_ID_POSITIONS))
        var status: Insatsrekommendationstatus = Insatsrekommendationstatus.OK
        var currentId = clean(diagnoseId)
        while (currentId.length >= MIN_ID_POSITIONS) {
            val measure = measureExists(possibleMeasures, currentId)
            if (measure != null) {
                return Pair(measure, status)
            }
            // Make the icd10-code one position shorter, and thus more general.
            currentId = currentId.substring(0, currentId.length - 1)
            status = Insatsrekommendationstatus.DIAGNOSKOD_PA_HOGRE_NIVA
        }
        return Pair(null, Insatsrekommendationstatus.INFORMATION_SAKNAS)
    }

    private fun measureExists(measures: List<Measure>, diagnoseId: String): Measure? =
            measures.find { clean(it.diagnoseId) == diagnoseId }

    private fun clean(diagnoseId: String): String = diagnoseId.toUpperCase(Locale.ENGLISH).replace(".", "")
}
