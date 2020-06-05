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
import javax.persistence.JoinColumn
import javax.persistence.OneToMany
import javax.persistence.Temporal

@Entity
data class PredictionDiagnosis(val diagnosisId: String,
                               val prevalence: Double,
                               val resolution: Int?,
                               val modelVersion: String,
                               @OneToMany(fetch = FetchType.EAGER)
                               @JoinColumn(name = "diagnosis_id")
                               val questions: Collection<PredictionPriority>,
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
)

