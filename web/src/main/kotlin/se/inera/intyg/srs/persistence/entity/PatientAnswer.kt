package se.inera.intyg.srs.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

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