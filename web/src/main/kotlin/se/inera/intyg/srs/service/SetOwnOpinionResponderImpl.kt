package se.inera.intyg.srs.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import se.inera.intyg.clinicalprocess.healthcond.srs.setownopinion.v1.SetOwnOpinionRequestType
import se.inera.intyg.clinicalprocess.healthcond.srs.setownopinion.v1.SetOwnOpinionResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.setownopinion.v1.SetOwnOpinionResponseType
import se.inera.intyg.srs.persistence.repository.ProbabilityRepository
import se.inera.intyg.srs.vo.OwnOpinionModule

@Service
class SetOwnOpinionResponderImpl(val ownOpinionModule: OwnOpinionModule,
                                 val probabilityRepo: ProbabilityRepository) : SetOwnOpinionResponderInterface {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun setOwnOpinion(request: SetOwnOpinionRequestType): SetOwnOpinionResponseType {
        log.debug("Set own opinion request received")
        val probabilities = probabilityRepo.findByCertificateIdAndDiagnosisOrderByTimestampDesc(request.intygId.extension, request.diagnos.code)
        if (probabilities.isEmpty()) {
            throw RuntimeException("Found no stored probability to store the opinion on")
        }
        val probability = probabilities[0]
        val response = SetOwnOpinionResponseType()
        val result = ownOpinionModule.setOwnOpinion(request.vardgivareId.extension,
                request.vardenhetId.extension,
                probability,
                request.egenBedomningRisk.value())
        response.resultCode = result
        return response
    }
}