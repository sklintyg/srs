package se.inera.intyg.srs.service

import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Service
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.*

@Service
class GetSRSInformationResponderImpl : GetSRSInformationResponderInterface {
    private val log = LogManager.getLogger()

    override fun getSRSInformation(request: GetSRSInformationRequestType?): GetSRSInformationResponseType {
        log.info("Received request from ${request?.konsumentId?.extension}")
        val response = GetSRSInformationResponseType()
        response.resultCode = ResultCodeEnum.OK
        val bedomningsUnderlag = Bedomningsunderlag()
        response.bedomningsunderlag.add(bedomningsUnderlag)
        return response
    }

}
