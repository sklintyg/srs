package se.inera.intyg.srs.persistence

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
        @Id @GeneratedValue(strategy = GenerationType.AUTO)
        val id: Long = -1) {

    override fun toString() =
            "Probability(id=$id, certificateId=$certificateId, probability=$probability, riskCategory=$riskCategory" +
                    "incommingDiagnosis=$incommingDiagnosis, diagnosis=$diagnosis)"

}