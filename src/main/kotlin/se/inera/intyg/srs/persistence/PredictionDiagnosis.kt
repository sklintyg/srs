package se.inera.intyg.srs.persistence

import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.OneToMany

@Entity
class PredictionDiagnosis(@Id
                          val id: Long,
                          val diagnosisId: String,
                          @OneToMany(fetch = FetchType.EAGER, mappedBy = "diagnosis")
                          val questions: MutableCollection<PredictionPriority>) {

}