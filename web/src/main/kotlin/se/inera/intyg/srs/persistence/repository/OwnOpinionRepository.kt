package se.inera.intyg.srs.persistence.repository

import org.springframework.data.repository.CrudRepository
import se.inera.intyg.srs.persistence.entity.OwnOpinion
import se.inera.intyg.srs.persistence.entity.Probability

interface OwnOpinionRepository : CrudRepository<OwnOpinion, Long> {
    fun findOwnOpinionByCareGiverIdAndCareUnitIdAndProbability(careGiverId: String, careUnitId: String, probability: Probability): OwnOpinion?
}