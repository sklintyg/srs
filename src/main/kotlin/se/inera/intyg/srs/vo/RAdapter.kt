package se.inera.intyg.srs.vo

import org.apache.logging.log4j.LogManager
import org.rosuda.JRI.Rengine
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import javax.annotation.PreDestroy

@Configuration
@Profile("runtime")
class RAdapter() : PredictionAdapter {
    private val DATA_FILE_EXTENSION = ".rdata"

    private val log = LogManager.getLogger()

    lateinit var rengine: Rengine

    val dirPath = "/opt/models"

    // These are hard coded for now, but will later be configurable
    val DAYS = "90"
    val EMPLOYMENT = "emp."
    val SICKDAYS_LASTYEAR = "0"

    init {
        // TODO: for now set data file to last found file. This will have to be more sophisticated later on.
        try {
            var dataFilePath: String? = null
            Files.walk(Paths.get(dirPath)).filter {
                Files.isRegularFile(it) && it.getName(it.getNameCount() - 1).toString().toLowerCase().endsWith(DATA_FILE_EXTENSION)
            }.forEach { file ->
                dataFilePath = file.toAbsolutePath().toString()
            }

            if (dataFilePath != null) {
                rengine = Rengine(arrayOf("--vanilla"), false, null)
                val library_result = rengine.eval("library(pch)")
                val loadmodel_result = rengine.eval("load('$dataFilePath')  ", false)
                if (loadmodel_result == null) {
                    throw RuntimeException("The prediction model does not exist!")
                }
            } else {
                log.error("No R data files found in $dirPath")
                throw Exception("No R data files found in $dirPath")
            }
        } catch (e: IOException) {
            log.error("Error while reading from directory $dirPath: ", e)
            throw(e)
        } catch (e: Exception) {
            log.error("Error while initializing R engine: ", e)
            throw(e)
        }
    }

    @PreDestroy
    fun shutdown() {
        rengine.end()
    }

    override fun getPrediction(person: Person, /* Ignored, will later determine which R file to use.*/ diagnose: Diagnose): Double {
        var prediction = 0.0

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
            prediction = rOutput.asDouble()
            println("\nSuccessful prediction, result: " + prediction)
        } else {
            println("\n** An error occurred during execution of the prediction model!")
        }
        return prediction;
    }

}
