package se.inera.intyg.srs.vo

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v2.Diagnosprediktion
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v2.Diagnosprediktionstatus
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v2.FragaSvar
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v2.Prediktionsfaktorer
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v2.Risksignal
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.EgenBedomningRiskType
import se.inera.intyg.srs.persistence.entity.Consent
import se.inera.intyg.srs.persistence.entity.PatientAnswer
import se.inera.intyg.srs.persistence.entity.PredictionDiagnosis
import se.inera.intyg.srs.persistence.entity.Probability
import se.inera.intyg.srs.persistence.repository.DiagnosisRepository
import se.inera.intyg.srs.persistence.repository.PatientAnswerRepository
import se.inera.intyg.srs.persistence.repository.ProbabilityRepository
import se.inera.intyg.srs.persistence.repository.ResponseRepository
import se.inera.intyg.srs.service.LOCATION_KEY
import se.inera.intyg.srs.service.QUESTIONS_AND_ANSWERS_KEY
import se.inera.intyg.srs.service.REGION_KEY
import se.inera.intyg.srs.service.ZIP_CODE_KEY
import se.inera.intyg.srs.service.monitoring.logPrediction
import se.inera.intyg.srs.util.PredictionInformationUtil
import se.inera.intyg.srs.util.getModelForDiagnosis
import se.riv.clinicalprocess.healthcond.certificate.types.v2.Diagnos
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@Service
class PredictionInformationModule(val rAdapter: PredictionAdapter,
                                  val diagnosisRepo: DiagnosisRepository,
                                  val probabilityRepo: ProbabilityRepository,
                                  val patientAnswerRepo: PatientAnswerRepository,
                                  val consentModule: ConsentModule,
                                  val responseRepo: ResponseRepository) : InformationModule<Diagnosprediktion> {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun getInfoForDiagnosis(diagnosisId: String): Diagnosprediktion =
            throw NotImplementedError("Predictions can not be made with only diagnosis.")

    override fun getInfo(persons: List<Person>, extraParams: Map<String, Map<String, String>>, careUnitHsaId: String, calculateIndividual: Boolean): Map<Person, List<Diagnosprediktion>> {
        log.trace("Persons: $persons")
        val predictions = HashMap<Person, List<Diagnosprediktion>>()
        persons.forEach { person ->
            predictions.put(person, createInfo(person, extraParams, careUnitHsaId, calculateIndividual))
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

            log.debug("Fetching model for incoming diagnosis ${incomingDiagnosis.code}")
            val diagnosis = diagnosisRepo.getModelForDiagnosis(incomingDiagnosis.code)
            log.debug("Got diagnosis $diagnosis")

            if (diagnosis != null) {
                diagnosPrediktion.diagnos = buildDiagnos(diagnosis.diagnosisId)
                diagnosPrediktion.prevalens = diagnosis.prevalence
            }

            val consent = if (consentModule.consentNeeded()) consentModule.getConsent(person.personId, careUnitHsaId) else null
            log.debug("certificateId ${person.certificateId} diagnosis ${diagnosis?.diagnosisId}")
            diagnosPrediktion.risksignal = Risksignal()
            if (!predictIndividualRisk && !person.certificateId.isBlank() && diagnosis != null
                    && (!consentModule.consentNeeded() || consent != null)) {
                log.trace("Do not predict individual risk, looking for historic entries on the certificate")
                fillWithHistoricPrediction(diagnosPrediktion, person, diagnosis)
            } else if (diagnosis != null && (!consentModule.consentNeeded() || consent != null) &&
                    predictIndividualRisk && isCorrectPredictionParamsAgainstDiagnosis(diagnosis, extraParams)) {
                log.trace("Predict individual risk, we got a diagnosis and got correct prediction params")
                fillWithCalculatedPrediction(diagnosPrediktion, person, incomingDiagnosis, extraParams)
            } else {
                log.trace("No consent was given, no prediction was requested or incorrect combination of parameters, " +
                        "responding with prediction NOT_OK")
                diagnosPrediktion.diagnosprediktionstatus = Diagnosprediktionstatus.NOT_OK
                diagnosPrediktion.risksignal.riskkategori = 0
            }

            diagnosPrediktion.risksignal.beskrivning =
                    PredictionInformationUtil.categoryDescriptions[diagnosPrediktion.risksignal.riskkategori]
            if (predictIndividualRisk) {
                logPrediction(extraParams, diagnosPrediktion.diagnos?.code ?: "",
                        diagnosis?.prevalence?.toString() ?: "", person.sex.name, person.ageCategory,
                        diagnosPrediktion?.sannolikhetOvergransvarde?.toString() ?: "", diagnosPrediktion.risksignal.riskkategori,
                        diagnosPrediktion?.diagnosprediktionstatus?.toString() ?: "", person.certificateId, careUnitHsaId)
            }

            outgoingPrediction.add(diagnosPrediktion)
        }
        return outgoingPrediction
    }

    /**
     * Calculates a new prediction given a set of input parameters
     */
    private fun fillWithCalculatedPrediction(diagnosPrediktion: Diagnosprediktion, person: Person,
                                             incomingDiagnosis: Diagnosis, extraParams: Map<String, Map<String, String>>) {
        var calculatedPrediction: Prediction? = rAdapter.getPrediction(person, incomingDiagnosis, extraParams)
        diagnosPrediktion.diagnosprediktionstatus = calculatedPrediction?.status
        diagnosPrediktion.berakningstidpunkt = calculatedPrediction?.timestamp

        if (calculatedPrediction?.status == Diagnosprediktionstatus.OK ||
                calculatedPrediction?.status == Diagnosprediktionstatus.DIAGNOSKOD_PA_HOGRE_NIVA) {
            log.trace("Have diagnosis and a calculated prediction")
            diagnosPrediktion.sannolikhetOvergransvarde = calculatedPrediction.prediction
            diagnosPrediktion.diagnos = buildDiagnos(calculatedPrediction.diagnosis)
            diagnosPrediktion.risksignal.riskkategori = calculateRisk(calculatedPrediction.prediction!!)
            persistProbability(diagnosPrediktion, person.certificateId, extraParams)
        } else {
            diagnosPrediktion.risksignal.riskkategori = 0
        }
    }

    /**
     * Looks fo a historic prediction and fills the result object diagnosPrediktion
     * @param diagnosPrediktion The result object to fill with historic prediction data
     * @param person Person indata
     * @param diagnosis Diagnosis data entity
     */
    private fun fillWithHistoricPrediction(diagnosPrediktion: Diagnosprediktion, person: Person, diagnosis: PredictionDiagnosis) {
        // Check if we have a historic prediction
        val historicProbabilities = probabilityRepo.findByCertificateIdAndDiagnosisOrderByTimestampDesc(
                person.certificateId, diagnosis.diagnosisId)
        if (historicProbabilities.isNotEmpty()) {
            val historicProbability = historicProbabilities[0]
            log.trace("Found historic entry $historicProbability")
            diagnosPrediktion.sannolikhetOvergransvarde = historicProbability.probability
            diagnosPrediktion.diagnos = buildDiagnos(historicProbability.diagnosis, historicProbability.diagnosisCodeSystem)
            diagnosPrediktion.diagnosprediktionstatus = Diagnosprediktionstatus.valueOf(historicProbability.predictionStatus)
            diagnosPrediktion.inkommandediagnos.codeSystem = historicProbability.incomingDiagnosisCodeSystem
            diagnosPrediktion.inkommandediagnos.code = historicProbability.incomingDiagnosis
            diagnosPrediktion.risksignal.riskkategori = calculateRisk(historicProbability.probability)

            if (historicProbability.ownOpinion != null) {
                diagnosPrediktion.lakarbedomningRisk = EgenBedomningRiskType.fromValue(historicProbability.ownOpinion.opinion)
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
                                frageidSrs = pa.predictionResponse.question?.predictionId
                                svarsidSrs = pa.predictionResponse.predictionId
                            })
                        }
            }
            diagnosPrediktion.berakningstidpunkt = historicProbability.timestamp
        } else {
            // We shouldn't do a prediction and found no historic so we're setting NOT_OK on the returned (not existing) prediction
            diagnosPrediktion.diagnosprediktionstatus = Diagnosprediktionstatus.NOT_OK
            diagnosPrediktion.risksignal.riskkategori = 0
        }
    }

    private fun buildDiagnos(code: String, codeSystem: String = "1.2.752.116.1.1.1.1.3"): Diagnos {
        val diagnos = Diagnos()
        diagnos.codeSystem = codeSystem
        diagnos.code = code
        return diagnos
    }

    private fun isCorrectPredictionParamsAgainstDiagnosis(diagnosis: PredictionDiagnosis, extraParams: Map<String, Map<String,
            String>>): Boolean {
        val included = HashMap<String, String>()
        extraParams[QUESTIONS_AND_ANSWERS_KEY]?.map { included.put(it.key, it.value) }
        log.debug("Checking if correct prediction params, got params: $extraParams")

        val required = HashMap<String, List<String>>()

        diagnosis.questions.forEach {
            required.put(it.question.predictionId, it.question.answers.map { it.predictionId })
        }

        val (isOk, errorList) = isCorrectQuestionsAndAnswers(included, required)
        if (!isOk) {
            log.error("Missing mandatory prediction parameters for ${diagnosis.diagnosisId}: $errorList")
        }
        return isOk
    }

    private fun isCorrectQuestionsAndAnswers(included: HashMap<String, String>, required: HashMap<String, List<String>>):
            Pair<Boolean, List<String>> {
        val errorList: MutableList<String> = ArrayList()
        log.debug("Checking if correct questions and answers included: $included, required: $required")

        if (!included.keys.containsAll(required.keys)) {
            required.keys.filter { !included.keys.contains(it) }.toCollection(errorList)
        }

        included.forEach {
            if (required[it.key] != null && !required[it.key]!!.contains(it.value)) {
                errorList.add("Incorrect answer: ${it.value} for question: ${it.key}")
            }
        }

        return if (errorList.isEmpty()) {
            Pair(true, errorList)
        } else {
            log.debug("Found errors $errorList")
            Pair(false, errorList)
        }

    }

    private fun persistProbability(diagnosPrediction: Diagnosprediktion, certificateId: String, extraParams: Map<String, Map<String, String>>) {
        log.debug("Persisting probability for certificateId: $certificateId")
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
        log.trace("extraParams: $extraParams")
        extraParams[QUESTIONS_AND_ANSWERS_KEY]?.forEach { q, r ->
            log.trace("question: $q, response: $r")
            val predictionResponse = responseRepo.findPredictionResponseByQuestionAndResponse(q, r)
            log.debug("Found predictionResponse $predictionResponse")
            if (predictionResponse != null) {
                var patientAnswer = patientAnswerRepo.findByProbabilityAndPredictionResponse(probability, predictionResponse)
                if (patientAnswer == null) {
                    log.debug("Creating PatientAnswer probability.id: ${probability.id}, " +
                            "predictionResponse(question=response): ${predictionResponse.question?.predictionId}=${predictionResponse.predictionId} ")
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
