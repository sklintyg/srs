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
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.Temporal

@Entity
data class PredictionDiagnosis(val diagnosisId: String,
                               val prevalence: Double,
                               val resolution: Int?,
                               val modelVersion: String,
                               val forSubdiagnosis: Boolean,
                               @OneToMany(fetch = FetchType.EAGER)
                               @JoinColumn(name = "diagnosis_id")
                               val questions: Collection<PredictionPriority>,
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
)

