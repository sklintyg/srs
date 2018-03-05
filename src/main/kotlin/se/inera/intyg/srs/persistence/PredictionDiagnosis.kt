package se.inera.intyg.srs.persistence

import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToMany

@Entity
class PredictionDiagnosis(@Id
                          val id: Long,
                          val diagnosisId: String,
                          val prevalence: Double,
                          val threshold: Double,
                          val thresholdElevated: Double,
                          @OneToMany(fetch = FetchType.EAGER)
                          @JoinColumn(name="diagnosis_id")
                          val questions: Collection<PredictionPriority>)
