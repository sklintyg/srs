package se.inera.intyg.srs.persistence.repository

import org.springframework.data.repository.CrudRepository
import se.inera.intyg.srs.persistence.entity.NationalStatistic
import java.util.*

interface NationalStatisticRepository : CrudRepository<NationalStatistic, Long> {

    fun findByDiagnosisIdOrderByDayIntervalMaxExcl(diagnosisId: String): List<NationalStatistic>

    fun findOneByDiagnosisIdAndDayIntervalMaxExcl(diagnosisId: String, dayIntervalMaxExcl: Int): Optional<NationalStatistic>

}
