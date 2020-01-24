package se.inera.intyg.srs.vo

import org.rosuda.JRI.Rengine
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v2.Diagnosprediktionstatus
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
        /*The R statement below somehow implicitely closes the log file (closing it again causes segfault). Someone with
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

    override fun getPrediction(person: Person, diagnosis: Diagnosis, extraParams: Map<String, Map<String, String>>): Prediction {
        // Synchronizing here is an obvious performance bottle neck, but we have no choice since the R engine is
        // single-threaded, and cannot cope with concurrent calls. Intygsprojektet has accepted that R be used
        // for the execution of prediction models, and there is no reason to the believe that the number of calls
        // to this service will be excessive during the pilot. However, if this is more widely deployed once the pilot
        // is over, we would need to consider porting the R models to some solution that scales better.
        lock.withLock {
            val (model, status) = getModelForDiagnosis(diagnosis.code)

            if (model == null) {
                return Prediction(diagnosis.code, null, status, LocalDateTime.now())
            }

            try {
                loadModel(model.file)
            } catch (e: Exception) {
                log.error("Loading model file $model.fileName did not succeed: ", e)
                return Prediction(diagnosis.code, null, Diagnosprediktionstatus.NOT_OK, LocalDateTime.now())
            }

            val rDataFrame = StringBuilder("data <- data.frame(").apply {
                append("SA_Days_tot_modified = as.integer(90), ")
                append("Sex = '${person.sex.predictionString}', ")
                append("age_cat_fct = '${person.ageCategory}', ")
                append("Region = '" + extraParams[LOCATION_KEY]?.get(REGION_KEY) + "', ")
                append(extraParams[QUESTIONS_AND_ANSWERS_KEY]?.entries?.joinToString(", ", transform = { (key, value) -> "'$key' = '$value'" }))
                append(", check.names=F)")
            }.toString()
            log.trace("Evaluating rDataFrame: $rDataFrame")

            rengine.eval(rDataFrame)
            val rOutput = rengine.eval("output <- round(pch:::predict.pch(model,newdata = data)\$Surv, 2)")

            return if (rOutput != null) {
                log.debug("Successful prediction, result: " + rOutput.asDouble())
                Prediction(model.diagnosis, rOutput.asDouble(), status, LocalDateTime.now())
            } else {
                log.debug("R produced no output")
                wipeRLogFileAndReportError()
                Prediction(diagnosis.code, null, Diagnosprediktionstatus.NOT_OK, LocalDateTime.now())
            }
        }
    }

    private fun loadModel(file: File) {
        log.debug("R loading from: {}", file.absolutePath)
        rengine.eval("model <- readRDS('${file.absolutePath}')  ", false) ?: throw RuntimeException("The prediction model does not exist!")
    }

    private fun getModelForDiagnosis(diagnosisId: String): Pair<ModelFileUpdateService.Model?, Diagnosprediktionstatus> {
        var currentId = cleanDiagnosisCode(diagnosisId)
        log.debug("getModelForDiagnosis diagnosisId: {}, cleanDiagnosisId: {}", diagnosisId, currentId)
        if (currentId.length > MAX_ID_POSITIONS) {
            return Pair(null, Diagnosprediktionstatus.NOT_OK)
        }

        var status: Diagnosprediktionstatus = Diagnosprediktionstatus.OK
        while (currentId.length >= MIN_ID_POSITIONS) {
            val model = modelService.modelForCode(currentId)
            log.debug("modelForCode currentId: {}, gave: {}", currentId, model)

            if (model != null) {
                return Pair(model, status)
            }
            currentId = currentId.substring(0, currentId.length - 1)
            // Once we have shortened the code, we need to indicate that the info is not on the original level
            status = Diagnosprediktionstatus.DIAGNOSKOD_PA_HOGRE_NIVA
        }
        return Pair(null, Diagnosprediktionstatus.PREDIKTIONSMODELL_SAKNAS)
    }

    private fun cleanDiagnosisCode(diagnosisId: String): String = diagnosisId.toUpperCase(Locale.ENGLISH).replace(".", "")

}
