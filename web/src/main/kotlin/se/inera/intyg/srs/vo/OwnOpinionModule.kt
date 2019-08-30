package se.inera.intyg.srs.vo

import org.springframework.stereotype.Service
import se.inera.intyg.srs.persistence.entity.OwnOpinion
import se.inera.intyg.srs.persistence.repository.OwnOpinionRepository
import se.inera.intyg.srs.persistence.entity.Probability
import se.riv.clinicalprocess.healthcond.certificate.types.v2.ResultCodeEnum
import java.time.LocalDateTime

@Service
class OwnOpinionModule(private val ownOpinionRepo: OwnOpinionRepository) {
    fun getOwnOpinion(careGiverHsaId: String, careUnitHsaId: String, probability: Probability) : OwnOpinion? {
        return ownOpinionRepo.findOwnOpinionByCareGiverIdAndCareUnitIdAndProbability(careGiverHsaId, careUnitHsaId, probability)
    }

    fun setOwnOpinion(careGiverHsaId: String, careUnitHsaId: String, probability: Probability, opinion: String): ResultCodeEnum {
        var ownOpinion = ownOpinionRepo.findOwnOpinionByCareGiverIdAndCareUnitIdAndProbability(careGiverHsaId, careUnitHsaId, probability)
        if (ownOpinion == null) {
            ownOpinion = OwnOpinion(careGiverHsaId, careUnitHsaId, probability, opinion, LocalDateTime.now())
        }
        // Update own opinion
        ownOpinion.createdTime = LocalDateTime.now()
        ownOpinionRepo.save(ownOpinion) ?: return ResultCodeEnum.ERROR
        return ResultCodeEnum.OK
    }
}