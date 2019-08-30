package se.inera.intyg.srs.persistence.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany

@Entity
data class PredictionResponse(
        val answer: String,
        val predictionId: String,
        val isDefault: Boolean,
        val priority: Int,
        @ManyToOne
        @JoinColumn(name = "question_id")
        @JsonIgnore
        var question: PredictionQuestion?,
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long = -1
) {

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "predictionResponse")
    @JsonIgnore
    var patientAnswers: Collection<PatientAnswer> = emptyList()

    override fun toString(): String {
        return "PredictionResponse(id=$id, answer='$answer', predictionId='$predictionId', isDefault=$isDefault, priority=$priority)"
    }
}
