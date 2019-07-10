package se.inera.intyg.srs.service

import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import org.springframework.stereotype.Component
import java.io.File

/**
 * Scheduled service for monitoring R model files in models.dir.
 * The update interval is configurable via image.update.cron.
 */
@Component
class ModelFileUpdateService(val resourceLoader: ResourceLoader,
        @Value("\${model.location-pattern}") val locationPattern: String) {

    private val log = LogManager.getLogger()

    private val DATA_FILE_EXTENSION = ".rds"

    private var models = mapOf<String, List<Model>>()

    init {
        update()
    }

    fun modelForCode(currentId: String, version: String? = null): Model? =
            if (version == null) {
                models[currentId]?.maxBy { it.version }
            } else {
                models[currentId]?.find { it.version == version }
            }

    fun update() {
        doUpdate(locationPattern)
    }

    fun applyModels(resources: List<Resource>) {
        log.info("Applying models: {}", resources)
        models = collect(Sequence { resources.iterator() })
    }

    private final fun doUpdate(locationPattern: String) {
        log.info("Performing model update... locationPattern: {}", locationPattern)
        models = collect(Sequence {
            ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(locationPattern).iterator()
        })
        log.info("Models found {}", models?.map {(k,v) -> k })
    }

    private fun collect(sequence: Sequence<Resource>): Map<String, List<Model>> {
        return sequence
                .filter { it.filename.toLowerCase().endsWith(DATA_FILE_EXTENSION) }
                .map { toModel(it) }
                .groupBy { it.diagnosis }
    }


    private fun toModel(resource: Resource): Model {
        val name = resource.filename
        val dStartPos = name.indexOf('_')
        val dEndPos = name.lastIndexOf('_')
        val vEndPos = name.indexOf('.')
        val diagnosis = name.substring(dStartPos + 1, dEndPos)
        val version = name.substring(dEndPos + 1, vEndPos)
        val file = File.createTempFile(resource.filename, DATA_FILE_EXTENSION)
        val out = file.outputStream()
        try {
            resource.inputStream.copyTo(out)
        } finally {
            out.close()
        }
        file.deleteOnExit()
        return Model(diagnosis, version, file)
    }

    class Model(val diagnosis: String, val version: String, val file: File)

}
