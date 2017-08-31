package se.inera.intyg.srs.persistence

import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.OneToMany

@Entity
class PredictionQuestion(@Id
                         val id: Long,
                         val question: String,
                         val helpText: String,
                         val predictionId: String,
                         @OneToMany(fetch = FetchType.EAGER, mappedBy = "predictionQuestion")
                         val answers: MutableCollection<PredictionResponse>) {

    override fun toString(): String {
        return "PredictionQuestion(id=$id, question='$question', helpText='$helpText', predictionId='$predictionId', answers=$answers)"
    }
}