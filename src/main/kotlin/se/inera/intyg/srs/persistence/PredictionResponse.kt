package se.inera.intyg.srs.persistence

import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.OneToMany

@Entity
class PredictionResponse(@Id
                         val id: Long,
                         val answer: String,
                         val predictionId: String,
                         val isDefault: Boolean,
                         val priority: Int
                        ) {
    @ManyToOne
    lateinit var question: PredictionQuestion

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "predictionResponse")
    var patientAnswers: Collection<PatientAnswer> = emptyList()

    override fun toString(): String {
        return "PredictionResponse(id=$id, answer='$answer', predictionId='$predictionId', isDefault=$isDefault, priority=$priority)"
    }
}
