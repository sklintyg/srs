package se.inera.intyg.srs.service

import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import se.inera.intyg.clinicalprocess.healthcond.srs.getconsent.v1.GetConsentRequestType
import se.inera.intyg.clinicalprocess.healthcond.srs.getconsent.v1.GetConsentResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.getconsent.v1.GetConsentResponseType
import se.inera.intyg.clinicalprocess.healthcond.srs.getconsent.v1.Samtyckesstatus
import se.inera.intyg.srs.persistence.Consent

import se.inera.intyg.srs.vo.ConsentModule
import java.time.LocalDateTime

@Service
class GetConsentResponderImpl(@Autowired val consentModule : ConsentModule) : GetConsentResponderInterface{

    private val log = LogManager.getLogger()

    override fun getConsent(request: GetConsentRequestType): GetConsentResponseType {
        log.info("Get consent request received for hsaId: ${request.vardgivareId.extension}")
        val response = GetConsentResponseType()
        var consent = consentModule.getConsent(request.personId)

        if (consent == null) {
            log.info("No consent found, setting status INGET")
            consent = Consent(request.personId, false, request.vardgivareId.extension, LocalDateTime.now())
            response.samtyckesstatus = Samtyckesstatus.INGET
        } else {
            response.samtyckesstatus = if (consent.samtycke) Samtyckesstatus.JA else Samtyckesstatus.NEJ
        }
        response.isSamtycke = consent.samtycke
        response.sparattidpunkt = consent.skapatTid

        return response
    }

}