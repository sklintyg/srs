package se.inera.intyg.srs.persistence.repository

import org.springframework.data.repository.CrudRepository
import se.inera.intyg.srs.persistence.entity.Measure
import java.util.*

interface MeasureRepository : CrudRepository<Measure, Long> {

    fun findByDiagnosisIdStartingWith(diagnosisId: String): List<Measure>
    fun findByDiagnosisId(diagnosisId: String): Optional<Measure>

}
