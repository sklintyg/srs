package se.inera.intyg.srs.service

import org.apache.logging.log4j.LogManager
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.annotation.Order
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Component
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Atgardstyp
import se.inera.intyg.srs.persistence.DiagnosisRepository
import se.inera.intyg.srs.persistence.Measure
import se.inera.intyg.srs.persistence.MeasurePriority
import se.inera.intyg.srs.persistence.MeasurePriorityRepository
import se.inera.intyg.srs.persistence.MeasureRepository
import se.inera.intyg.srs.persistence.Recommendation
import se.inera.intyg.srs.persistence.RecommendationRepository
import java.time.LocalDateTime

/**
 * Updates the measure recommendations and the diagnosis prevalences from file
 */
@Component
@Order(100)
class MeasuresAndPrevalenceFileUpdateService(@Value("\${recommendations.file}") val recommendationsFile: String,
                                             @Value("\${recommendations.importMaxLines: 1000}") val importMaxLines: Int,
                                             val recommendationsRepo: RecommendationRepository,
                                             val measureRepo: MeasureRepository,
                                             val measurePriorityRepo: MeasurePriorityRepository,
                                             val diagnosisRepo: DiagnosisRepository,
                                             val resourceLoader: ResourceLoader) {

    private val log = LogManager.getLogger()

    final fun doUpdate() {
        doRemoveOldRecommendations()
        doUpdateRecommendationsAndPrevalence()
    }

    private final fun doRemoveOldRecommendations() {
        measurePriorityRepo.deleteAll()
        recommendationsRepo.deleteAll()
        measureRepo.deleteAll()
    }

    private final fun updateRecommendations(workbook: XSSFWorkbook, importTimestamp: LocalDateTime) {
        val sheet = workbook.getSheet("Kopplingstabell")
        var readMoreRows = true
        var rowNumber = 2
        var unimportableRows = 0
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
                    log.debug("Found recommendation in import file {}, {}, {}, {}, {}, {}, {},",
                            diagnosisId, diagnosisText, recommendationId, category, priority, text)
                    var measure = measureRepo.findByDiagnosisId(diagnosisId).orElse(null)
                    if (measure == null) {
                        log.debug("didnt find measure with diagnosisId $diagnosisId in db, saving new entity")
                        measure = measureRepo.save(Measure(diagnosisId, diagnosisText, importTimestamp.toString()))
                    }
                    var recommendation = recommendationsRepo.findById(recommendationId).orElse(null)
                    if (recommendation == null) {
                        log.debug("didnt find recommendation with id $recommendationId in db, saving new entity")
                        recommendation = recommendationsRepo.save(Recommendation(Atgardstyp.fromValue(category), title, text, recommendationId))
                    }
                    measurePriorityRepo.save(MeasurePriority(priority, recommendation, measure))
                } else {
                    unimportableRows++
                }
            } else {
                unimportableRows++
            }
            rowNumber++
            if (rowNumber > importMaxLines) {
                readMoreRows = false
            }
        }
    }

    private final fun updatePrevalence(workbook: XSSFWorkbook) {
        val sheet = workbook.getSheet("SRS_DIAGNOSER")
        var readMoreRows = true
        var rowNumber = 3
        var unimportableRows = 0
        while (readMoreRows && unimportableRows < 4) {
            val row = sheet.getRow(rowNumber) ?: break
            val diagnosisId = row.getCell(1).stringCellValue.replace(".","")
            if (diagnosisId.isNullOrBlank()) {
                unimportableRows++
                rowNumber++
                continue
            } else if (diagnosisId.length == 3) {
                val prevalence = row.getCell(3).numericCellValue
                log.info("Setting prevalence for $diagnosisId to $prevalence")
                diagnosisRepo.findOneByDiagnosisId(diagnosisId).let { diagnosis ->
                    diagnosisRepo.save(diagnosis!!.copy(prevalence = prevalence))
                }
            }
            rowNumber++
            if (rowNumber > importMaxLines) {
                readMoreRows = false
            }
        }
    }

    private final fun doUpdateRecommendationsAndPrevalence() {
        log.info("Performing update of recommendations and prevalence from file $recommendationsFile")
        val importTimestamp = LocalDateTime.now()
        val excelFileStream = resourceLoader.getResource(recommendationsFile).inputStream
        XSSFWorkbook(excelFileStream).use { workbook ->
            updateRecommendations(workbook, importTimestamp)
            updatePrevalence(workbook)
        }
    }

}
