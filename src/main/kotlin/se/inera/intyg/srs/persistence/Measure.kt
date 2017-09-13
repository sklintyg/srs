package se.inera.intyg.srs.persistence

import javax.persistence.*

@Entity
class Measure(@Id
              val id: Long,
              val diagnosisId: String,
              @Column(name = "diagnosis_text", columnDefinition = "text")
              val diagnosisText: String,
              val version: String,
              @OneToMany(fetch = FetchType.EAGER)
              @JoinColumn(name="measure_id")
              val priorities: Collection<MeasurePriority>) {

    override fun toString() =
            "Measure(id=$id, diagnosisId='$diagnosisId', diagnosisText='$diagnosisText', version='$version', priorities='$priorities')"

}
