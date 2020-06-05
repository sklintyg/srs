package se.inera.intyg.srs.persistence.entity

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Temporal

@Entity
data class PredictionQuestion(val question: String?,
                              val helpText: String?,
                              val predictionId: String,
                              val modelVersion: String,
                              @OneToMany(mappedBy = "question",fetch = FetchType.EAGER)
//                              @JoinColumn(name = "question_id")
                              var answers: Collection<PredictionResponse> = emptyList(),
                              @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
                              val id: Long = -1,
                              @CreationTimestamp
                              @Column(name = "created", updatable = false)
                              @Temporal(javax.persistence.TemporalType.TIMESTAMP)
                              var created: Date = Date(),
                              @UpdateTimestamp
                              @Column(name = "modified", updatable = false)
                              @Temporal(javax.persistence.TemporalType.TIMESTAMP)
                              var modified: Date = Date()
) {

    override fun toString(): String {
        return "PredictionQuestion(id=$id, question='$question', helpText='$helpText', predictionId='$predictionId', answers=$answers)"
    }
}
