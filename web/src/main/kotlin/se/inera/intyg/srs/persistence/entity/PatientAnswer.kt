package se.inera.intyg.srs.persistence.entity

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
class PatientAnswer(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long = -1) {
    // See: https://dev.to/livioribeiro/mapping-jpa-entities-with-kotlin-36d

    @ManyToOne
    @JoinColumn(name = "probability_id", referencedColumnName = "id")
    lateinit var probability: Probability

    @ManyToOne
    @JoinColumn(name = "prediction_response_id", referencedColumnName = "id")
    lateinit var predictionResponse: PredictionResponse

    override fun toString() = "PatientAnswer(probability: ${probability.id}, predictionResponse: ${predictionResponse.id}"
}