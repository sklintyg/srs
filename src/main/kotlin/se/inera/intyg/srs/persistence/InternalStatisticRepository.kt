package se.inera.intyg.srs.persistence

import org.springframework.data.repository.CrudRepository

interface InternalStatisticRepository : CrudRepository<InternalStatistic, Long> {

    fun findByDiagnosisId(diagnosisId: String): List<InternalStatistic>

}
