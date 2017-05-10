package se.inera.intyg.srs.db

import org.springframework.data.repository.CrudRepository

interface MeasureRepository : CrudRepository<Measure, Long> {

    fun findByDiagnoseIdStartingWith(diagnoseId: String): List<Measure>

}
