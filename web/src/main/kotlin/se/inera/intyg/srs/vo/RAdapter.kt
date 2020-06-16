package se.inera.intyg.srs.vo

import org.rosuda.JRI.Rengine
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v3.Diagnosprediktionstatus
import se.inera.intyg.srs.service.LOCATION_KEY
import se.inera.intyg.srs.service.ModelFileUpdateService
import se.inera.intyg.srs.service.QUESTIONS_AND_ANSWERS_KEY
import se.inera.intyg.srs.service.REGION_KEY
import java.io.BufferedReader
import java.io.File
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import javax.annotation.PreDestroy
import kotlin.concurrent.withLock

@Configuration
@Profile("runtime")
open class RAdapter(val modelService: ModelFileUpdateService,
               @Value("\${r.log.file.path}") val rLogFilePath: String,
               @Value("\${r.debugFlag}") debugFlag: Int) : PredictionAdapter {
    private val MIN_ID_POSITIONS = 3
    private val MAX_ID_POSITIONS = 5

    private val log = LoggerFactory.getLogger(javaClass)

    private val rengine: Rengine = Rengine(arrayOf("--vanilla"), false, null)

    private val lock: Lock = ReentrantLock()

    init {

        // Turn on logging from R
        Rengine.DEBUG = debugFlag
        startRLogging()

        // Load required library pch
        rengine.eval("library(pch)")

    }

    @PreDestroy
    fun shutdown() {
        rengine.eval("sink()")
        rengine.end()
    }

    private fun startRLogging() {
        rengine.eval("log<-file('$rLogFilePath')")
        rengine.eval("sink(log, append=TRUE)")
        rengine.eval("sink(log, append=TRUE, type='message')")
    }

    // INTYG-4481: In case of execution error in R: append log contents to main log.
    // Then clear out old R log, by closing and then reopening log file with append disabled.
    private fun wipeRLogFileAndReportError() {
        /*The R statement below somehow implicitly closes the log file (closing it again causes segfault). Someone with
          R experience should confirm this, in order to make sure we don't leak resources (file pointers).*/
        rengine.eval("sink(file = NULL)")
        val logtext = File(rLogFilePath).bufferedReader().use(BufferedReader::readText)
        log.error("""
            |Error occurred in R execution.
            |See the dump below from the R log for details:
            |---------R LOG BEGIN---------
            """.trimMargin()
                .plus("\n")
                .plus(logtext)
                .plus("\n----------R LOG END----------"))

        startRLogging()
    }

    /**
     * Get a prediction of the probability that the sick leave will last longer than 90 days.
     * @param person The person on sick leave
     * @param diagnosis The main diagnosis
     * @param extraParams A map of other parameters for the prediction.
     * Shall be a map of two entries, one entry is a location map and the other entry is a
     * "questions and answer" map.
     *
     *  Pseudo example for diagnosis model F43 (with comments) of the map in JSON notation to get an idea of the structure:
     *
     *  "extraParams": {
     *          "Location": {
     *                  "Region": "VAST",
     *              },
     *          "QuestionsAndAnswers": {
     *              // Haft annat sjukskrivningsfall som blev längre än 14 dagar i sträck, senaste 12 månaderna
     *              "SA_1_gross": "0", // Nej
     *
     *              // Vård för F430 (Akut stressreaktion) senaste 12 månaderna
     *              "any_visits_-365_+6_F430": "0", // Nej
     *
     *              // Vård för F438 (Andra specificerade reaktioner på svår stress) senaste 12 månaderna
     *              "any_visits_-365_+6_F438": "0", // Nej
     *
     *              // Sjukskrivningsgrad i början av detta sjukskrivningsfall
     *              "SA_ExtentFirst": "1", // 100%
     *
     *              // Huvudsaklig sysselsättning vid detta sjukskrivningsfalls början
     *              "SA_SyssStart_fct": "not_unempl", // Yrkesarbetar, Föräldraledig, Studerar
     *
     *              // Född i Sverige
     *              "birth_cat_fct": "SW" // Ja
     *          }
     *  }
     *
     * The values for the questions and answer map corresponds to the model of
     * the diagnosis and is specified in an Excel document, see SRS_PM_indata_2_1.xls
     *
     * Questions (or parameters) are referred to as variables in the document, possible answer values are referred
     * to as factors.
     *
     * Some variables and factors are the same for all models, these are:
     *
     * "SA_Days_tot_modified" which is constantly 90 for all models in this implementation.
     *
     * "Sex" and "age_cat_fct" which are collected from the person object.
     *
     * "Region" which is collected from the location map
     *
     * Also see GetSRSInformationResponderImpl or PredictionInformationModuleTest to se how it is populated.
     * Also see RAdapter to understand more of how it is interpreted.
     *
     * @param daysIntoSickLeave Number of days into the sick leave when th prediction is made, the first calculation is done based on day 15
     */
    override fun getPrediction(person: Person,
                               diagnosis: CertDiagnosis,
                               extraParams: Map<String, Map<String, String>>,
                               daysIntoSickLeave:Int): Prediction {

        log.debug("RAdapter.getPrediction for diagnosis {}", diagnosis.code)
        // Synchronizing here is an obvious performance bottle neck, but we have no choice since the R engine is
        // single-threaded, and cannot cope with concurrent calls. Intygsprojektet has accepted that R be used
        // for the execution of prediction models, and there is no reason to the believe that the number of calls
        // to this service will be excessive during the pilot. However, if this is more widely deployed once the pilot
        // is over, we would need to consider porting the R models to some solution that scales better.
        lock.withLock {
            val (model, status) = getModelForDiagnosis(diagnosis.code)

            if (model == null) {
                return Prediction(diagnosis.code, null, status, LocalDateTime.now(), daysIntoSickLeave, null)
            }

            try {
                loadModel(model.file)
            } catch (e: Exception) {
                log.error("Loading model file $model.fileName did not succeed: ", e)
                return Prediction(diagnosis.code, null, Diagnosprediktionstatus.NOT_OK, LocalDateTime.now(), daysIntoSickLeave, model.version)
            }

            val rDataFrame = StringBuilder("data <- data.frame(").apply {
                append("SA_Days_tot_modified = as.integer(90), ") //Calculate the probability that the sick leave lasts longer than 90 days
                append("Sex = '${person.sex.predictionString}', ")
                append("age_cat_fct = '${person.ageCategory}', ")
                append("Region = '" + extraParams[LOCATION_KEY]?.get(REGION_KEY) + "', ")
                append(extraParams[QUESTIONS_AND_ANSWERS_KEY]?.entries?.joinToString(", ", transform = { (key, value) -> "'$key' = '$value'" }))
                append(", check.names=F)")
            }.toString()
            log.trace("Evaluating rDataFrame: $rDataFrame")

            rengine.eval(rDataFrame)
            val rOutput = rengine.eval("output <- round(pch:::predict.pch(model,newdata = data)\$Surv, 2)")
            return if (rOutput != null && daysIntoSickLeave <= 15) {
                // The normal case is that we do the initial prediction at 15 days into the sick leave
                val prediction = rOutput.asDouble()
                log.debug("Successful prediction, result: " + prediction)
                val actualDiagnosisPredicted = getActualPredictedDiagnosis(diagnosis.code, model.diagnosis, extraParams[QUESTIONS_AND_ANSWERS_KEY])
                Prediction(actualDiagnosisPredicted, prediction, status, LocalDateTime.now(), daysIntoSickLeave, model.version)
            } else if (rOutput != null && daysIntoSickLeave > 15) {
                // If we currently are more than 15 days into the sick leave we need to use Bayes Theorem P(T>X | T>Y) = P(T>X)/P(T>Y)
                // The above is read the probability that T will be bigger than X given that T is bigger than Y.
                // For example the probability that T will be bigger than 90 days (T>90) given that we already are at day 30 (T>30)
                // Thus, we need to do yet another call to R to get the P(T>Y) and use that for to calculate the wanted probability.
                val prediction = rOutput.asDouble()
                log.trace("prediction90: $prediction")

                val rDataFrame2 = StringBuilder("data <- data.frame(").apply {
                    append("SA_Days_tot_modified = as.integer($daysIntoSickLeave), ")
                    append("Sex = '${person.sex.predictionString}', ")
                    append("age_cat_fct = '${person.ageCategory}', ")
                    append("Region = '" + extraParams[LOCATION_KEY]?.get(REGION_KEY) + "', ")
                    append(extraParams[QUESTIONS_AND_ANSWERS_KEY]?.entries?.joinToString(", ", transform = { (key, value) -> "'$key' = '$value'" }))
                    append(", check.names=F)")
                }.toString()
                log.trace("Evaluating rDataFrame2: $rDataFrame2")

                rengine.eval(rDataFrame2)
                val rOutput2 = rengine.eval("output2 <- round(pch:::predict.pch(model,newdata = data)\$Surv, 2)")
                val prediction2 = rOutput2.asDouble();
                log.trace("prediction2 ($daysIntoSickLeave days into): $prediction2")
                val predictionDaysInto = prediction/prediction2
                log.trace("predictionDaysInto: $predictionDaysInto")
                val actualDiagnosisPredicted = getActualPredictedDiagnosis(diagnosis.code, model.diagnosis, extraParams[QUESTIONS_AND_ANSWERS_KEY])
                Prediction(actualDiagnosisPredicted, predictionDaysInto, status, LocalDateTime.now(), daysIntoSickLeave, model.version)
            } else {
                log.debug("R produced no output")
                wipeRLogFileAndReportError()
                Prediction(diagnosis.code, null, Diagnosprediktionstatus.NOT_OK, LocalDateTime.now(), daysIntoSickLeave, model.version)
            }
        }
    }

    /**
     * We might actually have done the prediction for a 4 character diagnosis code even though the model is based on 3 character codes.
     *
     * If we have question id's ending with "[prediction model's diag code]_subdiag_group" then the answer has been
     * set automatically to get a more accurate prediction using 4 letters of the diagnosis diagnosis code (e.g. F348A -> F438) instead
     * of the usual 3 letters (F438A -> F43).
     * The model is normally for 3 letter diagnosis codes but since we add the extra question we get a 4 letter accuracy in those cases
     * and want to reflect that in the returned Prediction object.
     */
    private fun getActualPredictedDiagnosis(incomingDiagnosis:String, predicionModelDiagnosis:String,
                                                                       questionsAndAnswers: Map<String, String>?): String {
        if (questionsAndAnswers == null) {
            return predicionModelDiagnosis
        }
        // check if we have a question id containing "${predicionModelDiagnosis}_subdiag_group", that has a non blank answer
        val hasSubDiagGroupAutomaticAnswer = questionsAndAnswers.keys
                .any { k -> k.contains("${predicionModelDiagnosis}_subdiag_group") && !questionsAndAnswers[k].isNullOrBlank() }
        return if (hasSubDiagGroupAutomaticAnswer) {
            incomingDiagnosis.substring(0, Math.min(incomingDiagnosis.length, 4))
        } else {
            predicionModelDiagnosis;
        }
    }

    private fun loadModel(file: File) {
        log.debug("R loading from: {}", file.absolutePath)
        rengine.eval("model <- readRDS('${file.absolutePath}')  ", false) ?: throw RuntimeException("The prediction model does not exist!")
    }

    /**
     * Finds an R model for the given diagnosis code.
     * If we have a perfect match, e.g. M75 then return that model immediately and set status OK
     *
     * If we don't have a perfect match for e.g. F438A, shorten the string until we find the model e.g. F43 and flag with
     * status "diagnosis on higher level".
     *
     * If we get a too long input, return no model with status NOT_OK
     *
     * If the input diagnosis code looks ok but we didn't manage to find a model, set status "prediction model missing"
     *
     */
    private fun getModelForDiagnosis(diagnosisId: String): Pair<ModelFileUpdateService.Model?, Diagnosprediktionstatus> {
        var currentId = cleanDiagnosisCode(diagnosisId)
        log.debug("getModelForDiagnosis diagnosisId: {}, cleanDiagnosisId: {}", diagnosisId, currentId)
        var status: Diagnosprediktionstatus = Diagnosprediktionstatus.OK
        if (currentId.length == 3) {
            log.debug("We got a three character diagnosis code, fallback to model without subdiag group params");
            val model = modelService.modelForCodeWithoutSubdiag(currentId)
            log.debug("modelForCodeWithoutSubDiag currentId: {}, gave: {}", currentId, model?.version)
            if (model != null) {
                return Pair(model, status);
            }
        } else {
            if (currentId.length > MAX_ID_POSITIONS) {
                return Pair(null, Diagnosprediktionstatus.NOT_OK)
            }

            // Find a suitable model by cutting of character by character from the end of the diagnosis code
            while (currentId.length > MIN_ID_POSITIONS) {
                val model = modelService.modelForCode(currentId)
                log.debug("modelForCode currentId: {}, gave: {}", currentId, model)


                if (model != null) {
                    return Pair(model, status)
                }
                // Since we didn't find a model, Remove one character from the end of the id/diagnosisCode string to try again
                currentId = currentId.substring(0, currentId.length - 1)

                // Once we have shortened the code, we need to indicate that the info is not on the original level
                status = Diagnosprediktionstatus.DIAGNOSKOD_PA_HOGRE_NIVA
            }
            // if no hit when we reach the minimum length, try to find both with and after that without subdiag support at the minimum
            // length otherwise we give up and return that the prediction model is missing
            var model = modelService.modelForCode(currentId)
            if (model != null) {
                return Pair(model, status)
            } else {
                val modelWithoutSubdiag = modelService.modelForCodeWithoutSubdiag(currentId)
                if (modelWithoutSubdiag != null) {
                    return Pair(modelWithoutSubdiag, status)
                }
            }
        }
        return Pair(null, Diagnosprediktionstatus.PREDIKTIONSMODELL_SAKNAS)
    }

    private fun cleanDiagnosisCode(diagnosisId: String): String = diagnosisId.toUpperCase(Locale.ENGLISH).replace(".", "")

}
