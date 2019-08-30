package se.inera.intyg.srs.statistics

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Diagnosstatistik
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Statistikstatus
import se.inera.intyg.srs.persistence.entity.NationalStatistic
import se.inera.intyg.srs.persistence.repository.NationalStatisticRepository
import se.inera.intyg.srs.service.YOUTHS
import se.inera.intyg.srs.vo.Diagnosis
import se.inera.intyg.srs.vo.Person
import se.inera.intyg.srs.vo.Sex
import se.inera.intyg.srs.vo.StatisticModule
import java.math.BigInteger
import java.time.LocalDateTime
import java.util.stream.Collectors

class StatisticModuleTest {

    private val DIAGNOSIS_A1234 = "A1234"
    private val DIAGNOSIS_A12 = "A12"
    private val DIAGNOSIS_B12 = "B12"
    private val aDate = LocalDateTime.of(2017, 1, 1, 1, 1)

    lateinit var nationalStatisticsRepo: NationalStatisticRepository
    lateinit var module: StatisticModule

    @BeforeEach
    fun setup() {
        nationalStatisticsRepo = mock()
        module = StatisticModule(nationalStatisticsRepo)
        initData()
    }

    private fun initData() {
        whenever(nationalStatisticsRepo.findByDiagnosisIdOrderByDayIntervalMaxExcl(DIAGNOSIS_A12))
                .thenReturn(listOf(NationalStatistic("A12", 0, 30, 100,
                        100, LocalDateTime.now())))
        whenever(nationalStatisticsRepo.findByDiagnosisIdOrderByDayIntervalMaxExcl(DIAGNOSIS_B12))
                .thenReturn(listOf(NationalStatistic("B12", 0, 30, 150,
                        150, LocalDateTime.now())))
    }

    @Test
    fun longerDiagnoseYieldsShortenedVersion() {
        val result = doGetInfo(listOf(DIAGNOSIS_A1234)).first()

        assertEquals(DIAGNOSIS_A12, result.diagnos.code)
        assertEquals(DIAGNOSIS_A1234, result.inkommandediagnos.code)
        assertEquals(BigInteger.valueOf(100), result.data[0].individer)
        assertEquals(Statistikstatus.DIAGNOSKOD_PA_HOGRE_NIVA, result.statistikstatus)

    }

    @Test
    fun nonExistantDiagnosisGivesCorrectStatus() {
        val nonExistant = "NOPE"
        val result = doGetInfo(listOf(nonExistant)).first()

        assertEquals(Statistikstatus.STATISTIK_SAKNAS, result.statistikstatus)
        assertEquals(nonExistant, result.inkommandediagnos.code)
    }

    @Test
    fun multipleDiagnoses() {
        val result = doGetInfo(listOf(DIAGNOSIS_A12, DIAGNOSIS_B12))

        assertEquals("Wrong number of entries in result", 2, result.size)
        assertEquals(DIAGNOSIS_A12, result.get(0).diagnos.code)
        assertEquals(BigInteger.valueOf(100), result.get(0).data[0].individer)
        assertEquals(Statistikstatus.OK, result.get(0).statistikstatus)

        assertEquals(DIAGNOSIS_B12, result.get(1).diagnos.code)
        assertEquals(BigInteger.valueOf(150), result.get(1).data[0].individer)
        assertEquals(Statistikstatus.OK, result.get(1).statistikstatus)
    }

    private fun doGetInfo(diagnoses: List<String>): List<Diagnosstatistik> {
        val diagnosesList = diagnoses.stream().map { Diagnosis(it) }.collect(Collectors.toList())
        val person = Person("1212121212", YOUTHS, Sex.MAN, diagnosesList, "test1")
        return module.getInfo(listOf(person), mapOf()).get(person)!!
    }

}
