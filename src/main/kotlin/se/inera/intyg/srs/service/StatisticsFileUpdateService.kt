package se.inera.intyg.srs.service

import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import se.inera.intyg.srs.persistence.InternalStatistic
import se.inera.intyg.srs.persistence.StatisticRepository
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale

/**
 * Scheduled service for checking for new statistics images in the dir specified with statistics.image.dir,
 * the update interval is also configurable via image.update.cron, both in application.properties.
 */
@Component
@EnableScheduling
@Profile("scheduledUpdate")
class StatisticsFileUpdateService(@Value("\${statistics.image.dir}") val imageDir: String,
                                  @Value("\${base.url}") val baseUrl: String,
                                  @Autowired val repo: StatisticRepository) {

    private val log = LogManager.getLogger()

    private val imageFileExtension: String = "jpg"

    private val urlExtension: String = "/image/"

    // For now, only codes with three positions are allowed.
    private val fileNameRegex = Regex("""\w\d{2}""")

    init {
        doUpdate()
    }

    @Transactional
    @Scheduled(cron = "\${image.update.cron}")
    fun update() {
        doUpdate()
    }

    private final fun doUpdate() {
        log.info("Performing scheduled image update...")

        val dbEntries: List<InternalStatistic> = repo.findAll().toList()
        val diskEntries = ArrayList<String>()

        File(imageDir).walk()
                .map { it.toPath() }
                .filterNotNull()
                .filter { file -> isRequiredFileType(file) && isVaildFileName(file) }
                .forEach { file ->
                    val fileName = fixFileName(file)
                    val fileModifiedTime = getModifiedTime(file)
                    val existingImage = dbEntries.find { cleanDiagnosisCode(it.diagnosisId) == fileName }

                    if (existingImage == null) {
                        log.info("New file found, saving as: $fileName")
                        repo.save(InternalStatistic(fileName, buildUrl(fileName), fileModifiedTime))
                    } else if (!existingImage.timestamp.equals(fileModifiedTime)) {
                        log.info("Existing but modified file found, updating $fileName")
                        existingImage.timestamp = fileModifiedTime
                        repo.save(existingImage)
                    }

                    diskEntries.add(fileName)
                }

        // Cleanup removed files
        if (diskEntries.size != dbEntries.size) {
            dbEntries.filter { it.diagnosisId !in diskEntries }.forEach {
                log.info("Statistics image for ${it.diagnosisId} no longer available, removing from database.")
                repo.delete(it.id)
            }
        }
    }

    private fun fixFileName(file: Path): String = file.fileName.toString().dropLast(4)

    private fun isVaildFileName(file: Path): Boolean = fileNameRegex.matches(fixFileName(file))

    private fun buildUrl(fileName: String): String = "$baseUrl$urlExtension$fileName"

    private fun cleanDiagnosisCode(diagnosisId: String): String = diagnosisId.toUpperCase(Locale.ENGLISH).replace(".", "")

    private fun getModifiedTime(file: Path): LocalDateTime {
        return LocalDateTime.ofInstant(
                Files.readAttributes(file, BasicFileAttributes::class.java)
                        .lastModifiedTime().toInstant(), ZoneId.systemDefault())
    }

    private fun isRequiredFileType(file: Path): Boolean {
        return Files.isRegularFile(file)
                && file.getName(file.getNameCount() - 1).toString().toLowerCase().endsWith(imageFileExtension)
    }

}
