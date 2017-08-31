package se.inera.intyg.srs.service

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Scheduled service for monitoring R model files in models.dir.
 * The update interval is configurable via image.update.cron.
 */
@Component
@EnableScheduling
class ModelFileUpdateService(@Value("\${model.dir}") val modelDir: String) {

    private val log : Logger

    init {
        log = LogManager.getLogger()
        update()
    }

    private val DATA_FILE_EXTENSION = ".rdata"

    private val models = mutableMapOf<String, Model>()

    fun modelForCode(currentId: String): Model? {
        return models.get(currentId)
    }

    @Scheduled(cron = "\${model.update.cron}")
    final private fun update() {
        log.info("Performing scheduled model file update...")

        try {
            Files.walk(Paths.get(modelDir)).filter {
                Files.isRegularFile(it) && it.getName(it.getNameCount() - 1).toString().toLowerCase().endsWith(DATA_FILE_EXTENSION)
            }.forEach { file ->
                addFile(file)
            }
        } catch (e: IOException) {
            log.error("Error while reading from directory $modelDir: ", e)
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

    class Model(val diagnosis: String, val version: String, val fileName: String)

}