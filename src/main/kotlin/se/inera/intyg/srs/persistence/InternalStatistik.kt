package se.inera.intyg.srs.persistence

import java.time.LocalDateTime
import org.hibernate.annotations.Type
import javax.persistence.*

@Entity
class InternalStatistik(val diagnosisId: String,
                        @Column(columnDefinition = "clob")
                        var pictureUrl: String,
                        @Type(type = "org.jadira.usertype.dateandtime.threeten.PersistentLocalDateTime")
                        var timestamp: LocalDateTime,
                        @Id @GeneratedValue(strategy = GenerationType.AUTO)
                        val id: Long = -1) {

    override fun toString() =
            "Measure(id=$id, diagnosisId='$diagnosisId', pictureUrl='$pictureUrl', timestamp='$timestamp')"

}
