package se.inera.intyg.srs.measures

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Atgardsrekommendationstatus.DIAGNOSKOD_PA_HOGRE_NIVA
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Atgardsrekommendationstatus.INFORMATION_SAKNAS
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Atgardsrekommendationstatus.OK
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Atgardstyp
import se.inera.intyg.srs.persistence.Measure
import se.inera.intyg.srs.persistence.MeasurePriority
import se.inera.intyg.srs.persistence.MeasureRepository
import se.inera.intyg.srs.persistence.Recommendation
import se.inera.intyg.srs.service.YOUTHS
import se.inera.intyg.srs.vo.Diagnosis
import se.inera.intyg.srs.vo.MeasureInformationModule
import se.inera.intyg.srs.vo.Person
import se.inera.intyg.srs.vo.Sex

class MeasureInformationModuleTest {

    private val DIAGNOSIS_A1234 = "A1234"
    private val DIAGNOSIS_A12 = "A12"
    private val DIAGNOSIS_B12 = "B12"

    lateinit var measureRepo: MeasureRepository

    lateinit var module: MeasureInformationModule

    @Before
    fun setUp() {
        measureRepo = mock()
        module = MeasureInformationModule(measureRepo)
        insertMeasureData()
    }

    fun insertMeasureData() {
        whenever(measureRepo.findByDiagnosisIdStartingWith(DIAGNOSIS_A12)).thenReturn(listOf(Measure(1, DIAGNOSIS_A12, "Depression", "1.0",
                listOf(MeasurePriority(1, Recommendation(1, Atgardstyp.REK, "Softa"))))))
        whenever(measureRepo.findByDiagnosisIdStartingWith(DIAGNOSIS_B12)).thenReturn(listOf(Measure(2, DIAGNOSIS_B12, "Benbrott", "1.0",
                listOf(MeasurePriority(1, Recommendation(2, Atgardstyp.OBS, "Hoppa på ett ben"))))))
    }

    @Test
    fun nonExistingDiagnosisIsReportedAndTheInputDiagnosisIsReusedInOutput() {
        val nonExisting = "Z123"
        val person = Person("1212121212", YOUTHS, Sex.MAN, listOf(Diagnosis(nonExisting)), "test1")
        val result = module.getInfo(listOf(person), mapOf())
        assertEquals(nonExisting, result.get(person)!!.rekommendation.get(0).inkommandediagnos.code)
        assertEquals(INFORMATION_SAKNAS, result.get(person)!!.rekommendation.get(0).atgardsrekommendationstatus)
    }

    @Test
    fun diagnosisCodeIsShortenedUntilItMatches() {
        val person = Person("1212121212", YOUTHS, Sex.MAN, listOf(Diagnosis(DIAGNOSIS_A1234)), "test1")
        val result = module.getInfo(listOf(person), mapOf())
        assertEquals(DIAGNOSIS_A12, result.get(person)!!.rekommendation.get(0).diagnos.code)
        assertEquals(DIAGNOSIS_A1234, result.get(person)!!.rekommendation.get(0).inkommandediagnos.code)
        assertEquals(DIAGNOSKOD_PA_HOGRE_NIVA, result.get(person)!!.rekommendation.get(0).atgardsrekommendationstatus)
    }

    @Test
    fun measureShouldBeReturnedForEachMatchingDiagnosis() {
        val person = Person("1212121212", YOUTHS, Sex.MAN, listOf(Diagnosis(DIAGNOSIS_A12), Diagnosis(DIAGNOSIS_B12)), "test1")
        val result = module.getInfo(listOf(person), mapOf())
        assertEquals(2, result.get(person)!!.rekommendation.size)
        assertEquals(DIAGNOSIS_A12, result.get(person)!!.rekommendation.get(0).diagnos.code)
        assertEquals(1, result.get(person)!!.rekommendation.get(0).atgard.size)
        assertEquals(Atgardstyp.REK, result.get(person)!!.rekommendation.get(0).atgard.get(0).atgardstyp)
        assertEquals(OK, result.get(person)!!.rekommendation.get(0).atgardsrekommendationstatus)
        assertEquals(DIAGNOSIS_B12, result.get(person)!!.rekommendation.get(1).diagnos.code)
        assertEquals(1, result.get(person)!!.rekommendation.get(1).atgard.size)
        assertEquals(Atgardstyp.OBS, result.get(person)!!.rekommendation.get(1).atgard.get(0).atgardstyp)
        assertEquals(OK, result.get(person)!!.rekommendation.get(1).atgardsrekommendationstatus)
    }

}
