package se.inera.intyg.srs.vo

import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Service
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Diagnosprediktion
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Diagnosprediktionstatus
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Prediktion
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Risksignal

import se.inera.intyg.srs.service.monitoring.logPrediction
import se.inera.intyg.srs.persistence.DiagnosisRepository
import se.inera.intyg.srs.persistence.PredictionDiagnosis
import se.inera.intyg.srs.persistence.Probability
import se.inera.intyg.srs.persistence.ProbabilityRepository
import se.inera.intyg.srs.util.PredictionInformationUtil
import se.inera.intyg.srs.util.getModelForDiagnosis

import se.riv.clinicalprocess.healthcond.certificate.types.v2.Diagnos
import java.math.BigInteger
import java.time.LocalDateTime

@Service
class PredictionInformationModule(val rAdapter: PredictionAdapter,
                                  val diagnosisRepo: DiagnosisRepository,
                                  val probabilityRepo: ProbabilityRepository) : InformationModule<Prediktion> {

    private val log = LogManager.getLogger()

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

            var calculatedPrediction: Prediction? = null
            val diagnosis = diagnosisRepo.getModelForDiagnosis(incomingDiagnosis.code)

            if (diagnosis != null && isCorrectPredictionParamsAgainstDiagnosis(diagnosis, extraParams)) {
                calculatedPrediction = rAdapter.getPrediction(person, incomingDiagnosis, extraParams)
                diagnosPrediktion.diagnosprediktionstatus = calculatedPrediction.status
            } else {
                diagnosPrediktion.diagnosprediktionstatus = Diagnosprediktionstatus.NOT_OK
            }

            val riskSignal = Risksignal()
            diagnosPrediktion.risksignal = riskSignal
            if (diagnosis != null && calculatedPrediction != null &&
                    (calculatedPrediction.status == Diagnosprediktionstatus.OK ||
                    calculatedPrediction.status == Diagnosprediktionstatus.DIAGNOSKOD_PA_HOGRE_NIVA)) {
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

            riskSignal.beskrivning = PredictionInformationUtil.categoryDescriptions[riskSignal.riskkategori]

            logPrediction(extraParams, diagnosPrediktion.diagnos?.code ?: "", diagnosis?.prevalence?.toString() ?: "", person.sex.name,
                    person.ageCategory, calculatedPrediction?.prediction?.toString() ?: "", riskSignal.riskkategori.intValueExact(),
                    calculatedPrediction?.status?.toString() ?: "")

            outgoingPrediction.diagnosprediktion.add(diagnosPrediktion)
        }
        return outgoingPrediction
    }

    private fun isCorrectPredictionParamsAgainstDiagnosis(diagnosis: PredictionDiagnosis, extraParams: Map<String,
            String>): Boolean {
        val inc = HashMap<String, String>()
        extraParams.filter { it.key != "Region" }.map { inc.put(it.key, it.value) }

        val req = HashMap<String, List<String>>()

        diagnosis.questions.forEach {
            req.put(it.question.predictionId, it.question.answers.map { it.predictionId })
        }
        val (isOk, errorList) = isCorrectQuestionsAndAnswers(inc, req)
        if (!isOk) {
            log.error("Missing mandatory prediction parameters for ${diagnosis.diagnosisId}: $errorList")
        }
        return isOk
    }

    private fun isCorrectQuestionsAndAnswers(inc: HashMap<String, String>, req: HashMap<String, List<String>>) :
            Pair<Boolean, List<String>> {
        val errorList: MutableList<String> = ArrayList()

        if (!inc.keys.containsAll(req.keys)) {
            req.keys.filter { !inc.keys.contains(it) }.toCollection(errorList)
        }

        inc.forEach {
            if (req[it.key] != null && !req[it.key]!!.contains(it.value)) {
                errorList.add("Incorrect answer: ${it.value} for question: ${it.key}")
            }
        }

        return if (errorList.isEmpty()) {
            Pair(true, errorList)
        } else {
            Pair(false, errorList)
        }

    }

    private fun persistProbability(diagnosPrediction: Diagnosprediktion, certificateId: String) {
        log.info("Persisting probability for certificateId: $certificateId")
        val probability = Probability(certificateId, diagnosPrediction.sannolikhetOvergransvarde,
                diagnosPrediction.risksignal.riskkategori.intValueExact(), diagnosPrediction.inkommandediagnos.code,
                diagnosPrediction.diagnos.code, LocalDateTime.now())
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
