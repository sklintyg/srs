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
import se.inera.intyg.srs.persistence.InternalStatistic
import se.inera.intyg.srs.persistence.InternalStatisticRepository
import se.inera.intyg.srs.persistence.NationalStatistic
import se.inera.intyg.srs.persistence.NationalStatisticRepository
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
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
 * Scheduled service for checking for new statistics images in the dir specified with statistics.image.dir,
 * the update interval is also configurable via image.update.cron, both in application.properties.
 */
@Component
@Profile("runtime")
class StatisticsFileUpdateService(@Value("\${statistics.image.location-pattern}") val imageLocationPattern: String,
                                  @Value("\${statistics.national.file}") val nationalStatisticsFile: String,
                                  @Value("\${base.url}") val baseUrl: String,
                                  val internalStatisticRepo: InternalStatisticRepository,
                                  val nationalStatisticsFileRepo: NationalStatisticRepository,
                                  val resourceLoader: ResourceLoader) {

    private val log = LogManager.getLogger()

    private val imageFileExtension: String = "jpg"

    private val urlExtension: String = "/image/"

    // For now, only codes with three positions are allowed.
    private val fileNameRegex = Regex("""\w\d{2}""")

    init {
        doUpdate()
    }

    private final fun doUpdate() {
        doUpdateNationalStatistics()
        doUpdateImages()
    }

    private final fun logDataRow(currentRow: Row) {
        val cellsInRow = currentRow.iterator()
        while (cellsInRow.hasNext()) {
            val currentCell = cellsInRow.next()
            if (currentCell.getCellType() === CellType.STRING) {
                log.info(currentCell.getStringCellValue() + " | ")
            } else if (currentCell.getCellType() === CellType.NUMERIC) {
                log.info(currentCell.getNumericCellValue().toString() + "(numeric)")
            }
        }
    }

    private final fun getDiagnosisTitles(currentRow: Row): LinkedHashMap<String, MutableMap<Pair<Int, Int>, Int>> {
        val diagnosisMap = LinkedHashMap<String, MutableMap<Pair<Int, Int>, Int>>()
        val cellsInRow = currentRow.iterator()
        while (cellsInRow.hasNext()) {
            val currentCell = cellsInRow.next()
            if (currentCell.getCellType() === CellType.STRING) {
                val value = currentCell.getStringCellValue()
                if (!StringUtils.isEmpty(value)) {
                    diagnosisMap.put(value, mutableMapOf())
                    log.debug("Found diagnosis title $value")
                }
            }
        }
        return diagnosisMap
    }

    private final fun daysToInt(days: String): Pair<Int, Int> = when {
        days.contains("15-29") -> Pair(15, 29)
        days.contains("30-89") -> Pair(30, 89)
        days.contains("90-179") -> Pair(90, 179)
        days.contains("180-365") -> Pair(180, 365)
        days.contains("366") -> Pair(366, Int.MAX_VALUE)
        else -> throw IllegalArgumentException("Incorrect day interval field in input data file for National Statistics, days interval: $days")
    }

    private final fun fillMapWithRowData(diagnosisMap: LinkedHashMap<String, MutableMap<Pair<Int, Int>, Int>>, rowNum: Int, sheet: XSSFSheet) {
        val row = sheet.getRow(rowNum)
        val days = row.getCell(0).stringCellValue
        var cellColumn = 1
        diagnosisMap.forEach { _, diagnosisDaysQtyMap ->
            val cell = row.getCell(cellColumn)
            val quantity = cell.numericCellValue
            diagnosisDaysQtyMap.put(daysToInt(days), quantity.roundToInt())
            cellColumn++
        }
    }

    private final fun doUpdateNationalStatistics() {
        log.info("Performing update of national statistics...")
        log.info("Importing from $nationalStatisticsFile")
        val fileModified = LocalDateTime.now()
        val excelFileStream = resourceLoader.getResource(nationalStatisticsFile).inputStream
        excelFileStream.use {
            val workbook = XSSFWorkbook(excelFileStream)
            workbook.use {
                val sheet = workbook.getSheet("Antal fall")

                // Titles are on row number 3
                var row = sheet.getRow(3)
                val diagnosisMap = getDiagnosisTitles(row)

                // Values per diagnosis are on rows 4 to 8
                for (rowNum in 4..8) {
                    fillMapWithRowData(diagnosisMap, rowNum, sheet)
                }
                diagnosisMap.forEach { diagnosisId, diagnosisDaysQtyMap ->
                    diagnosisDaysQtyMap.forEach { days, qty ->
                        var accumulatedQtyForDiagUntilDays = 0
                        for (daysIt in arrayOf(Pair(15, 29), Pair(30, 89), Pair(90, 179), Pair(180, 365), Pair(366, Int.MAX_VALUE))) {
                            if (daysIt.second <= days.second) {
                                accumulatedQtyForDiagUntilDays = accumulatedQtyForDiagUntilDays + (diagnosisDaysQtyMap.get(daysIt)!!)
                            }
                        }
                        log.debug("Creating or Updating National statistic diagnosisId: {}, days interval: {} with qty: {}", diagnosisId, days, qty)
                        var statEntryOpt = nationalStatisticsFileRepo.findOneByDiagnosisIdAndDayIntervalMaxExcl(diagnosisId, days.second)
                        if (!statEntryOpt.isPresent()) {
                            log.info("Creating National statistic diagnosisId: {}, days interval: {} with qty: {}", diagnosisId, days, qty)
                            var statEntry = NationalStatistic(diagnosisId, days.first, days.second, qty, accumulatedQtyForDiagUntilDays, fileModified)
                            nationalStatisticsFileRepo.save(statEntry)
                        } else if (statEntryOpt.get().intervalQuantity != qty
                                || statEntryOpt.get().accumulatedQuantity != accumulatedQtyForDiagUntilDays) {
                            log.info("Updating National statistic diagnosisId: {}, days interval: {} with qty: {}", diagnosisId, days, qty)
                            var statEntry = statEntryOpt.get()
                            statEntry.intervalQuantity = qty
                            statEntry.accumulatedQuantity = accumulatedQtyForDiagUntilDays
                            statEntry.timestamp = fileModified
                            nationalStatisticsFileRepo.save(statEntry)
                        }
                    }
                }
                log.debug("Imported national statistics: {}", diagnosisMap)
            }
        }
    }

    private final fun doUpdateImages() {
        log.info("Performing image update... using imageLocationPattern: $imageLocationPattern")

        val dbEntries: List<InternalStatistic> = internalStatisticRepo.findAll().toList()
        val imageResourceEntries = ArrayList<String>()

        ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(imageLocationPattern)
                .mapNotNull { Pair(it.filename, it.lastModified()) }
                .filter { (fileName, _) -> isRequiredFileType(fileName) && isVaildFileName(fileName) }
                .map { (fileName, lastModified) -> Pair(fixFileName(fileName), lastModified) }
                .forEach { (fileName, lastModified) ->
                    val existingImage = dbEntries.find { cleanDiagnosisCode(it.diagnosisId) == fileName }
                    val imageLastModifiedTime = Instant.ofEpochMilli(lastModified).atZone(ZoneId.systemDefault()).toLocalDateTime()
                    if (existingImage == null) {
                        log.info("New file found, saving as: $fileName")
                        internalStatisticRepo.save(InternalStatistic(fileName, buildUrl(fileName), imageLastModifiedTime))
                    } else if (!existingImage.timestamp.equals(imageLastModifiedTime)) {
                        log.info("Existing but modified file found, updating $fileName")
                        existingImage.timestamp = imageLastModifiedTime
                        internalStatisticRepo.save(existingImage)
                    }

                    imageResourceEntries.add(fileName)
                }

        // Cleanup removed files
        if (imageResourceEntries.size != dbEntries.size) {
            dbEntries.filter { it.diagnosisId !in imageResourceEntries }.forEach {
                log.info("Statistics image for ${it.diagnosisId} no longer available, removing from database.")
                internalStatisticRepo.deleteById(it.id)
            }
        }
    }

    private fun fixFileName(fileName: String): String = fileName.dropLast(4)

    private fun isVaildFileName(fileName: String): Boolean = fileNameRegex.matches(fixFileName(fileName))

    private fun buildUrl(fileName: String): String = "$baseUrl$urlExtension$fileName"

    private fun cleanDiagnosisCode(diagnosisId: String): String = diagnosisId.toUpperCase(Locale.ENGLISH).replace(".", "")

    private fun isRequiredFileType(fileName: String): Boolean {
        return fileName.toLowerCase().endsWith(imageFileExtension)
    }

}
