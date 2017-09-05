package se.inera.intyg.srs.persistence

import java.time.LocalDateTime
import org.hibernate.annotations.Type
import javax.persistence.*

@Entity
class InternalStatistic(val diagnosisId: String,
                        @Column(length = 400)
                        var pictureUrl: String,
                        @Type(type = "org.jadira.usertype.dateandtime.threeten.PersistentLocalDateTime")
                        var timestamp: LocalDateTime,
                        @Id @GeneratedValue(strategy = GenerationType.AUTO)
                        val id: Long = -1) {

    override fun toString() =
            "Measure(id=$id, diagnosisId='$diagnosisId', pictureUrl='$pictureUrl', timestamp='$timestamp')"

}
