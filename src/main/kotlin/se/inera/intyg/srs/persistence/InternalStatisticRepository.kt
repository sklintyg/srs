package se.inera.intyg.srs.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.CrudRepository

interface InternalStatisticRepository : JpaRepository<InternalStatistic, Long> {

    fun findByDiagnosisId(diagnosisId: String): List<InternalStatistic>

}
