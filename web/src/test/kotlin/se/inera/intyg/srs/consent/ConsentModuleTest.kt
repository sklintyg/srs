package se.inera.intyg.srs.consent

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.slf4j.LoggerFactory
import se.inera.intyg.srs.persistence.entity.Consent
import se.inera.intyg.srs.persistence.repository.ConsentRepository
import se.inera.intyg.srs.vo.ConsentModule
import se.riv.clinicalprocess.healthcond.certificate.types.v2.ResultCodeEnum
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneId
import java.time.ZonedDateTime

class ConsentModuleTest {

    private val log = LoggerFactory.getLogger(javaClass)

    private lateinit var consentModule: ConsentModule
    private lateinit var repo: ConsentRepository

    private val PERSONNUMMER_WITH_CONSENT: String = "191212121212"
    private val PERSONNUMMER_WITHOUT_CONSENT: String = "191010101010"
    private val HSAID: String = "test"
    private val consent = Consent("191111111111", HSAID, LocalDateTime.of(2017, 1, 1, 1, 1, 1), 3)
    private val clock = Clock.systemDefaultZone();

    @BeforeEach
    fun setup() {
        repo = mock()
        consentModule = ConsentModule(repo, clock)
        initData()
    }

    private fun initData() {
        whenever(repo.findConsentByPersonnummerAndVardenhetId(PERSONNUMMER_WITH_CONSENT, HSAID))
                .thenReturn(createConsent(PERSONNUMMER_WITH_CONSENT, HSAID))
        whenever(repo.save(Mockito.any<Consent>())).thenReturn(consent)
    }

    private fun createConsent(personnummer: String, hsaId: String): Consent {
        return Consent(personnummer, hsaId, LocalDateTime.of(2017, 1, 1, 1, 1), 1)
    }

    @Test
    fun testGetConsentExists() {
        val consent = consentModule.getConsent(PERSONNUMMER_WITH_CONSENT, HSAID)
        assertNotNull(consent)
    }

    @Test
    fun testGetConsentNonExisting() {
        val consent = consentModule.getConsent(PERSONNUMMER_WITHOUT_CONSENT, HSAID)
        assertNull(consent)
    }

    @Test
    fun testConsentNeeded() {
        ZonedDateTime.of(2020, 1, 31, 23,59,0,0,ZoneId.systemDefault())
        val skewClock = Clock.fixed(
                ZonedDateTime.of(2020, 1, 31, 23,59,0,0,
                        ZoneId.systemDefault()).toInstant(),
                ZoneId.systemDefault()
        )
        val consentModuleMock = ConsentModule(mock(), skewClock)
        assertTrue(consentModuleMock.consentNeeded())
    }

    @Test
    fun testConsentNotNeeded() {
        val skewClock = Clock.fixed(
                LocalDate.of(2020, Month.FEBRUARY, 1)
                        .atStartOfDay(ZoneId.systemDefault()).toInstant(),
                ZoneId.systemDefault()
        )
        val consentModuleMock = ConsentModule(mock(), skewClock)
        assertFalse(consentModuleMock.consentNeeded())
    }

    @Test
    fun testSetConsent() {
        val personnummer = "191111111111"
        assertNull(consentModule.getConsent(personnummer, HSAID))
        val result = consentModule.setConsent(personnummer, true, HSAID)
        assertNotNull(result)
        assertEquals(ResultCodeEnum.OK, result)
    }

}
