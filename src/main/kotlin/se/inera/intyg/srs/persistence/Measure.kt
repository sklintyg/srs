package se.inera.intyg.srs.persistence

import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.OneToMany

@Entity
class Measure(@Id
              val id: Long,
              val diagnosisId: String,
              val diagnosisText: String,
              val version: String,
              @OneToMany(fetch = FetchType.EAGER, mappedBy = "measure")
              val priorities: MutableCollection<MeasurePriority>) {

    override fun toString() =
            "Measure(id=$id, diagnosisId='$diagnosisId', diagnosisText='$diagnosisText', version='$version', priorities='$priorities')"

}
