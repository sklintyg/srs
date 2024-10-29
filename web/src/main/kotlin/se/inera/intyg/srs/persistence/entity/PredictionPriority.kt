package se.inera.intyg.srs.persistence.entity

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.util.*
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Temporal

@Entity
class PredictionPriority(val priority: Int,
                         val modelVersion: String,
                         val forSubdiagnosis: Boolean,
                         @ManyToOne
                         val question: PredictionQuestion,
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
    return "PredictionPriority(id=$id, priority='$priority', modelVersion='$modelVersion', forSubDiagnosis='$forSubdiagnosis', " +
        "created='$created', modified='$modified', question='$question')"
  }
}
