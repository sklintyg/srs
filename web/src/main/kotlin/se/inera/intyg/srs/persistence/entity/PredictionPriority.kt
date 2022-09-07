package se.inera.intyg.srs.persistence.entity

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.Temporal

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
                         @Temporal(javax.persistence.TemporalType.TIMESTAMP)
                         var created: Date = Date(),
                         @UpdateTimestamp
                         @Column(name = "modified", updatable = false)
                         @Temporal(javax.persistence.TemporalType.TIMESTAMP)
                         var modified: Date = Date()
) {
  override fun toString(): String {
    return "PredictionPriority(id=$id, priority='$priority', modelVersion='$modelVersion', forSubDiagnosis='$forSubdiagnosis', " +
        "created='$created', modified='$modified', question='$question')"
  }
}
