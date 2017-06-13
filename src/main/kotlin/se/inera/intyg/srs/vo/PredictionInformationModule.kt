package se.inera.intyg.srs.vo

import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Diagnos
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Diagnosprediktion
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
        val prediction = Prediktion()
        person.diagnoses.forEach { diagnose ->
            val diagnos = Diagnos()
            diagnos.code = diagnose.code
            diagnos.codeSystem = diagnose.codeSystem

            val diagnosPrediktion = Diagnosprediktion()
            diagnosPrediktion.sannolikhetLangvarig = rAdapter.getPrediction(person, diagnose)
            diagnosPrediktion.diagnos = diagnos

            prediction.diagnosprediktion.add(diagnosPrediktion)
        }
        return prediction
    }

}
