package se.inera.intyg.srs.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany

@Entity
data class Measure(val diagnosisId: String,
              val diagnosisText: String,
              val version: String,
              @OneToMany(fetch = FetchType.EAGER, mappedBy = "measure")
              val priorities: Collection<MeasurePriority> = emptyList(),
              @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
              val id: Long = -1
            ) {

    override fun toString() =
            "Measure(id=$id, diagnosisId='$diagnosisId', diagnosisText='$diagnosisText', version='$version', priorities='$priorities')"

}
