package se.inera.intyg.srs.vo

import org.apache.logging.log4j.LogManager
import org.rosuda.JRI.Rengine
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Diagnosprediktionstatus
import se.inera.intyg.srs.service.ModelFileUpdateService
import java.util.*
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import javax.annotation.PreDestroy
import kotlin.concurrent.withLock

@Configuration
@Profile("runtime")
class RAdapter(val modelService: ModelFileUpdateService) : PredictionAdapter {
    private val MIN_ID_POSITIONS = 3

    private val log = LogManager.getLogger()

    private val rengine: Rengine = Rengine(arrayOf("--vanilla"), false, null)

    init {

        // Turn on logging from R
        Rengine.DEBUG = 1
        val filepath = "/tmp/r-log"
        rengine.eval("log<-file('$filepath')")
        rengine.eval("sink(log, append=TRUE)")
        rengine.eval("sink(log, append=TRUE, type='message')")

        // Load required library pch
        rengine.eval("library(pch)")
    }

    @PreDestroy
    fun shutdown() {
        rengine.end()
    }

    override fun getPrediction(person: Person, diagnosis: Diagnosis, extraParams: Map<String, String>): Prediction {
        // Synchronizing here is an obvious performance bottle neck, but we have no choice since the R engine is
        // single-threaded, and cannot cope with concurrent calls. Intygsprojektet has accepted that R be used
        // for the execution of prediction models, and there is no reason to the believe that the number of calls
        // to this service will be excessive during the pilot. However, if this is more widely deployed once the pilot
        // is over, we would need to consider porting the R models to some solution that scales better.
        val lock: Lock = ReentrantLock()
        lock.withLock {
            val (model, status) = getModelForDiagnosis(diagnosis.code)

            if (model == null) {
                return Prediction(diagnosis.code, null, Diagnosprediktionstatus.PREDIKTIONSMODELL_SAKNAS)
            }

            try {
                loadModel(model.fileName)
            } catch (e: Exception) {
                log.error("Loading model file $model.fileName did not succeed: ", e)
                return Prediction(diagnosis.code, null, Diagnosprediktionstatus.NOT_OK)
            }

            val rDataFrame = StringBuilder("data <- data.frame(").apply {
                append("SA_Days_tot_modified = as.integer(90), ")
                append("Sex = '${person.sex.predictionString}', ")
                append("age_cat_fct = '${person.ageCategory}', ")
                append(extraParams.entries.joinToString(", ", transform = { (key, value) -> "$key = '$value'" }))
                append(")")
            }.toString()

            rengine.eval(rDataFrame)
            val rOutput = rengine.eval("output <- round(predict(model,newdata = data)\$Surv, 2)")

            return if (rOutput != null) {
                log.info("Successful prediction, result: " + rOutput.asDouble())
                Prediction(model.diagnosis, rOutput.asDouble(), status)
            } else {
                log.error("An error occurred during execution of the prediction model.")
                Prediction(diagnosis.code, null, Diagnosprediktionstatus.NOT_OK)
            }
        }
    }

    private fun loadModel(dataFilePath: String) {
        rengine.eval("load('$dataFilePath')  ", false) ?: throw RuntimeException("The prediction model does not exist!")
    }

    private fun getModelForDiagnosis(diagnosisId: String): Pair<ModelFileUpdateService.Model?, Diagnosprediktionstatus> {
        var currentId = cleanDiagnosisCode(diagnosisId)
        var status: Diagnosprediktionstatus = Diagnosprediktionstatus.OK
        while (currentId.length >= MIN_ID_POSITIONS) {
            val model = modelService.modelForCode(currentId)
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
