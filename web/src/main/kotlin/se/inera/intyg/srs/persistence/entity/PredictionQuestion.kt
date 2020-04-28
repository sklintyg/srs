package se.inera.intyg.srs.persistence.entity

import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany

@Entity
data class PredictionQuestion(val question: String?,
                              val helpText: String?,
                              val predictionId: String,
                              @OneToMany(mappedBy = "question",fetch = FetchType.EAGER)
//                              @JoinColumn(name = "question_id")
                              var answers: Collection<PredictionResponse> = emptyList(),
                              @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
                              val id: Long = -1
) {

    override fun toString(): String {
        return "PredictionQuestion(id=$id, question='$question', helpText='$helpText', predictionId='$predictionId', answers=$answers)"
    }
}
