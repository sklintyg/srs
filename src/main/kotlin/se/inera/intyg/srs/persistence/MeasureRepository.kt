package se.inera.intyg.srs.persistence

import org.springframework.data.repository.CrudRepository
import java.util.*

interface MeasureRepository : CrudRepository<Measure, Long> {

    fun findByDiagnosisIdStartingWith(diagnosisId: String): List<Measure>
    fun findByDiagnosisId(diagnosisId: String): Optional<Measure>

}
