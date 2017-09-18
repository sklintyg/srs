package se.inera.intyg.srs.vo

import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Service
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Diagnosprediktion
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Diagnosprediktionstatus
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Prediktion
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Risksignal
import se.inera.intyg.srs.persistence.DiagnosisRepository
import se.inera.intyg.srs.persistence.PredictionDiagnosis
import se.inera.intyg.srs.persistence.Probability
import se.inera.intyg.srs.persistence.ProbabilityRepository
import se.inera.intyg.srs.service.monitoring.logPrediction
import se.riv.clinicalprocess.healthcond.certificate.types.v2.Diagnos
import java.math.BigInteger

@Service
class PredictionInformationModule(val rAdapter: PredictionAdapter,
                                  val diagnosisRepo: DiagnosisRepository,
                                  val probabilityRepo: ProbabilityRepository) : InformationModule<Prediktion> {

    private val log = LogManager.getLogger()

    private val categoryDescriptions = mapOf(BigInteger.ONE to "Prediktion saknas.",
            BigInteger.valueOf(2) to "Ingen förhöjd risk detekterad.",
            BigInteger.valueOf(3) to "Förhöjd risk detekterad.",
            BigInteger.valueOf(4) to "Starkt förhöjd risk detekterad.")

    override fun getInfo(persons: List<Person>, extraParams: Map<String, String>): Map<Person, Prediktion> {
        log.info(persons)
        val predictions = HashMap<Person, Prediktion>()
        persons.forEach { person ->
            predictions.put(person, createInfo(person, extraParams))
        }
        return predictions
    }

    private fun createInfo(person: Person, extraParams: Map<String, String>): Prediktion {
        val outgoingPrediction = Prediktion()

        person.diagnoses.forEach { incomingDiagnosis ->
            val diagnosPrediktion = Diagnosprediktion()
            diagnosPrediktion.inkommandediagnos = originalDiagnosis(incomingDiagnosis)

            val calculatedPrediction = rAdapter.getPrediction(person, incomingDiagnosis, extraParams)
            diagnosPrediktion.diagnosprediktionstatus = calculatedPrediction.status

            val diagnosis = diagnosisRepo.findOneByDiagnosisId(calculatedPrediction.diagnosis)

            val riskSignal = Risksignal()
            diagnosPrediktion.risksignal = riskSignal

            if ((calculatedPrediction.status == Diagnosprediktionstatus.OK ||
                    calculatedPrediction.status == Diagnosprediktionstatus.DIAGNOSKOD_PA_HOGRE_NIVA) && diagnosis != null) {
                val outgoingDiagnosis = Diagnos()
                outgoingDiagnosis.codeSystem = incomingDiagnosis.codeSystem
                outgoingDiagnosis.code = calculatedPrediction.diagnosis

                diagnosPrediktion.sannolikhetOvergransvarde = calculatedPrediction.prediction
                diagnosPrediktion.diagnos = outgoingDiagnosis
                riskSignal.riskkategori = calculateRisk(diagnosis, calculatedPrediction.prediction!!)

                persistProbability(diagnosPrediktion, person.certificateId)

            } else {
                riskSignal.riskkategori = BigInteger.ONE
            }
            riskSignal.beskrivning = categoryDescriptions[riskSignal.riskkategori]
            logPrediction(extraParams, diagnosPrediktion.diagnos.code, diagnosis?.prevalence?.toString() ?: "", person.sex.name,
                    person.ageCategory, calculatedPrediction.prediction?.toString() ?: "", riskSignal.riskkategori.intValueExact(),
                    calculatedPrediction.status.toString())

            outgoingPrediction.diagnosprediktion.add(diagnosPrediktion)
        }
        return outgoingPrediction
    }

    private fun persistProbability(diagnosPrediction: Diagnosprediktion, certificateId: String) {
        log.info("Persisting probability for certificateId: $certificateId")
        val probability = Probability(certificateId, diagnosPrediction.sannolikhetOvergransvarde,
                diagnosPrediction.risksignal.riskkategori.intValueExact(),
                diagnosPrediction.inkommandediagnos.code, diagnosPrediction.diagnos.code)
        probabilityRepo.save(probability)
    }

    private fun calculateRisk(diagnosis: PredictionDiagnosis, prediction: Double): BigInteger =
            when {
                prediction <= (2 * diagnosis.prevalence) / (1 * diagnosis.prevalence + 1) -> BigInteger.valueOf(2)
                prediction <= (4 * diagnosis.prevalence) / (3 * diagnosis.prevalence + 1) -> BigInteger.valueOf(3)
                else -> BigInteger.valueOf(4)
            }
}

fun originalDiagnosis(incoming: Diagnosis): Diagnos {
    val original = Diagnos()
    original.codeSystem = incoming.codeSystem
    original.code = incoming.code
    return original
}
