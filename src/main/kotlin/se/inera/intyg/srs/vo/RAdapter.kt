package se.inera.intyg.srs.vo

import org.apache.logging.log4j.LogManager
import org.rosuda.JRI.Rengine
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Diagnosprediktionstatus
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import javax.annotation.PreDestroy

@Configuration
@Profile("runtime")
class RAdapter() : PredictionAdapter {
    private val MIN_ID_POSITIONS = 3

    private val DATA_FILE_EXTENSION = ".rdata"

    private val log = LogManager.getLogger()

    lateinit var rengine: Rengine

    val dirPath = "/opt/models"  // TODO: should be configurable

    private val models = mutableMapOf<String, Model>()

    // These are hard coded for now, but will later be configurable.
    val DAYS = "90"
    val EMPLOYMENT = "emp."
    val SICKDAYS_LASTYEAR = "0"

    init {
        try {
            Files.walk(Paths.get(dirPath)).filter {
                Files.isRegularFile(it) && it.getName(it.getNameCount() - 1).toString().toLowerCase().endsWith(DATA_FILE_EXTENSION)
            }.forEach { file ->
                addFile(file)
            }

            rengine = Rengine(arrayOf("--vanilla"), false, null)
            rengine.eval("library(pch)")
        } catch (e: IOException) {
            log.error("Error while reading from directory $dirPath: ", e)
        } catch (e: Exception) {
            log.error("Error while initializing R engine: ", e)
        }
    }

    private fun addFile(file: Path) {
        val fileName = file.fileName.toString()
        val dStartPos = fileName.indexOf('_')
        val dEndPos = fileName.lastIndexOf('_')
        val vEndPos = fileName.indexOf('.')
        val diagnosis = fileName.substring(dStartPos + 1, dEndPos)
        val version = fileName.substring(dEndPos + 1, vEndPos)
        models.put(diagnosis, Model(diagnosis, version, file.toAbsolutePath().toString()))
    }

    @PreDestroy
    fun shutdown() {
        rengine.end()
    }

    override fun getPrediction(person: Person, diagnosis: Diagnosis): Prediction {

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

        val rDataFrame = "data <- data.frame(" +
                "days = as.integer(" + DAYS + "), " +
                "age = as.integer(" + person.age + "), " +
                "sex = '" + person.sex.predictionString + "', " +
                "SA_syssStart = '" + EMPLOYMENT + "', " +
                "SA_ExtentFirst = '" + person.extent.predictionString + "', " +
                "SA_total_grossd_Yminus1 = '" + SICKDAYS_LASTYEAR + "')"

        val cmdPrediction = "output <- round(predict(model,newdata = data)\$Surv, 2)"

        rengine.eval(rDataFrame)
        val rOutput = rengine.eval(cmdPrediction)

        if (rOutput != null) {
            log.info("Successful prediction, result: " + rOutput.asDouble())
            return Prediction(model.diagnosis, rOutput.asDouble(), status)
        } else {
            log.error("An error occurred during execution of the prediction model: ")
            return Prediction(diagnosis.code, null, Diagnosprediktionstatus.NOT_OK)
        }
    }

    fun loadModel(dataFilePath: String) {
        val loadmodel_result = rengine.eval("load('$dataFilePath')  ", false)
        if (loadmodel_result == null) {
            throw RuntimeException("The prediction model does not exist!")
        }
    }

    private fun getModelForDiagnosis(diagnosisId: String): Pair<Model?, Diagnosprediktionstatus> {
        var currentId = cleanDiagnosisCode(diagnosisId)
        var status: Diagnosprediktionstatus = Diagnosprediktionstatus.OK
        while (currentId.length >= MIN_ID_POSITIONS) {
            val model = modelForCode(currentId)
            if (model != null) {
                return Pair(model, status)
            }
            currentId = currentId.substring(0, currentId.length - 1)
            // Once we have shortened the code, we need to indicate that the info is not on the original level
            status = Diagnosprediktionstatus.DIAGNOSKOD_PA_HOGRE_NIVA
        }
        return Pair(null, Diagnosprediktionstatus.PREDIKTIONSMODELL_SAKNAS)
    }

    private fun modelForCode(currentId: String): Model? {
        return models.get(currentId)
    }

    private fun cleanDiagnosisCode(diagnosisId: String): String = diagnosisId.toUpperCase(Locale.ENGLISH).replace(".", "")

    private class Model(val diagnosis: String, val version: String, val fileName: String)
}
