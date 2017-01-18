package se.inera.intyg.srs.service

import org.springframework.stereotype.Service
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.GetSRSInformationRequestType
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.GetSRSInformationResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.GetSRSInformationResponseType

@Service
class GetSRSInformationResponderImpl : GetSRSInformationResponderInterface {

    override fun getSRSInformation(request: GetSRSInformationRequestType?): GetSRSInformationResponseType {
        TODO("not implemented")
    }

}