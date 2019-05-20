package se.inera.intyg.srs.service

import org.apache.logging.log4j.LogManager
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Atgardstyp
import se.inera.intyg.srs.persistence.InternalStatistic
import se.inera.intyg.srs.persistence.InternalStatisticRepository
import se.inera.intyg.srs.persistence.Measure
import se.inera.intyg.srs.persistence.MeasurePriority
import se.inera.intyg.srs.persistence.MeasurePriorityRepository
import se.inera.intyg.srs.persistence.MeasureRepository
import se.inera.intyg.srs.persistence.NationalStatistic
import se.inera.intyg.srs.persistence.NationalStatisticRepository
import se.inera.intyg.srs.persistence.Recommendation
import se.inera.intyg.srs.persistence.RecommendationRepository
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Locale
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap
import kotlin.collections.List
import kotlin.collections.MutableMap
import kotlin.collections.filter
import kotlin.collections.find
import kotlin.collections.forEach
import kotlin.collections.map
import kotlin.collections.mapNotNull
import kotlin.collections.mutableMapOf
import kotlin.collections.toList
import kotlin.math.roundToInt

/**
 * Updates the recommendations from file
 */
@Component
class RecommendationsFileUpdateService(@Value("\${recommendations.file}") val recommendationsFile: String,
                                       val recommendationsRepo: RecommendationRepository,
                                       val measureRepo: MeasureRepository,
                                       val prioRepo: MeasurePriorityRepository,
                                       val resourceLoader: ResourceLoader) {

    private val log = LogManager.getLogger()

    init {
//        doUpdate()
    }

    private final fun doUpdate() {
        doUpdateRecommendations()
    }

    private final fun doUpdateRecommendations() {
        log.info("Performing update of recommendations from file $recommendationsFile")
        val fileModified = LocalDateTime.now()
        val excelFileStream = resourceLoader.getResource(recommendationsFile).inputStream
        XSSFWorkbook(excelFileStream).use { workbook ->
            val sheet = workbook.getSheet("Per diagnos")
            var readMoreRows = true
            var rowNumber = 1
            var unimportableRows = 0
            while (readMoreRows && unimportableRows < 4) {
                val row = sheet.getRow(rowNumber) ?: break
                val diagnosisId = row.getCell(0).stringCellValue.replace(".","")
                val diagnosisText = row.getCell(1).stringCellValue
                val recommendationId = row.getCell(2).numericCellValue.toLong()
                val category = row.getCell(3).stringCellValue
                val priority = row.getCell(4).numericCellValue.toInt()
                val title = row.getCell(7).stringCellValue
                val text = row.getCell(8).stringCellValue

                if (!diagnosisId.isNullOrBlank() && !diagnosisText.isNullOrBlank() && !category.isNullOrBlank()) {
                    log.debug("Found recommendation in import file {}, {}, {}, {}, {}, {}, {},",
                            diagnosisId, diagnosisText, recommendationId, category, priority, text)

//                    val existingMeasure = measureRepo.findByDiagnosisId(diagnosisId)
//
//                    val existingRecommendation = recommendationsRepo.(recommendationId)

                    recommendationsRepo.findById(recommendationId).map { existingRecommendation ->
                        recommendationsRepo.save(existingRecommendation.copy(
                                recommendationTitle = title, recommendationText = text))
                    }.orElse(
                        recommendationsRepo.save(Recommendation(recommendationId, Atgardstyp.fromValue(category), title, text))
                    )
//
//                    if (existingRecommendation.isPresent) {
//                        existingRecommendation = recommendationsRepo.save(
//                                existingRecommendation.get().copy(
//                                        recommendationText = text
//                                )
//                        )
//                    } else {
//
//                    }
//                    existingRecommendation.ifPresent(())
//
//                    measureRepo.save(Measure(1, "F438A", "Utmattningssyndrom", "1.0",
//                            listOf(prioRepo.save(MeasurePriority(1, recommendation01)),
//
//                    if (existingMeasure != null) {
//                        // Update the measure
////                        existingMeasure.diagnosisId = diagnosisId
//                        existingMeasure.diagnosisText = diagnosisText
////                        existingMeasure.version = existingMeasure.version+1
//
//                    } else {
//                        val measure = Measure(diagnosisId = diagnosisId, diagnosisText = diagnosisText)
//                    }
////                    importedApartments.add(a)
                } else {
                    unimportableRows++
                }
                rowNumber++
                if (rowNumber > 500) {
                    readMoreRows = false
                }
            }
        }
    }

}
