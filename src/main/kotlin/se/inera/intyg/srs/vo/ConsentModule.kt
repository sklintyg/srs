package se.inera.intyg.srs.vo

import org.springframework.stereotype.Service
import se.inera.intyg.srs.persistence.Consent
import se.inera.intyg.srs.persistence.ConsentRepository
import se.riv.clinicalprocess.healthcond.certificate.types.v2.ResultCodeEnum
import java.time.LocalDateTime

@Service
class ConsentModule(private val consentRepo: ConsentRepository) {
    fun getConsent(personnummer: String, vardenhetHsaId: String) : Consent? {
        return consentRepo.findConsentByPersonnummerAndVardenhetId(personnummer, vardenhetHsaId)
    }

    fun setConsent(personnummer: String, samtycke: Boolean, vardenhetHsaId: String): ResultCodeEnum {
        var consent = consentRepo.findConsentByPersonnummerAndVardenhetId(personnummer, vardenhetHsaId)
        if (samtycke) {
            if (consent == null) {
                consent = Consent(personnummer, vardenhetHsaId, LocalDateTime.now())
            }
            // Update consent
            consent.skapatTid = LocalDateTime.now()
            consentRepo.save(consent) ?: return ResultCodeEnum.ERROR
        } else if (consent != null) {
            // If not consent, i.e patient does not consent, remove the db entry.
            consentRepo.delete(consent)
        }
        return ResultCodeEnum.OK
    }
}