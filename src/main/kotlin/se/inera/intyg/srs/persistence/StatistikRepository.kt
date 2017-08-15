package se.inera.intyg.srs.persistence

import org.springframework.data.repository.CrudRepository

interface StatistikRepository : CrudRepository<InternalStatistik, Long> {

    fun findByDiagnosisId(diagnosisId: String): List<InternalStatistik>

}
