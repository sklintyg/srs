package se.inera.intyg.srs.vo

import org.apache.logging.log4j.LogManager
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Diagnos
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Diagnosprediktion
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Prediktion
import se.inera.intyg.srs.vo.Extent.EN_FJARDEDEL
import se.inera.intyg.srs.vo.Extent.HALFTEN
import se.inera.intyg.srs.vo.Extent.HELT_NEDSATT
import se.inera.intyg.srs.vo.Extent.TRE_FJARDEDEL
import java.lang.Math.exp
import java.util.*


const val INTERCEPT = -1.66

class PrediktionInformationModule : InformationModule<Prediktion> {

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
            diagnosPrediktion.sannolikhetLangvarig = createProbability(person, diagnose)
            diagnosPrediktion.diagnos = diagnos

            prediction.diagnosprediktion.add(diagnosPrediktion)
        }
        return prediction
    }

    private fun createProbability(person: Person, diagnose: Diagnose): Double {
        val term = INTERCEPT + ageTerm(person) + sexTerm(person) + extentTerm(person) + diagnosisTerm(diagnose)
        return exp(term) / (1 + exp(term))
    }

    private fun ageTerm(person: Person) =
            when (person.age) {
                in 0..24 -> 0.0
                in 25..34 -> 0.34
                in 35..44 -> 0.54
                in 45..54 -> 0.61
                in 55..64 -> 0.63
                else -> throw IllegalArgumentException("Illegal age: ${person.age}")
            }

    private fun sexTerm(person: Person) =
            when (person.sex) {
                Sex.WOMAN -> 0.58
                Sex.MAN -> 0.41
            }

    private fun extentTerm(person: Person) =
            when (person.extent) {
                null -> 0.0
                HELT_NEDSATT -> 0.49
                EN_FJARDEDEL, HALFTEN, TRE_FJARDEDEL -> 0.0
            }

    private fun diagnosisTerm(diagnose: Diagnose) =
            when {
                diagnose.code.startsWith("Z") -> 0.76
                diagnose.code.startsWith("M") -> 0.34
                else -> 0.0
            }

}
