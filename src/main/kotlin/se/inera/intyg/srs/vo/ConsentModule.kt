package se.inera.intyg.srs.vo

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import se.inera.intyg.srs.persistence.Consent
import se.inera.intyg.srs.persistence.ConsentRepository
import se.riv.clinicalprocess.healthcond.certificate.types.v2.ResultCodeEnum
import java.time.LocalDateTime

@Service
class ConsentModule(@Autowired private val consentRepo: ConsentRepository) {
    fun getConsent(personnummer: String, hsaId: String) : Consent? {
        return consentRepo.findConsentByPersonnummerAndVardgivareId(personnummer, hsaId)
    }

    fun setConsent(personnummer: String, samtycke: Boolean, vardenhetId: String): ResultCodeEnum {
        val consent = Consent(personnummer, samtycke, vardenhetId, LocalDateTime.now())
        if (consentRepo.save(consent) == null) {
            return ResultCodeEnum.ERROR
        }
        return ResultCodeEnum.OK
    }
}