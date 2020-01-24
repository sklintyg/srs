package se.inera.intyg.srs.service

import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import se.inera.intyg.srs.persistence.entity.NationalStatistic
import se.inera.intyg.srs.persistence.repository.NationalStatisticRepository
import java.time.LocalDateTime
import kotlin.math.roundToInt

/**
 * Service for updating national statistics from file.
 */
@Component
class StatisticsFileUpdateService(@Value("\${statistics.national.file}") val nationalStatisticsFile: String,
                                  val nationalStatisticsFileRepo: NationalStatisticRepository,
                                  val resourceLoader: ResourceLoader) {

    private val log = LoggerFactory.getLogger(javaClass)

    final fun doUpdate() {
        doRemoveOldStatistics()
        doUpdateNationalStatistics()
    }

    private final fun doRemoveOldStatistics() {
        nationalStatisticsFileRepo.deleteAll()
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
}
