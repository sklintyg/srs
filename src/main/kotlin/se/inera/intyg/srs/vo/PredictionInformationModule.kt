package se.inera.intyg.srs.vo

import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Diagnos
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Diagnosprediktion
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Diagnosprediktionstatus
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Prediktion
import java.util.*

@Service
class PredictionInformationModule : InformationModule<Prediktion> {

    @Autowired
    lateinit var rAdapter: PredictionAdapter

    private val log = LogManager.getLogger()

    override fun getInfo(persons: List<Person>): Map<Person, Prediktion> {
        log.info(persons)
        val predictions = HashMap<Person, Prediktion>()
        persons.forEach { person ->
            predictions.put(person, createInfo(person))
        }
        return predictions
    }

    private fun createInfo(person: Person): Prediktion {
        val outgoingPrediction = Prediktion()

        person.diagnoses.forEach { incomingDiagnosis ->
            val diagnosPrediktion = Diagnosprediktion()
            diagnosPrediktion.inkommandediagnos = originalDiagnosis(incomingDiagnosis)

            val calculatedPrediction = rAdapter.getPrediction(person, incomingDiagnosis)
            diagnosPrediktion.diagnosprediktionstatus = calculatedPrediction.status

            if (calculatedPrediction.status == Diagnosprediktionstatus.OK ||
                    calculatedPrediction.status == Diagnosprediktionstatus.DIAGNOSKOD_PA_HOGRE_NIVA) {
                val outgoingDiagnosis = Diagnos()
                outgoingDiagnosis.codeSystem = incomingDiagnosis.codeSystem
                outgoingDiagnosis.code = calculatedPrediction.diagnosis

                diagnosPrediktion.sannolikhetLangvarig = calculatedPrediction.prediction
                diagnosPrediktion.diagnos = outgoingDiagnosis
            }

            outgoingPrediction.diagnosprediktion.add(diagnosPrediktion)
        }

        return outgoingPrediction
    }
}

fun originalDiagnosis(incoming: Diagnosis): Diagnos {
    val original = Diagnos()
    original.codeSystem = incoming.codeSystem
    original.code = incoming.code
    return original
}
