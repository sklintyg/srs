package se.inera.intyg.srs.service

import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import se.inera.intyg.srs.persistence.InternalStatistik
import se.inera.intyg.srs.persistence.StatistikRepository
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Component
@EnableScheduling
class StatisticsFileUpdateService(@Value("\${statistics.image.dir}") val imageDir: String,
                                  @Value("\${base.url}") val baseUrl: String,
                                  @Autowired val repo: StatistikRepository) {
    private val log = LogManager.getLogger()
    private val imageFileExtension: String = "jpg"
    private val urlExtension: String = "/image/"
    private val fileNameRegex = Regex("""\w\d{2}""")

    @Transactional
    @Scheduled(cron = "\${image.update.cron}")
    fun update() {
        log.info("Performing scheduled image update...")

        @Suppress("UNCHECKED_CAST")
        val dbEntries = repo.findAll() as List<InternalStatistik>
        val diskEntries = ArrayList<String>()

        Files.walk(Paths.get(imageDir))
                .filter { file -> isRequiredFileType(file) && isVaildFileName(file) }
                .forEach { file ->
                    val fileName = fixFileName(file)
                    val fileModifiedTime = getModifiedTime(file)
                    var existingImage = dbEntries.find { cleanDiagnosisCode(it.diagnosisId) == fileName }

                    if (existingImage == null) {
                        log.info("New file found, saving as: $fileName")
                        repo.save(InternalStatistik(fileName, buildUrl(fileName), fileModifiedTime))
                    } else if (!existingImage.timestamp.equals(fileModifiedTime)) {
                        println("Existing: ${existingImage.timestamp} Modified: $fileModifiedTime")
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

    private fun getModifiedTime(file: Path?): LocalDateTime {
        return LocalDateTime.ofInstant(
                Files.readAttributes(file, BasicFileAttributes::class.java)
                        .lastModifiedTime().toInstant(), ZoneId.systemDefault())
    }

    private fun isRequiredFileType(file: Path): Boolean {
        return Files.isRegularFile(file)
                && file.getName(file.getNameCount() - 1).toString().toLowerCase().endsWith(imageFileExtension)
    }


}