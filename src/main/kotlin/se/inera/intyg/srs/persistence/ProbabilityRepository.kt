package se.inera.intyg.srs.persistence

import org.springframework.data.repository.CrudRepository

interface ProbabilityRepository : CrudRepository<Probability, Long> {
    fun findByCertificateId(certificateId: String): List<Probability>
    fun findFirstByCertificateIdOrderByTimestampDesc(certificateId: String): Probability?
}