package se.inera.intyg.srs.persistence

import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany

@Entity
class Measure(val diagnoseId: String,
              val diagnoseText: String,
              val version: String,
              @OneToMany(fetch = FetchType.EAGER)
              val priorities: Collection<Priority>,
              @Id @GeneratedValue(strategy = GenerationType.AUTO)
              val id: Long = -1) {

    override fun toString() =
            "Measure(id=$id, diagnoseId='$diagnoseId', diagnoseText='$diagnoseText', version='$version', priorities='$priorities')"

}
