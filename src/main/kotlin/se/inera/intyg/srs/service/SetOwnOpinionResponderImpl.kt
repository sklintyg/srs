package se.inera.intyg.srs.service

import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Service
import se.inera.intyg.clinicalprocess.healthcond.srs.setownopinion.v1.SetOwnOpinionRequestType
import se.inera.intyg.clinicalprocess.healthcond.srs.setownopinion.v1.SetOwnOpinionResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.setownopinion.v1.SetOwnOpinionResponseType
import se.inera.intyg.srs.persistence.ProbabilityRepository
import se.inera.intyg.srs.vo.OwnOpinionModule

@Service
class SetOwnOpinionResponderImpl(val ownOpinionModule: OwnOpinionModule,
                                 val probabilityRepo: ProbabilityRepository) : SetOwnOpinionResponderInterface {

    private val log = LogManager.getLogger()

    override fun setOwnOpinion(request: SetOwnOpinionRequestType): SetOwnOpinionResponseType {
        log.info("Set own opinion request received")
        val probability = probabilityRepo.findFirstByCertificateIdAndDiagnosisOrderByTimestampDesc(request.intygId.extension, request.diagnos.code)
        if (probability == null) {
            throw RuntimeException("Found no stored probability to store the opinion on")
        }
        val response = SetOwnOpinionResponseType()
        val result = ownOpinionModule.setOwnOpinion(request.vardgivareId.extension,
                request.vardenhetId.extension,
                probability,
                request.egenBedomningRisk.value())
        response.resultCode = result
        return response
    }
}