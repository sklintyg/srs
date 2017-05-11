package se.inera.intyg.srs.persistence

import org.springframework.data.repository.CrudRepository

interface RecommendationRepository : CrudRepository<Recommendation, Long> {

}
