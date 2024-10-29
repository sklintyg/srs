package se.inera.intyg.srs.persistence.entity

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.util.*
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Temporal

@Entity
data class PredictionQuestion(val question: String?,
                              val helpText: String?,
                              val predictionId: String,
                              val modelVersion: String,
                              val forSubdiagnosis: Boolean,
                              @OneToMany(mappedBy = "question",fetch = FetchType.EAGER)
//                              @JoinColumn(name = "question_id")
                              var answers: Collection<PredictionResponse> = emptyList(),
                              @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
                              val id: Long = -1,
                              @CreationTimestamp
                              @Column(name = "created", updatable = false)
                              @Temporal(jakarta.persistence.TemporalType.TIMESTAMP)
                              var created: Date = Date(),
                              @UpdateTimestamp
                              @Column(name = "modified", updatable = false)
                              @Temporal(jakarta.persistence.TemporalType.TIMESTAMP)
                              var modified: Date = Date()
) {

    override fun toString(): String {
        return "PredictionQuestion(id=$id, question='$question', helpText='$helpText', predictionId='$predictionId', answers=$answers, " +
            "modelVersion=$modelVersion, forSubdiagnosis=$forSubdiagnosis)"
    }
}
