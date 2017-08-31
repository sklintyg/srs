package se.inera.intyg.srs.persistence

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
class PredictionResponse(@Id
                         val id: Long,
                         val answer: String,
                         val predictionId: String,
                         val default: Boolean,
                         val priority: Int,
                         @ManyToOne
                         @JoinColumn(name = "question_id")
                         val predictionQuestion: PredictionQuestion) {

}
