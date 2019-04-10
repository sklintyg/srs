package se.inera.intyg.srs.vo

import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Service
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v2.Diagnosprediktion
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v2.Diagnosprediktionstatus
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v2.Risksignal
import se.inera.intyg.srs.persistence.DiagnosisRepository
import se.inera.intyg.srs.persistence.PatientAnswer
import se.inera.intyg.srs.persistence.PatientAnswerRepository
import se.inera.intyg.srs.persistence.PredictionDiagnosis
import se.inera.intyg.srs.persistence.Probability
import se.inera.intyg.srs.persistence.ProbabilityRepository
import se.inera.intyg.srs.persistence.ResponseRepository
import se.inera.intyg.srs.service.monitoring.logPrediction
import se.inera.intyg.srs.util.PredictionInformationUtil
import se.inera.intyg.srs.util.getModelForDiagnosis
import se.riv.clinicalprocess.healthcond.certificate.types.v2.Diagnos
import java.math.BigInteger
import java.time.LocalDateTime

@Service
class PredictionInformationModule(val rAdapter: PredictionAdapter,
                                  val diagnosisRepo: DiagnosisRepository,
                                  val probabilityRepo: ProbabilityRepository,
                                  val patientAnswerRepo: PatientAnswerRepository,
                                  val responseRepo: ResponseRepository) : InformationModule<Diagnosprediktion> {

    private val log = LogManager.getLogger()

    override fun getInfoForDiagnosis(diagnosisId: String): Diagnosprediktion =
            throw NotImplementedError("Predictions can not be made with only diagnosis.")

    override fun getInfo(persons: List<Person>, extraParams: Map<String, String>, userHsaId: String, calculateIndividual: Boolean): Map<Person, List<Diagnosprediktion>> {
        log.info(persons)
        val predictions = HashMap<Person, List<Diagnosprediktion>>()
        persons.forEach { person ->
            predictions.put(person, createInfo(person, extraParams, userHsaId, calculateIndividual))
        }
        return predictions
    }

    private fun createInfo(person: Person, extraParams: Map<String, String>, userHsaId: String, predictIndividualRisk: Boolean): List<Diagnosprediktion> {
        val outgoingPrediction = mutableListOf<Diagnosprediktion>()

        person.diagnoses.forEach { incomingDiagnosis ->
            val diagnosPrediktion = Diagnosprediktion()
            diagnosPrediktion.inkommandediagnos = originalDiagnosis(incomingDiagnosis)

            var calculatedPrediction: Prediction? = null
            val diagnosis = diagnosisRepo.getModelForDiagnosis(incomingDiagnosis.code)

            if (diagnosis != null) {
                diagnosPrediktion.prevalens = diagnosis.prevalence
            }

            if (diagnosis != null && isCorrectPredictionParamsAgainstDiagnosis(diagnosis, extraParams) && predictIndividualRisk) {
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

                persistProbability(diagnosPrediktion, person.certificateId, extraParams)

            } else {
                riskSignal.riskkategori = BigInteger.ONE
            }

            riskSignal.beskrivning = PredictionInformationUtil.categoryDescriptions[riskSignal.riskkategori]

            logPrediction(extraParams, diagnosPrediktion.diagnos?.code ?: "", diagnosis?.prevalence?.toString() ?: "", person.sex.name,
                    person.ageCategory, calculatedPrediction?.prediction?.toString() ?: "", riskSignal.riskkategori.intValueExact(),
                    calculatedPrediction?.status?.toString() ?: "", person.certificateId, userHsaId)

            outgoingPrediction.add(diagnosPrediktion)
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

    private fun isCorrectQuestionsAndAnswers(inc: HashMap<String, String>, req: HashMap<String, List<String>>):
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

    private fun persistProbability(diagnosPrediction: Diagnosprediktion, certificateId: String, extraParams: Map<String, String>) {
        log.info("Persisting probability for certificateId: $certificateId")
        var probability = Probability(certificateId,
                diagnosPrediction.sannolikhetOvergransvarde,
                diagnosPrediction.risksignal.riskkategori.intValueExact(),
                diagnosPrediction.inkommandediagnos.code,
                diagnosPrediction.diagnos.code,
                LocalDateTime.now())
        probability = probabilityRepo.save(probability)
        log.info("extraParams: $extraParams")
        extraParams.forEach { q, r ->
            log.info("question: $q, response: $r")
            val predictionResponse = responseRepo.findPredictionResponseByQuestionAndResponse(q, r)
            log.info("Found predictionResponse $predictionResponse")
            if (predictionResponse != null) {
                var patientAnswer = patientAnswerRepo.findByProbabilityAndPredictionResponse(probability, predictionResponse)
                if (patientAnswer == null) {
                    log.info("Creating PatientAnswer probability.id: ${probability.id}, predictionResponse(question=response): ${predictionResponse.question.predictionId}=${predictionResponse.predictionId} ")
                    patientAnswer = PatientAnswer()
                }
                patientAnswer.probability = probability
                patientAnswer.predictionResponse = predictionResponse
                patientAnswerRepo.save(patientAnswer)
            }
        }
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
