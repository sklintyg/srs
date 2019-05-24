package se.inera.intyg.srs.service

import org.apache.logging.log4j.LogManager
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
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
@Profile("runtime")
class RecommendationsFileUpdateService(@Value("\${recommendations.file}") val recommendationsFile: String,
                                       val recommendationsRepo: RecommendationRepository,
                                       val measureRepo: MeasureRepository,
                                       val measurePriorityRepo: MeasurePriorityRepository,
                                       val prioRepo: MeasurePriorityRepository,
                                       val resourceLoader: ResourceLoader) {

    private val log = LogManager.getLogger()

    init {
        doUpdate()
    }

    private final fun doUpdate() {
        doRemoveOldRecommendations()
        doUpdateRecommendations()
    }

    private final fun doRemoveOldRecommendations() {
        measurePriorityRepo.deleteAll()
        recommendationsRepo.deleteAll()
        measureRepo.deleteAll()
    }

    private final fun doUpdateRecommendations() {
        log.info("Performing update of recommendations from file $recommendationsFile")
        val importTimestamp = LocalDateTime.now()
        val excelFileStream = resourceLoader.getResource(recommendationsFile).inputStream
        XSSFWorkbook(excelFileStream).use { workbook ->
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
                    val recommendationId = row.getCell(1).numericCellValue.toLong()

                    val category = row.getCell(2).stringCellValue
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
                if (rowNumber > 500) {
                    readMoreRows = false
                }
            }
        }
    }

}
