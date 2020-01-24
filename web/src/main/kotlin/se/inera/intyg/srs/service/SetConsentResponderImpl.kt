package se.inera.intyg.srs.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import se.inera.intyg.clinicalprocess.healthcond.srs.setconsent.v1.SetConsentRequestType
import se.inera.intyg.clinicalprocess.healthcond.srs.setconsent.v1.SetConsentResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.setconsent.v1.SetConsentResponseType
import se.inera.intyg.srs.vo.ConsentModule

@Service
class SetConsentResponderImpl(val consentModule: ConsentModule) : SetConsentResponderInterface {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun setConsent(request: SetConsentRequestType): SetConsentResponseType {
        log.debug("Set consent request received")
        val response = SetConsentResponseType()
        val result = consentModule.setConsent(request.personId, request.isSamtycke, request.vardenhetId.extension)
        response.resultCode = result
        return response
    }
}