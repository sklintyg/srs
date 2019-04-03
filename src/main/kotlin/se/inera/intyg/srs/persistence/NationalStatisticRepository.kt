package se.inera.intyg.srs.persistence

import org.springframework.data.repository.CrudRepository
import java.util.Optional

interface NationalStatisticRepository : CrudRepository<NationalStatistic, Long> {

    fun findByDiagnosisIdOrderByDayIntervalMaxExcl(diagnosisId: String): List<NationalStatistic>

    fun findOneByDiagnosisIdAndDayIntervalMaxExcl(diagnosisId: String, dayIntervalMaxExcl: Int): Optional<NationalStatistic>

}
