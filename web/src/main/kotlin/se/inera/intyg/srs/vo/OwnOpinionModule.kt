package se.inera.intyg.srs.vo

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import se.inera.intyg.srs.persistence.entity.OwnOpinion
import se.inera.intyg.srs.persistence.entity.Probability
import se.inera.intyg.srs.persistence.repository.OwnOpinionRepository
import se.riv.clinicalprocess.healthcond.certificate.types.v2.ResultCodeEnum
import java.time.LocalDateTime
import javax.transaction.Transactional

@Service
open class OwnOpinionModule(private val ownOpinionRepo: OwnOpinionRepository) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun getOwnOpinion(careGiverHsaId: String, careUnitHsaId: String, probability: Probability) : OwnOpinion? {
        return ownOpinionRepo.findOwnOpinionByCareGiverIdAndCareUnitIdAndProbability(careGiverHsaId, careUnitHsaId, probability)
    }

    @Transactional
    open fun setOwnOpinion(careGiverHsaId: String, careUnitHsaId: String, probability: Probability, opinion: String): ResultCodeEnum {
        log.debug("setOwnOpinion(careGiverHsaId: $careGiverHsaId, careUnitHsaId: $careUnitHsaId, " +
            "probability: $probability, opinion: $opinion)")
        var ownOpinion = ownOpinionRepo.findOwnOpinionByCareGiverIdAndCareUnitIdAndProbability(careGiverHsaId, careUnitHsaId, probability)
        if (ownOpinion == null) {
            log.debug("Found no previous own opinion for caregiver, vare unit and probability, creating a new one.")
            ownOpinion = OwnOpinion(careGiverHsaId, careUnitHsaId, probability, opinion, LocalDateTime.now())
        } else {
            log.debug("Updating existing own opinion to new opinion: $opinion. Prior to update we have opinion: ${ownOpinion.opinion}")
            ownOpinion.opinion = opinion;
        }
        // Update own opinion
        ownOpinion.createdTime = LocalDateTime.now()
        ownOpinion = ownOpinionRepo.save(ownOpinion) ?: return ResultCodeEnum.ERROR
        log.debug("ownOpinion was saved and now has opinion: ${ownOpinion.opinion}")
        return ResultCodeEnum.OK
    }
}