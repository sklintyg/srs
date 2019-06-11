package se.inera.intyg.srs.vo

import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Service
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v2.Diagnosprediktion
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v2.Diagnosprediktionstatus
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v2.FragaSvar
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v2.Prediktionsfaktorer
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v2.Risksignal
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.EgenBedomningRiskType
import se.inera.intyg.srs.persistence.ConsentRepository
import se.inera.intyg.srs.persistence.DiagnosisRepository
import se.inera.intyg.srs.persistence.PatientAnswer
import se.inera.intyg.srs.persistence.PatientAnswerRepository
import se.inera.intyg.srs.persistence.PredictionDiagnosis
import se.inera.intyg.srs.persistence.Probability
import se.inera.intyg.srs.persistence.ProbabilityRepository
import se.inera.intyg.srs.persistence.ResponseRepository
import se.inera.intyg.srs.service.LOCATION_KEY
import se.inera.intyg.srs.service.QUESTIONS_AND_ANSWERS_KEY
import se.inera.intyg.srs.service.REGION_KEY
import se.inera.intyg.srs.service.ZIP_CODE_KEY
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
                                  val consentRepository: ConsentRepository,
                                  val responseRepo: ResponseRepository) : InformationModule<Diagnosprediktion> {

    private val log = LogManager.getLogger()

    override fun getInfoForDiagnosis(diagnosisId: String): Diagnosprediktion =
            throw NotImplementedError("Predictions can not be made with only diagnosis.")

    override fun getInfo(persons: List<Person>, extraParams: Map<String, Map<String, String>>, unitHsaId: String, calculateIndividual: Boolean): Map<Person, List<Diagnosprediktion>> {
        log.info(persons)
        val predictions = HashMap<Person, List<Diagnosprediktion>>()
        persons.forEach { person ->
            predictions.put(person, createInfo(person, extraParams, unitHsaId, calculateIndividual))
        }
        return predictions
    }

    /**
     * Create DiagnosPrediktion from incoming Person objects and extra params
     */
    private fun createInfo(person: Person, extraParams: Map<String, Map<String, String>>, careUnitHsaId: String, predictIndividualRisk: Boolean): List<Diagnosprediktion> {
        log.debug("createInfo(person: $person, extraParams: $extraParams, careUnitHsaId: " +
                "$careUnitHsaId, predictIndividualRisk: $predictIndividualRisk)")
        val outgoingPrediction = mutableListOf<Diagnosprediktion>()

        person.diagnoses.forEach { incomingDiagnosis ->
            log.trace("working with incomingDiagnosis: $incomingDiagnosis")

            val diagnosPrediktion = Diagnosprediktion()
            diagnosPrediktion.inkommandediagnos = originalDiagnosis(incomingDiagnosis)

            var calculatedPrediction: Prediction? = null
            val diagnosis = diagnosisRepo.getModelForDiagnosis(incomingDiagnosis.code)

            if (diagnosis != null) {
                diagnosPrediktion.diagnos = buildDiagnos(diagnosis.diagnosisId)
                diagnosPrediktion.prevalens = diagnosis.prevalence
            }

            val consent = consentRepository.findConsentByPersonnummerAndVardenhetId(person.personId, careUnitHsaId)

            if (!predictIndividualRisk && !person.certificateId.isBlank() && diagnosis != null && consent != null) {
                log.trace("Do not predict individual risk, looking for historic entries on the certificate")
                // Check if we have a historic prediction
                val historicProbability = probabilityRepo.findFirstByCertificateIdAndDiagnosisOrderByTimestampDesc(
                        person.certificateId, diagnosis.diagnosisId)
                if (historicProbability != null) {
                    log.trace("Found historic entry")
                    diagnosPrediktion.sannolikhetOvergransvarde = historicProbability.probability
                    diagnosPrediktion.diagnos = buildDiagnos(historicProbability.diagnosis, historicProbability.diagnosisCodeSystem)
                    diagnosPrediktion.diagnosprediktionstatus = Diagnosprediktionstatus.valueOf(historicProbability.predictionStatus)
                    diagnosPrediktion.inkommandediagnos.codeSystem = historicProbability.incomingDiagnosisCodeSystem
                    diagnosPrediktion.inkommandediagnos.code = historicProbability.incomingDiagnosis

                    val risksignal = Risksignal()
                    risksignal.riskkategori = calculateRisk(historicProbability.probability!!)
                    risksignal.beskrivning = PredictionInformationUtil.categoryDescriptions[risksignal.riskkategori]
                    diagnosPrediktion.risksignal = risksignal

                    if (historicProbability.ownOpinion != null) {
                        diagnosPrediktion.lakarbedomningRisk = EgenBedomningRiskType.fromValue(historicProbability.ownOpinion?.opinion)
                    }
                    if (!historicProbability.patientAnswers.isNullOrEmpty()) {
                        diagnosPrediktion.prediktionsfaktorer = Prediktionsfaktorer()
                        diagnosPrediktion.prediktionsfaktorer.postnummer = historicProbability.zipCode
                        if (!historicProbability.region.isNullOrBlank()) {
                            diagnosPrediktion.prediktionsfaktorer.fragasvar.add(
                                FragaSvar().apply {
                                    frageidSrs = "Region"
                                    svarsidSrs = historicProbability.region
                                }
                            )
                        }
                        diagnosPrediktion.prediktionsfaktorer.postnummer = historicProbability.zipCode
                        historicProbability.patientAnswers
                                .forEach { pa->
                                    diagnosPrediktion.prediktionsfaktorer.fragasvar.add(FragaSvar().apply {
                                        frageidSrs = pa.predictionResponse.question.predictionId
                                        svarsidSrs = pa.predictionResponse.predictionId
                                    })
                                }
                    }
                    diagnosPrediktion.berakningstidpunkt = historicProbability.timestamp

                }
            } else if (diagnosis != null && isCorrectPredictionParamsAgainstDiagnosis(diagnosis, extraParams) && predictIndividualRisk) {
                log.trace("Predict individual risk, we got a diagnosis and got correct prediction params")
                calculatedPrediction = rAdapter.getPrediction(person, incomingDiagnosis, extraParams)
                diagnosPrediktion.diagnosprediktionstatus = calculatedPrediction.status
                diagnosPrediktion.berakningstidpunkt = calculatedPrediction.timestamp
            } else {
                log.trace("Incorrect combination of parameters, responding with NOT_OK")
                diagnosPrediktion.diagnosprediktionstatus = Diagnosprediktionstatus.NOT_OK
            }

            val riskSignal = Risksignal()
            diagnosPrediktion.risksignal = riskSignal
            if (diagnosis != null && calculatedPrediction != null &&
                    (calculatedPrediction.status == Diagnosprediktionstatus.OK ||
                            calculatedPrediction.status == Diagnosprediktionstatus.DIAGNOSKOD_PA_HOGRE_NIVA)) {
                log.trace("Have diagnosis and a calculated prediction")
                diagnosPrediktion.sannolikhetOvergransvarde = calculatedPrediction.prediction
                diagnosPrediktion.diagnos = buildDiagnos(calculatedPrediction.diagnosis)

                riskSignal.riskkategori = calculateRisk(calculatedPrediction.prediction!!)

                persistProbability(diagnosPrediktion, person.certificateId, extraParams)

            } else {
                log.trace("Either no diagnosis or no calculated prediction, using risk category 0")
                riskSignal.riskkategori = 0
            }

            riskSignal.beskrivning = PredictionInformationUtil.categoryDescriptions[riskSignal.riskkategori]

            logPrediction(extraParams, diagnosPrediktion.diagnos?.code ?: "", diagnosis?.prevalence?.toString() ?: "", person.sex.name,
                    person.ageCategory, calculatedPrediction?.prediction?.toString() ?: "", riskSignal.riskkategori,
                    calculatedPrediction?.status?.toString() ?: "", person.certificateId, careUnitHsaId)

            outgoingPrediction.add(diagnosPrediktion)
        }
        return outgoingPrediction
    }

    private fun buildDiagnos(code: String, codeSystem: String = "1.2.752.116.1.1.1.1.3"): Diagnos {
        val diagnos = Diagnos()
        diagnos.codeSystem = codeSystem
        diagnos.code = code
        return diagnos
    }

//    private fun getHistoricPrediction(person: Person): Prediction {
//        val historicProbability = probabilityRepo.findFirstByCertificateIdOrderByTimestampDesc(person.certificateId)
//        val prediction = Prediction(historicProbability.diagnosis, historicProbability.)
//        diagnosPrediktion.diagnosprediktionstatus = historicProbability.
//    }

    private fun isCorrectPredictionParamsAgainstDiagnosis(diagnosis: PredictionDiagnosis, extraParams: Map<String, Map<String,
            String>>): Boolean {
        val inc = HashMap<String, String>()
        extraParams[QUESTIONS_AND_ANSWERS_KEY]?.map { inc.put(it.key, it.value) }
//        extraParams.filter { it.key != "Region" }.map { inc.put(it.key, it.value) }

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

    private fun persistProbability(diagnosPrediction: Diagnosprediktion, certificateId: String, extraParams: Map<String, Map<String, String>>) {
        log.info("Persisting probability for certificateId: $certificateId")
        var probability = Probability(certificateId,
                diagnosPrediction.sannolikhetOvergransvarde,
                diagnosPrediction.risksignal.riskkategori,
                diagnosPrediction.inkommandediagnos.codeSystem,
                diagnosPrediction.inkommandediagnos.code,
                diagnosPrediction.diagnos.codeSystem,
                diagnosPrediction.diagnos.code,
                diagnosPrediction.diagnosprediktionstatus.value(),
                LocalDateTime.now(),
                extraParams[LOCATION_KEY]?.get(REGION_KEY),
                extraParams[LOCATION_KEY]?.get(ZIP_CODE_KEY))
        probability = probabilityRepo.save(probability)
        log.info("extraParams: $extraParams")
        extraParams[QUESTIONS_AND_ANSWERS_KEY]?.forEach { q, r ->
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

    fun calculateRisk(prediction: Double): Int =
        when {
            prediction < 0.39 -> 1
            (prediction >= 0.39 && prediction <= 0.62) -> 2
            prediction > 0.62 -> 3
            else -> 0
        }
}

fun originalDiagnosis(incoming: Diagnosis): Diagnos {
    val original = Diagnos()
    original.codeSystem = incoming.codeSystem
    original.code = incoming.code
    return original
}
