package se.inera.intyg.srs.vo

import org.springframework.stereotype.Service
import se.inera.intyg.srs.persistence.Consent
import se.inera.intyg.srs.persistence.ConsentRepository
import se.riv.clinicalprocess.healthcond.certificate.types.v2.ResultCodeEnum
import java.time.LocalDateTime

@Service
class ConsentModule(private val consentRepo: ConsentRepository) {
    fun getConsent(personnummer: String, hsaId: String) : Consent? {
        return consentRepo.findConsentByPersonnummerAndVardgivareId(personnummer, hsaId)
    }

    fun setConsent(personnummer: String, samtycke: Boolean, vardgivarId: String): ResultCodeEnum {
        var consent = consentRepo.findConsentByPersonnummerAndVardgivareId(personnummer, vardgivarId)

        if (consent == null) {
            consent = Consent(personnummer, samtycke, vardgivarId, LocalDateTime.now())
        } else {
            // Update consent
            consent.skapatTid = LocalDateTime.now()
            consent.samtycke = samtycke
        }

        if (consentRepo.save(consent) == null) {
            return ResultCodeEnum.ERROR
        }
        return ResultCodeEnum.OK
    }
}