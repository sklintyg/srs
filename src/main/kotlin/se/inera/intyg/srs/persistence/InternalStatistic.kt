package se.inera.intyg.srs.persistence

import org.hibernate.annotations.Type
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class InternalStatistic(val diagnosisId: String,
                        var pictureUrl: String,
                        @Type(type = "org.jadira.usertype.dateandtime.threeten.PersistentLocalDateTime")
                        var timestamp: LocalDateTime,
                        @Id @GeneratedValue(strategy = GenerationType.AUTO)
                        val id: Long = -1) {

    override fun toString() =
            "InternalStatistic(id=$id, diagnosisId='$diagnosisId', pictureUrl='$pictureUrl', timestamp='$timestamp')"

}
