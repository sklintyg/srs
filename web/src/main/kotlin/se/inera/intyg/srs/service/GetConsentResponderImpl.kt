package se.inera.intyg.srs.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import se.inera.intyg.clinicalprocess.healthcond.srs.getconsent.v1.GetConsentRequestType
import se.inera.intyg.clinicalprocess.healthcond.srs.getconsent.v1.GetConsentResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.getconsent.v1.GetConsentResponseType
import se.inera.intyg.clinicalprocess.healthcond.srs.getconsent.v1.Samtyckesstatus
import se.inera.intyg.srs.vo.ConsentModule

@Service
class GetConsentResponderImpl(val consentModule: ConsentModule) : GetConsentResponderInterface {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun getConsent(request: GetConsentRequestType): GetConsentResponseType {
        log.debug("Get consent request received for care unit hsaId: ${request.vardenhetId.extension}")
        val response = GetConsentResponseType()
        val consent = consentModule.getConsent(request.personId, request.vardenhetId.extension)

        if (consent == null) {
            log.debug("No consent found, setting status INGET")
            response.samtyckesstatus = Samtyckesstatus.INGET
        } else {
            response.samtyckesstatus = Samtyckesstatus.JA
            response.isSamtycke = true
            response.sparattidpunkt = consent.skapatTid
        }

        return response
    }

}
