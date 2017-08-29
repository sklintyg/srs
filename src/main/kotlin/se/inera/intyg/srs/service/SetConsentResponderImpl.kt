package se.inera.intyg.srs.service

import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import se.inera.intyg.clinicalprocess.healthcond.srs.setconsent.v1.SetConsentRequestType
import se.inera.intyg.clinicalprocess.healthcond.srs.setconsent.v1.SetConsentResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.setconsent.v1.SetConsentResponseType
import se.inera.intyg.srs.vo.ConsentModule

@Service
class SetConsentResponderImpl(@Autowired val consentModule: ConsentModule): SetConsentResponderInterface {

    private val log = LogManager.getLogger()

    override fun setConsent(request: SetConsentRequestType): SetConsentResponseType {
        log.info("Set consent request received")
        val response = SetConsentResponseType()
        val result = consentModule.setConsent(request.personId, request.isSamtycke, request.vardgivareId.extension)
        response.resultCode = result
        return response
    }
}