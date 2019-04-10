package se.inera.intyg.srs.persistence

import org.hibernate.annotations.Type
import java.time.LocalDateTime
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.OneToOne

@Entity
class Probability(val certificateId: String,
                  val probability: Double,
                  val riskCategory: Int,
                  val incomingDiagnosis: String,
                  val diagnosis: String,
                  @Type(type = "org.jadira.usertype.dateandtime.threeten.PersistentLocalDateTime")
                  val timestamp: LocalDateTime,
                  @Id @GeneratedValue(strategy = GenerationType.AUTO)
                  val id: Long = -1) {

    @OneToOne(mappedBy = "probability", cascade = [CascadeType.ALL])
    val ownOpinion: OwnOpinion? = null

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "probability", cascade = [CascadeType.ALL])
    val patientAnswers: Collection<PatientAnswer>? = null

    override fun toString() =
        "Probability(id=$id, certificateId=$certificateId, probability=$probability, riskCategory=$riskCategory" +
                "incomingDiagnosis=$incomingDiagnosis, diagnosis=$diagnosis, timestamp=$timestamp, " +
                "ownOpinion: $ownOpinion)"

}
