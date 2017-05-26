package se.inera.intyg.srs.measures

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import se.inera.intyg.srs.persistence.Measure
import se.inera.intyg.srs.persistence.MeasureRepository
import se.inera.intyg.srs.persistence.Priority
import se.inera.intyg.srs.persistence.Recommendation
import se.inera.intyg.srs.vo.Diagnose
import se.inera.intyg.srs.vo.Extent
import se.inera.intyg.srs.vo.MeasureInformationModule
import se.inera.intyg.srs.vo.Person
import se.inera.intyg.srs.vo.Sex

class MeasureInformationModuleTest {

    lateinit var measureRepo: MeasureRepository

    lateinit var module: MeasureInformationModule

    @Before
    fun setUp() {
        measureRepo = mock()
        module = MeasureInformationModule()
        module.measureRepo = measureRepo
        insertMeasureData()
    }

    fun insertMeasureData() {
        whenever(measureRepo.findByDiagnoseIdStartingWith("A12")).thenReturn(listOf(Measure("A12", "Depression", "1.0", listOf((Priority(1, Recommendation("Softa")))))))
    }

    private val DIAGNOSE1 = "A1234"

    @Test
    fun firstTest() {
        val person: Person = Person("1212121212", 35, Sex.MAN, Extent.HELT_NEDSATT, listOf(Diagnose(DIAGNOSE1)))

        val result = module.getInfo(listOf(person))
        assertEquals("A12", result.get(person)!!.rekommendation.get(0).diagnos.code)
    }

}
