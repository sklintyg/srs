package se.inera.intyg.srs.persistence.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import se.inera.intyg.srs.persistence.entity.Probability

interface ProbabilityRepository : CrudRepository<Probability, Long> {

    fun findByCertificateId(certificateId: String): List<Probability>

    @Query("FROM Probability p LEFT JOIN FETCH p.ownOpinion " +
            "WHERE p.certificateId IN :certificateIds ORDER BY p.certificateId ASC, p.timestamp DESC")
    fun findByCertificateIdInAndOrderedByCertificateIdAndTimestamp(@Param("certificateIds") certificateIds: List<String>): List<Probability>

    @Query("FROM Probability p LEFT JOIN FETCH p.ownOpinion LEFT JOIN FETCH p.patientAnswers " +
            "WHERE p.certificateId = :certificateId AND p.diagnosis = :diagnosis ORDER BY timestamp DESC")
    fun findByCertificateIdAndDiagnosisOrderByTimestampDesc(certificateId: String, diagnosis: String): List<Probability>

}