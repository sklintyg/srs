package se.inera.intyg.srs.service

import org.slf4j.LoggerFactory
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
        @Value("\${model.location-pattern}") val locationPattern: String,
        @Value("\${model.location-pattern-without-subdiag}") val locationPatternWithoutSubdiag:String ) {

    private val log = LoggerFactory.getLogger(javaClass)

    private val DATA_FILE_EXTENSION = ".rds"

    private var models = mapOf<String, List<Model>>()
    private var modelsWithoutSubdiag = mapOf<String, List<Model>>()

    init {
        update()
    }

    fun modelForCodeWithoutSubdiag(currentId: String, version: String? = null): Model? =
        if (version == null) {
            modelsWithoutSubdiag[currentId]?.maxBy { it.version }
        } else {
            modelsWithoutSubdiag[currentId]?.find { it.version == version }
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

    fun applyModelsWithoutSubdiag(resources: List<Resource>) {
        log.info("Applying models without sub diags: {}", resources)
        modelsWithoutSubdiag = collect(Sequence { resources.iterator() })
    }

    private final fun doUpdate(locationPattern: String) {
        log.info("Performing model update... locationPattern: {}", locationPattern)
        models = collect(Sequence {
            ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(locationPattern).iterator()
        })
        log.info("Models found {}", models.map {(k) -> k })
        modelsWithoutSubdiag = collect(Sequence {
            ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(locationPatternWithoutSubdiag).iterator()
        })
        log.info("ModelsWithoutSubdiag found {}", modelsWithoutSubdiag.map {(k) -> k })

    }

    private fun collect(sequence: Sequence<Resource>): Map<String, List<Model>> {
        return sequence
                .filter { it.filename!!.toLowerCase().endsWith(DATA_FILE_EXTENSION) }
                .map { toModel(it) }
                .groupBy { it.diagnosis }
    }


    private fun toModel(resource: Resource): Model {
        val name = resource.filename
        val dStartPos = name!!.indexOf('_')
        val dEndPos = name.lastIndexOf('_')
        val vEndPos = name.lastIndexOf('.')
        val diagnosis = name.substring(dStartPos + 1, dEndPos)
        val version = name.substring(dEndPos + 2, vEndPos)
        val isTestModel: Boolean = diagnosis.startsWith("X99")
        val file = File.createTempFile(resource.filename, DATA_FILE_EXTENSION)
        val out = file.outputStream()
        try {
            resource.inputStream.copyTo(out)
        } finally {
            out.close()
        }
        file.deleteOnExit()
        return Model(diagnosis, version, file, isTestModel)
    }

    class Model(val diagnosis: String, val version: String, val file: File, val isTestModel:Boolean = false)

}
