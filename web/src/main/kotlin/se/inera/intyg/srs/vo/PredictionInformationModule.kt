package se.inera.intyg.srs.vo

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v3.Diagnosprediktion
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v3.Diagnosprediktionstatus
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v3.FragaSvar
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v3.Prediktionsfaktorer
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v3.Risksignal
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.EgenBedomningRiskType
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
import se.riv.clinicalprocess.healthcond.certificate.types.v2.IntygId
import java.time.LocalDateTime
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@Service
class PredictionInformationModule(val rAdapter: PredictionAdapter,
                                  val diagnosisRepo: DiagnosisRepository,
                                  val probabilityRepo: ProbabilityRepository,
                                  val patientAnswerRepo: PatientAnswerRepository,
                                  val consentModule: ConsentModule,
                                  val responseRepo: ResponseRepository,
                                  @Value("\${model.currentVersion}") val currentModelVersion: String
) : InformationModule<Diagnosprediktion> {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun getInfoForDiagnosis(diagnosisId: String): Diagnosprediktion =
            throw NotImplementedError("Predictions can not be made with only diagnosis.")

    override fun getInfo(persons: List<Person>, extraParams: Map<String, Map<String, String>>, careUnitHsaId: String,
                         calculateIndividual: Boolean, daysIntoSickLeave:Int): Map<Person, List<Diagnosprediktion>> {
        log.trace("Persons: $persons")
        val predictions = HashMap<Person, List<Diagnosprediktion>>()
        persons.forEach { person ->
            predictions.put(person, createInfo(person, extraParams, careUnitHsaId, calculateIndividual, daysIntoSickLeave))
        }
        return predictions
    }

    /**
     * Create DiagnosPrediktion from incoming Person objects and extra params
     */
    private fun createInfo(person: Person, extraParams: Map<String, Map<String, String>>, careUnitHsaId: String,
                           predictIndividualRisk: Boolean, daysIntoSickLeave:Int): List<Diagnosprediktion> {
        log.debug("createInfo(person: $person, extraParams: $extraParams, careUnitHsaId: " +
                "$careUnitHsaId, predictIndividualRisk: $predictIndividualRisk, daysIntoSickLeave: $daysIntoSickLeave)")
        val outgoingPrediction = mutableListOf<Diagnosprediktion>()

        person.certDiags.forEachIndexed { index, incomingCertDiagnosis ->
            log.trace("working with incomingCertDiagnosis: ${incomingCertDiagnosis.code} certificateId: ${incomingCertDiagnosis.certificateId} index: $index")

            val diagnosPrediktion = Diagnosprediktion()
            diagnosPrediktion.inkommandediagnos = originalDiagnosis(incomingCertDiagnosis)
            diagnosPrediktion.intygId = buildIntygId(careUnitHsaId, incomingCertDiagnosis.certificateId)

            log.debug("Fetching model for incoming diagnosis ${incomingCertDiagnosis.code} modelVersion ${currentModelVersion}")
            val diagnosis = diagnosisRepo.getModelForDiagnosis(incomingCertDiagnosis.code, currentModelVersion)
            log.debug("Got diagnosis $diagnosis")

            if (diagnosis != null) {
                diagnosPrediktion.diagnos = buildDiagnos(diagnosis.diagnosisId)
                diagnosPrediktion.prevalens = diagnosis.prevalence
            }

            log.debug("certificateId ${incomingCertDiagnosis.certificateId} diagnosis ${diagnosis?.diagnosisId}")
            diagnosPrediktion.risksignal = Risksignal()
            // Fill extension certificates (index>0) with historic entries and fill the current with historic or calculated risk
            if ((!predictIndividualRisk || index > 0) && incomingCertDiagnosis.certificateId.isNotEmpty() && diagnosis != null) {
                log.trace("Do not predict individual risk for index:$index, looking for historic entries on the certificate/diagnosis")
                fillWithHistoricPrediction(diagnosPrediktion, incomingCertDiagnosis, diagnosis)
            } else if (index == 0 && diagnosis != null && predictIndividualRisk) {
                log.trace("Predict individual risk, we got a diagnosis and got correct prediction params")
                fillWithCalculatedPrediction(diagnosPrediktion, person, incomingCertDiagnosis, extraParams, diagnosis, daysIntoSickLeave)
                logPrediction(extraParams, diagnosPrediktion.diagnos?.code ?: "",
                    diagnosis?.prevalence?.toString() ?: "", person.sex.name, person.ageCategory,
                    diagnosPrediktion?.sannolikhetOvergransvarde?.toString() ?: "", diagnosPrediktion.risksignal.riskkategori,
                    diagnosPrediktion?.diagnosprediktionstatus?.toString() ?: "", incomingCertDiagnosis.certificateId, careUnitHsaId)

            } else {
                log.trace("No consent was given, no prediction was requested or incorrect combination of parameters, " +
                        "responding with prediction NOT_OK")
                diagnosPrediktion.diagnosprediktionstatus = Diagnosprediktionstatus.NOT_OK
                diagnosPrediktion.risksignal.riskkategori = 0
            }

            diagnosPrediktion.risksignal.beskrivning =
                    PredictionInformationUtil.categoryDescriptions[diagnosPrediktion.risksignal.riskkategori]
            outgoingPrediction.add(diagnosPrediktion)
        }
        return outgoingPrediction
    }

    /**
     * Calculates a new prediction given a set of input parameters
     */
    private fun fillWithCalculatedPrediction(diagnosPrediktionToPopulate: Diagnosprediktion, person: Person,
                                             incomingCertDiagnosis: CertDiagnosis, extraParams: Map<String, Map<String, String>>,
                                             diagnosisPredictionModel:PredictionDiagnosis, daysIntoSickLeave:Int) {

        // decorate extraParams with automatic selection based on diagnosis code
        val decoratedExtraParams = decorateWithAutomaticSelectionParameters(diagnosisPredictionModel, incomingCertDiagnosis, extraParams)

        if (isCorrectPredictionParamsAgainstDiagnosis(diagnosisPredictionModel, decoratedExtraParams, incomingCertDiagnosis)) {

            var calculatedPrediction: Prediction? =
                rAdapter.getPrediction(person, incomingCertDiagnosis, decoratedExtraParams, daysIntoSickLeave)
            diagnosPrediktionToPopulate.diagnosprediktionstatus = calculatedPrediction?.status
            diagnosPrediktionToPopulate.berakningstidpunkt = calculatedPrediction?.timestamp

            if (calculatedPrediction?.status == Diagnosprediktionstatus.OK ||
                    calculatedPrediction?.status == Diagnosprediktionstatus.DIAGNOSKOD_PA_HOGRE_NIVA) {
                log.trace("Have diagnosis and a calculated prediction")
                diagnosPrediktionToPopulate.sannolikhetOvergransvarde = calculatedPrediction.prediction
                diagnosPrediktionToPopulate.diagnos = buildDiagnos(calculatedPrediction.diagnosis)
                diagnosPrediktionToPopulate.risksignal.riskkategori = calculateRisk(calculatedPrediction.prediction!!)
                persistProbability(diagnosPrediktionToPopulate, incomingCertDiagnosis.certificateId, extraParams) // we only want to persist user input, not the decorated extraParams
            } else {
                diagnosPrediktionToPopulate.risksignal.riskkategori = 0
            }

        } else {
            log.trace("Incorrect combination of parameters for prediction, " +
                    "responding with prediction NOT_OK")
            diagnosPrediktionToPopulate.diagnosprediktionstatus = Diagnosprediktionstatus.NOT_OK
            diagnosPrediktionToPopulate.risksignal.riskkategori = 0
        }
    }

    /**
     * Decorates the extraParams with answers to prediction factors with automatic selection
     * @param diagnosisPredictionModel Info about the prediction model containing prioritized questions and response alternatives
     * @param incomingDiagnosis The incoming full value for diagnosis
     * @param extraParams A map holding the current input data for the prediction, e.g. the user's responses
     * @return A new parameter map with automatically selected responses added.
     */
    private fun decorateWithAutomaticSelectionParameters(diagnosisPredictionModel:PredictionDiagnosis,
                                                         incomingCertDiagnosis: CertDiagnosis,
                                                         extraParams: Map<String, Map<String, String>>): Map<String, Map<String, String>> {
        val diagnosisCodeToFind = incomingCertDiagnosis.code.substring(0, incomingCertDiagnosis.code.length.coerceAtMost(4))
        log.debug("Looking for diagnosis code $diagnosisCodeToFind in automatic selection parameters")
        val newQnAMap:MutableMap<String, String> = (extraParams[QUESTIONS_AND_ANSWERS_KEY] ?: error("No QnA map was given")).toMutableMap()
        diagnosisPredictionModel.questions.forEach {pp ->
            pp.question.answers
                    .filter { a ->
                        log.debug("Checking answer ${a.predictionId} for automatic setting rule: ${a.automaticSelectionDiagnosisCode}")
                        a.automaticSelectionDiagnosisCode?.split(";")?.any { code ->
                            diagnosisCodeToFind.equals(code) } ?: false
                    }
                    .forEach {automaticResponse ->
                        log.debug("Found automatic response ${automaticResponse.predictionId} due to matching rule pattern " +
                                "${automaticResponse.automaticSelectionDiagnosisCode} for question " +
                                "${automaticResponse.question!!.predictionId} on diagnosis model ${diagnosisPredictionModel.diagnosisId}")
                        newQnAMap?.put(automaticResponse.question!!.predictionId, automaticResponse.predictionId)
                    }
        }
        val newExtraParams: MutableMap<String, Map<String, String>> = extraParams.toMutableMap()
        newExtraParams[QUESTIONS_AND_ANSWERS_KEY] = newQnAMap
        return newExtraParams
    }

    /**
     * Looks for a historic prediction and fills the result object diagnosPrediktion
     * @param diagnosPrediktion The result object to fill with historic prediction data
     * @param certificateId the certificate which had an earlier risk prediction
     * @param diagnosis Diagnosis data entity
     */
    private fun fillWithHistoricPrediction(diagnosPrediktion: Diagnosprediktion, incomingCertDiagnosis: CertDiagnosis, predictionDiagnosis:PredictionDiagnosis) {
        log.debug("fillWithHistoricPrediction(certId: ${incomingCertDiagnosis.certificateId}, diagnosis: ${incomingCertDiagnosis.code})")
        // Check if we have a historic prediction
        var diagToFind = incomingCertDiagnosis.code;
        var historicProbabilities: List<Probability> = listOf();
        if (diagToFind.length == 3) { // e.g. S52
            historicProbabilities = probabilityRepo.findByCertificateIdAndDiagnosisOrderByTimestampDesc(incomingCertDiagnosis.certificateId, diagToFind)
        } else { // e.g. F438a -> try F438a then F438 and stop (if 4 is the resolution)
            while (diagToFind.length >= predictionDiagnosis.resolution?:3 && historicProbabilities.isEmpty()) {
                log.debug("Trying to find historic prediction on diagnosis code ${diagToFind}")
                historicProbabilities = probabilityRepo.findByCertificateIdAndDiagnosisOrderByTimestampDesc(incomingCertDiagnosis.certificateId, diagToFind)
                diagToFind = diagToFind.dropLast(1);
            }
        }
        if (historicProbabilities.isNotEmpty()) {
            val historicProbability = historicProbabilities.first()
            log.trace("Found historic entry $historicProbability")
            diagnosPrediktion.sannolikhetOvergransvarde = historicProbability.probability
            diagnosPrediktion.diagnos = buildDiagnos(historicProbability.diagnosis, historicProbability.diagnosisCodeSystem)
            diagnosPrediktion.diagnosprediktionstatus = Diagnosprediktionstatus.valueOf(historicProbability.predictionStatus)
            diagnosPrediktion.inkommandediagnos.codeSystem = historicProbability.incomingDiagnosisCodeSystem
            diagnosPrediktion.inkommandediagnos.code = historicProbability.incomingDiagnosis
            diagnosPrediktion.risksignal.riskkategori = calculateRisk(historicProbability.probability)

            if (historicProbability.ownOpinion != null) {
                diagnosPrediktion.lakarbedomningRisk = EgenBedomningRiskType.fromValue(historicProbability.ownOpinion!!.opinion)
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
                historicProbability!!.patientAnswers!!.forEach { pa->
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

    private fun buildIntygId(certificateId:String, careUnitId: String): IntygId {
        return IntygId().apply {
            this.root = careUnitId;
            this.extension = certificateId;
        }
    }

    private fun buildDiagnos(code: String, codeSystem: String = "1.2.752.116.1.1.1.1.3"): Diagnos {
        val diagnos = Diagnos()
        diagnos.codeSystem = codeSystem
        diagnos.code = code
        return diagnos
    }

    private fun isCorrectPredictionParamsAgainstDiagnosis(diagnosis: PredictionDiagnosis, extraParams: Map<String, Map<String,
            String>>, incomingCertDiagnosis:CertDiagnosis): Boolean {
        val included = HashMap<String, String>()
        extraParams[QUESTIONS_AND_ANSWERS_KEY]?.map { included.put(it.key, it.value) }
        log.debug("Checking if correct prediction params, got params: $extraParams for diagnosis model: ${diagnosis.diagnosisId}, " +
            "incoming diagnosis: ${incomingCertDiagnosis.code}")

        val required = HashMap<String, List<String>>()

        diagnosis.questions.forEach {
            if (incomingCertDiagnosis.code.length == 3 && it.question.predictionId.contains("_subdiag_group")) {
                // Do nothing and continue with next...
                // We skip this since we cant use subdiag_group params on 3 character diagnosis codes
            } else {
                required.put(it.question.predictionId, it.question.answers.map { it.predictionId })
            }
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

fun originalDiagnosis(incoming: CertDiagnosis): Diagnos {
    val original = Diagnos()
    original.codeSystem = incoming.codeSystem
    original.code = incoming.code
    return original
}
