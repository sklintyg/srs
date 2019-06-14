package se.inera.intyg.srs.persistence

import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToMany

@Entity
data class PredictionDiagnosis(val diagnosisId: String,
                          val prevalence: Double,
                          @OneToMany(fetch = FetchType.EAGER)
                          @JoinColumn(name="diagnosis_id")
                          val questions: Collection<PredictionPriority>,
                          @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
                          val id: Long = -1)
