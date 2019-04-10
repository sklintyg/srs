package se.inera.intyg.srs.persistence

import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class InternalStatistic(val diagnosisId: String,
                        var pictureUrl: String,
                        var timestamp: LocalDateTime,
                        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
                        val id: Long = -1) {

    override fun toString() =
            "Measure(id=$id, diagnosisId='$diagnosisId', pictureUrl='$pictureUrl', timestamp='$timestamp')"

}
