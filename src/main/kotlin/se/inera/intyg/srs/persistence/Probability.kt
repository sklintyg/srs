package se.inera.intyg.srs.persistence

import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class Probability(
        val certificateId: String,
        val probability: Double,
        val riskCategory: Int,
        val incommingDiagnosis: String,
        val diagnosis: String,
        val timestamp: LocalDateTime,
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long = -1) {

    override fun toString() =
            "Probability(id=$id, certificateId=$certificateId, probability=$probability, riskCategory=$riskCategory" +
                    "incommingDiagnosis=$incommingDiagnosis, diagnosis=$diagnosis, timestamp=$timestamp)"

}
