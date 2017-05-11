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
              val priority: Int,
              val version: String,
              @OneToMany(fetch = FetchType.EAGER)
              var recommendations: MutableCollection<Priority> = mutableListOf(),
              @Id @GeneratedValue(strategy = GenerationType.AUTO)
              val id: Long = -1) {

    override fun toString() =
            "Measure(id=$id, diagnoseId='$diagnoseId', diagnoseText='$diagnoseText', priority='$priority', version='$version')"

}
