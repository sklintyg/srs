package se.inera.intyg.srs.statistics

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Statistikbild
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Statistikstatus
import se.inera.intyg.srs.persistence.InternalStatistic
import se.inera.intyg.srs.persistence.InternalStatisticRepository
import se.inera.intyg.srs.persistence.NationalStatisticRepository
import se.inera.intyg.srs.service.YOUTHS
import se.inera.intyg.srs.vo.Diagnosis
import se.inera.intyg.srs.vo.Person
import se.inera.intyg.srs.vo.Sex
import se.inera.intyg.srs.vo.StatisticModule
import java.time.LocalDateTime
import java.util.stream.Collectors

class StatisticModuleTest {

    private val DIAGNOSIS_A1234 = "A1234"
    private val DIAGNOSIS_A12 = "A12"
    private val A12_URL = "http://test.se/A12"
    private val B12_URL = "http://test.se/B12"
    private val DIAGNOSIS_B12 = "B12"
    private val aDate = LocalDateTime.of(2017, 1, 1, 1, 1)

    lateinit var nationalStatisticsRepo: NationalStatisticRepository
    lateinit var repo: InternalStatisticRepository
    lateinit var module: StatisticModule

    @Before
    fun setup() {
        repo = mock()
        nationalStatisticsRepo = mock()
        module = StatisticModule(repo, nationalStatisticsRepo)
        initData()
    }

    private fun initData() {
        whenever(repo.findByDiagnosisId(DIAGNOSIS_A12))
                .thenReturn(listOf(InternalStatistic("A12", A12_URL, aDate)))
        whenever(repo.findByDiagnosisId(DIAGNOSIS_B12))
                .thenReturn(listOf(InternalStatistic("B12", B12_URL, aDate)))
    }

    @Test
    fun longerDiagnoseYieldsShortenedVersion() {
        val result = doGetInfo(listOf(DIAGNOSIS_A1234)).first()

        assertEquals(DIAGNOSIS_A12, result.diagnos.code)
        assertEquals(DIAGNOSIS_A1234, result.inkommandediagnos.code)
        assertEquals(A12_URL, result.bildadress)
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
        assertEquals(A12_URL, result.get(0).bildadress)
        assertEquals(Statistikstatus.OK, result.get(0).statistikstatus)

        assertEquals(DIAGNOSIS_B12, result.get(1).diagnos.code)
        assertEquals(B12_URL, result.get(1).bildadress)
        assertEquals(Statistikstatus.OK, result.get(1).statistikstatus)
    }

    private fun doGetInfo(diagnoses: List<String>): List<Statistikbild> {
        val diagnosesList = diagnoses.stream().map { Diagnosis(it) }.collect(Collectors.toList())
        val person = Person("1212121212", YOUTHS, Sex.MAN, diagnosesList, "test1")
        return module.getInfo(listOf(person), mapOf()).get(person)!!
    }

}
