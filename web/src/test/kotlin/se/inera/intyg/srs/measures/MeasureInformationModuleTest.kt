package se.inera.intyg.srs.measures

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Atgardsrekommendationstatus.*
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Atgardstyp
import se.inera.intyg.srs.persistence.entity.Measure
import se.inera.intyg.srs.persistence.entity.MeasurePriority
import se.inera.intyg.srs.persistence.repository.MeasureRepository
import se.inera.intyg.srs.persistence.entity.Recommendation
import se.inera.intyg.srs.service.AT_DEATHS_DOOR
import se.inera.intyg.srs.service.YOUTHS
import se.inera.intyg.srs.vo.*

class MeasureInformationModuleTest {

    private val DIAGNOSIS_A1234 = "A1234"
    private val DIAGNOSIS_A12 = "A12"
    private val DIAGNOSIS_B12 = "B12"

    lateinit var measureRepo: MeasureRepository

    lateinit var module: MeasureInformationModule

    @BeforeEach
    fun setUp() {
        measureRepo = mock()
        module = MeasureInformationModule(measureRepo)
        insertMeasureData()
    }

    fun insertMeasureData() {
        whenever(measureRepo.findByDiagnosisIdStartingWith(DIAGNOSIS_A12)).thenReturn(
                listOf(
                        Measure(DIAGNOSIS_A12, "Depression", "1.0",
                                listOf(
                                        MeasurePriority(1,
                                                Recommendation(Atgardstyp.REK, "Chilla", "Softa", 1)
                                        )
                                ), id = 1
                        )
                )
        )
        whenever(measureRepo.findByDiagnosisIdStartingWith(DIAGNOSIS_B12)).thenReturn(
                listOf(
                        Measure(DIAGNOSIS_B12, "Benbrott", "1.0",
                                listOf(
                                        MeasurePriority(1,
                                                Recommendation(Atgardstyp.OBS, "Linka",
                                                        "Hoppa på ett ben", 2)
                                        )
                                ), id = 2
                        )
                )
        )
    }

    @Test
    fun nonExistingDiagnosisIsReportedAndTheInputDiagnosisIsReusedInOutput() {
        val nonExisting = "Z123"
        val person = Person("1212121212", YOUTHS, Sex.MAN, listOf(CertDiagnosis("test1",nonExisting)))
        val result = module.getInfo(listOf(person), mapOf())
        assertEquals(nonExisting, result.get(person)!![0].inkommandediagnos.code)
        assertEquals(INFORMATION_SAKNAS, result.get(person)!![0].atgardsrekommendationstatus)
    }

    @Test
    fun diagnosisCodeIsShortenedUntilItMatches() {
        val person = Person("1212121212", YOUTHS, Sex.MAN, listOf(CertDiagnosis("test1", DIAGNOSIS_A1234)))
        val result = module.getInfo(listOf(person), mapOf())
        assertEquals(DIAGNOSIS_A12, result.get(person)!![0].diagnos.code)
        assertEquals(DIAGNOSIS_A1234, result.get(person)!![0].inkommandediagnos.code)
        assertEquals(DIAGNOSKOD_PA_HOGRE_NIVA, result.get(person)!![0].atgardsrekommendationstatus)
    }

    @Test
    fun measureShouldBeReturnedForFirstDiagnosisPerPersonOnlyOnePerPerson() {
        val person = Person("1212121212", YOUTHS, Sex.MAN,
            listOf(CertDiagnosis("test1", DIAGNOSIS_A12), CertDiagnosis("test1", DIAGNOSIS_B12)))
        val result = module.getInfo(listOf(person), mapOf())
        assertEquals(1, result.get(person)!!.size)
        assertEquals(DIAGNOSIS_A12, result.get(person)!![0].diagnos.code)
        assertEquals(1, result.get(person)!![0].atgard.size)
        assertEquals(Atgardstyp.REK, result.get(person)!![0].atgard.get(0).atgardstyp)
        assertEquals(OK, result.get(person)!![0].atgardsrekommendationstatus) }

    @Test
    fun measureShouldBeReturnedForFirstDiagnosisPerPerson() {
        val person = Person("1212121212", YOUTHS, Sex.MAN,
            listOf(CertDiagnosis("test1", DIAGNOSIS_A12)))
        val person2 = Person("191212121212", AT_DEATHS_DOOR, Sex.MAN,
            listOf(CertDiagnosis("test2", DIAGNOSIS_B12)))
        val result = module.getInfo(listOf(person, person2), mapOf())

        assertEquals(1, result.get(person)!!.size)
        assertEquals(DIAGNOSIS_A12, result.get(person)!![0].diagnos.code)
        assertEquals(1, result.get(person)!![0].atgard.size)
        assertEquals(Atgardstyp.REK, result.get(person)!![0].atgard.get(0).atgardstyp)
        assertEquals(OK, result.get(person)!![0].atgardsrekommendationstatus)

        assertEquals(1, result.get(person2)!!.size)
        assertEquals(DIAGNOSIS_B12, result.get(person2)!![0].diagnos.code)
        assertEquals(1, result.get(person2)!![0].atgard.size)
        assertEquals(Atgardstyp.OBS, result.get(person2)!![0].atgard.get(0).atgardstyp)
        assertEquals(OK, result.get(person2)!![0].atgardsrekommendationstatus)
    }

}
