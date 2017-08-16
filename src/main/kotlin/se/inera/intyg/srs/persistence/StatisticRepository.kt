package se.inera.intyg.srs.persistence

import org.springframework.data.repository.CrudRepository

interface StatisticRepository : CrudRepository<InternalStatistic, Long> {

    fun findByDiagnosisId(diagnosisId: String): List<InternalStatistic>

}
