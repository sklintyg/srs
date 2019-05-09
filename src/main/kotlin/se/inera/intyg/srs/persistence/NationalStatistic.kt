package se.inera.intyg.srs.persistence

import org.hibernate.annotations.Type
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class NationalStatistic(val diagnosisId: String,
                        var dayIntervalMin: Int,
                        var dayIntervalMaxExcl: Int, // the upper limit of the closed day interval, the lower day limit is the upper limit of the limit below + 1
                        var intervalQuantity: Int, // the quantity of the given interval
                        var accumulatedQuantity: Int, // the accumulated quantity of this day interval plus the day intervals below this
                        var timestamp: LocalDateTime,
                        @Id @GeneratedValue(strategy = GenerationType.AUTO)
                        val id: Long = -1) {

    override fun toString() =
            "NationalStatistic(id=$id, diagnosisId='$diagnosisId', dayIntervalMin='$dayIntervalMin', " +
                    "dayIntervalMaxExcl='$dayIntervalMaxExcl', intervalQuantity='$intervalQuantity', " +
                    "accumulatedQuantity='$accumulatedQuantity', timestamp='$timestamp')"

}
