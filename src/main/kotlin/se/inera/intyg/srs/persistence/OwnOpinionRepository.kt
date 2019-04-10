package se.inera.intyg.srs.persistence

import org.springframework.data.repository.CrudRepository

interface OwnOpinionRepository : CrudRepository<OwnOpinion, Long> {
    fun findOwnOpinionByCareGiverIdAndCareUnitIdAndProbability(careGiverId: String, careUnitId: String, probability: Probability): OwnOpinion?
}