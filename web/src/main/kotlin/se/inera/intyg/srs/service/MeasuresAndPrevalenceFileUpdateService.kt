package se.inera.intyg.srs.service

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.annotation.Order
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Component
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Atgardstyp
import se.inera.intyg.srs.persistence.repository.DiagnosisRepository
import se.inera.intyg.srs.persistence.entity.Measure
import se.inera.intyg.srs.persistence.entity.MeasurePriority
import se.inera.intyg.srs.persistence.repository.MeasurePriorityRepository
import se.inera.intyg.srs.persistence.repository.MeasureRepository
import se.inera.intyg.srs.persistence.entity.Recommendation
import se.inera.intyg.srs.persistence.repository.RecommendationRepository
import java.time.LocalDateTime

/**
 * Updates the measure recommendations and the diagnosis prevalences from file
 */
@Component
@Order(100)
class MeasuresAndPrevalenceFileUpdateService(@Value("\${recommendations.file}") val recommendationsFile: String,
                                             @Value("\${recommendations.importMaxLines: 1000}") val importMaxLines: Int,
                                             @Value("\${model.currentVersion}") val currentModelVersion: String,
                                             val recommendationsRepo: RecommendationRepository,
                                             val measureRepo: MeasureRepository,
                                             val measurePriorityRepo: MeasurePriorityRepository,
                                             val diagnosisRepo: DiagnosisRepository,
                                             val resourceLoader: ResourceLoader) {

    private val log = LoggerFactory.getLogger(javaClass)

    final fun doUpdate() {
        doRemoveOldRecommendations()
        doUpdateRecommendationsAndPrevalence()
    }

    private fun doRemoveOldRecommendations() {
        log.info("Removing all measures and priorities")
        measurePriorityRepo.deleteAll()
        recommendationsRepo.deleteAll()
        measureRepo.deleteAll()
    }

    private fun importRecommendations(workbook: XSSFWorkbook, importTimestamp: LocalDateTime) {
        val sheet = workbook.getSheet("Kopplingstabell")
        var readMoreRows = true
        var rowNumber = 2
        var unimportableRows = 0
        var newMeasures = 0
        var doubletMeasures = 0
        var newRecommendations = 0
        var doubletRecommendations = 0
        var rowsIterated = 0
        while (readMoreRows && unimportableRows < 4) {
            val row = sheet.getRow(rowNumber) ?: break
            val diagnosisId = row.getCell(0).stringCellValue.replace(".","")
            if (!diagnosisId.isNullOrBlank()) {
                log.debug("cell 0, diagnosisId: $diagnosisId")
                log.debug("cell 1, recommendationId: {}", row.getCell(1))
                val category = row.getCell(2).stringCellValue
                var recommendationId = when (category) {
                    "FRL" -> row.getCell(1).stringCellValue.replace("F","9999").toLong()
                    "REH" -> row.getCell(1).stringCellValue.replace("R","8888").toLong()
                    else -> row.getCell(1).numericCellValue.toLong()
                }
                val priority = row.getCell(3).numericCellValue.toInt()
                val diagnosisText = row.getCell(4).stringCellValue
                val title = row.getCell(5).stringCellValue
                val text = row.getCell(6).stringCellValue

                if (!diagnosisText.isNullOrBlank() && (!title.isNullOrBlank() || !text.isNullOrBlank()) && !category.isNullOrBlank()) {
                    log.debug("Found recommendation in import file {}, {}, {}, {}, {}, {}",
                            diagnosisId, diagnosisText, recommendationId, category, priority, text)

                    // Only create new ones if we haven't already imported the measure and/or recommendation
                    // No need for updates since we always clear the recommendations and measures at each import
                    var measure = measureRepo.findByDiagnosisId(diagnosisId).orElse(null)
                    if (measure == null) {
                        log.debug("Didnt find measure with diagnosisId $diagnosisId in db, saving new entity")
                        measure = measureRepo.save(Measure(diagnosisId, diagnosisText, importTimestamp.toString()))
                        newMeasures++
                    } else {
                        doubletMeasures++
                    }
                    var recommendation = recommendationsRepo.findById(recommendationId).orElse(null)
                    if (recommendation == null) {
                        log.debug("Didnt find recommendation with id $recommendationId in db, saving new entity")
                        recommendation = recommendationsRepo.save(Recommendation(Atgardstyp.fromValue(category), title, text, recommendationId))
                        newRecommendations++
                    } else {
                        doubletRecommendations++
                    }
                    measurePriorityRepo.save(MeasurePriority(priority, recommendation!!, measure))
                } else {
                    log.debug("Unimportable row number $rowNumber")
                    unimportableRows++
                }
            } else {
                log.debug("Unimportable row number $rowNumber")
                unimportableRows++
            }
            rowNumber++
            if (rowNumber > importMaxLines) {
                readMoreRows = false
            }
            rowsIterated++
        }
        if (unimportableRows > importMaxLines) {
            log.warn("Stopping measure/recommendation import since we reached the maximum number of rows to import at rowNumber: $rowNumber")
        } else {
            log.info("Stopping measure/recommendation import due to unimportable rows, assuming we reached the end of the file at rowNumber: $rowNumber")
        }
        log.info("Finished measure and recommendation update, read $rowsIterated rows. " +
                "Imported measures: $newMeasures, doublets: $doubletMeasures. " +
                "Imported recommendations: $newRecommendations, doublets: $doubletRecommendations. " +
                "Unimportable rows (1 or 4 is normal to detect end of file): $unimportableRows")
    }

    private fun updatePrevalence(workbook: XSSFWorkbook) {
        val sheet = workbook.getSheet("SRS_DIAGNOSER")
        var readMoreRows = true
        var rowNumber = 3
        var updatedPrevalences = 0
        var unimportableRows = 0
        while (readMoreRows && unimportableRows < 4) {
            val row = sheet.getRow(rowNumber) ?: break
            val diagnosisId = row.getCell(1).stringCellValue.replace(".","")
            if (diagnosisId.isNullOrBlank()) {
                log.debug("Unimportable row number $rowNumber")
                unimportableRows++
            } else if (diagnosisId.length == 3) {
                val prevalence = row.getCell(3).numericCellValue
                log.debug("Setting prevalence for $diagnosisId to $prevalence")
                diagnosisRepo.findByDiagnosisIdAndModelVersion(diagnosisId, currentModelVersion).let { diags -> // with and without subdiags
                    diags.forEach { diagnosis ->
                        diagnosisRepo.save(diagnosis!!.copy(prevalence = prevalence))
                        updatedPrevalences++
                    }
                }
            }
            rowNumber++
            if (rowNumber > importMaxLines) {
                readMoreRows = false
            }
        }
        if (rowNumber > importMaxLines) {
            log.warn("Stopping prevalence import since we reached the maximum number of rows to import at rowNumber: $rowNumber")
        } else {
            log.info("Stopping prevalence import due to unimportable rows, assuming we reached the end of the file at rowNumber: $rowNumber")
        }
        log.info("Finished updating $updatedPrevalences prevalences at rowNumber: $rowNumber. " +
                "Unimportable rows (1 or 4 is normal to detect end of file): $unimportableRows")
    }

    private final fun doUpdateRecommendationsAndPrevalence() {
        log.info("Performing import of recommendations and prevalence from file $recommendationsFile")
        val importTimestamp = LocalDateTime.now()
        val excelFileStream = resourceLoader.getResource(recommendationsFile).inputStream
        XSSFWorkbook(excelFileStream).use { workbook ->
            importRecommendations(workbook, importTimestamp)
            updatePrevalence(workbook)
        }
        log.info("Finished updating recommendations and prevalence")
    }

}
