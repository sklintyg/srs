package se.inera.intyg.srs.consent

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import se.inera.intyg.srs.persistence.entity.Consent
import se.inera.intyg.srs.persistence.repository.ConsentRepository
import se.inera.intyg.srs.vo.ConsentModule
import se.riv.clinicalprocess.healthcond.certificate.types.v2.ResultCodeEnum
import java.time.LocalDateTime

class ConsentModuleTest {

    private lateinit var consentModule: ConsentModule
    private lateinit var repo: ConsentRepository

    private val PERSONNUMMER_WITH_CONSENT: String = "191212121212"
    private val PERSONNUMMER_WITHOUT_CONSENT: String = "191010101010"
    private val HSAID: String = "test"
    private val consent = Consent("191111111111", HSAID, LocalDateTime.of(2017, 1, 1, 1, 1, 1), 3)

    @BeforeEach
    fun setup() {
        repo = mock()
        consentModule = ConsentModule(repo)
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
    fun testSetConsent() {
        val personnummer = "191111111111"
        assertNull(consentModule.getConsent(personnummer, HSAID))
        val result = consentModule.setConsent(personnummer, true, HSAID)
        assertNotNull(result)
        assertEquals(ResultCodeEnum.OK, result)
    }

}
