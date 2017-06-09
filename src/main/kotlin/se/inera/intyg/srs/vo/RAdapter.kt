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
class RAdapter(): PredictionAdapter {
    private val DATA_FILE_EXTENSION = ".rdata"

    private val log = LogManager.getLogger()

    lateinit var rengine: Rengine

    val dirPath = "/opt/models"

    init {
        // TODO: for now set data file to last found file. This will have to be more sophisticated later on.
        try {
            var dataFilePath: String? = null
            println(Paths.get(dirPath))
            Files.walk(Paths.get(dirPath)).forEach {
                println("Path: " + it.toString())
            }
            Files.walk(Paths.get(dirPath)).filter {
                Files.isRegularFile(it) && it.getName(it.getNameCount() - 1).toString().toLowerCase().endsWith(DATA_FILE_EXTENSION)
            }.forEach { file ->
                dataFilePath = file.toAbsolutePath().toString()
            }

            if (dataFilePath != null) {
                println("Path: " + dataFilePath)
                rengine = Rengine(arrayOf("--vanilla"), false, null)
                val library_result = rengine.eval("library(pch)")
                val loadmodel_result = rengine.eval("load('$dataFilePath')  ", false)
                if (loadmodel_result == null) {
                    throw RuntimeException("The prediction model does not exist!")
                }
            }
        } catch (e: IOException) {
            log.error("Error while reading from directory {}", dirPath, e)
        } catch (e: Exception) {
            log.error("Error while initializing R engine:", e)
            throw(e)
        }
    }

    @PreDestroy
    fun shutdown() {
        rengine.end()
    }

    override fun doStuff() {
        val in_age = 62
        val in_sex = "F"
        val in_saextentfirst = "1"
        val in_days = 90
        val in_employment = "emp."
        val in_satotalgrossdyminus1 = "0"

        // -----Initialize a data frame (R) with indata parameters-----//
        val rDataFrame = "data <- data.frame(" +
                "days = as.integer(" + in_days + "), " +
                "age = as.integer(" + in_age + "), " +
                "sex = '" + in_sex + "', " +
                "SA_syssStart = '" + in_employment + "', " +
                "SA_ExtentFirst = '" + in_saextentfirst + "', " +
                "SA_total_grossd_Yminus1 = '" + in_satotalgrossdyminus1 + "')"

        val data_result = rengine.eval(rDataFrame)

        // -----Prepare the R command-----//
        val cmdPrediction = "output <- round(predict(model,newdata = data)\$Surv, 2)"
        // -----Execute R-----//
        val rOutput = rengine.eval(cmdPrediction)
        if (rOutput != null) {
            val prediction = rOutput.asDouble()
            println("\nSuccessful prediction, result: " + prediction)
        } else {
            println("\n** An error occurred during execution of the prediction model!")
        }
    }

}
