package se.inera.intyg.srs.persistence

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
                  val incomingDiagnosisCodeSystem: String,
                  val incomingDiagnosis: String,
                  val diagnosisCodeSystem: String,
                  val diagnosis: String,
                  val predictionStatus: String,
                  val timestamp: LocalDateTime,
                  val region: String?,
                  val zipCode: String?,
                  @Id @GeneratedValue(strategy = GenerationType.AUTO)
                  val id: Long = -1) {

    @OneToOne(mappedBy = "probability", cascade = [CascadeType.ALL])
    val ownOpinion: OwnOpinion? = null

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "probability", cascade = [CascadeType.ALL])
    val patientAnswers: Collection<PatientAnswer>? = null

    override fun toString() =
        "Probability(id=$id, certificateId=$certificateId, probability=$probability, riskCategory=$riskCategory" +
                "incomingDiagnosisCodeSystem=$incomingDiagnosisCodeSystem, incomingDiagnosis=$incomingDiagnosis, " +
                "diagnosisCodeSystem=$diagnosisCodeSystem, diagnosis=$diagnosis, predictionStatus:$predictionStatus, " +
                "timestamp=$timestamp, ownOpinion: $ownOpinion)"

}
