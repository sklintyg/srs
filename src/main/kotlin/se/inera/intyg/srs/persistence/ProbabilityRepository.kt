package se.inera.intyg.srs.persistence

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface ProbabilityRepository : CrudRepository<Probability, Long> {

    fun findByCertificateId(certificateId: String): List<Probability>

    @Query("FROM Probability p LEFT JOIN FETCH p.ownOpinion " +
            "WHERE p.certificateId IN :certificateIds ORDER BY p.certificateId ASC, p.timestamp DESC")
    fun findByCertificateIdInAndOrderedByCertificateIdAndTimestamp(@Param("certificateIds") certificateIds: List<String>): List<Probability>

    @Query("FROM Probability p LEFT JOIN FETCH p.ownOpinion LEFT JOIN FETCH p.patientAnswers " +
            "WHERE p.certificateId = :certificateId AND p.diagnosis = :diagnosis ORDER BY Timestamp DESC")
    fun findByCertificateIdAndDiagnosisOrderByTimestampDesc(certificateId: String, diagnosis: String): List<Probability>

}