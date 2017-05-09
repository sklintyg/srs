package se.inera.intyg.srs.db

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class Measure(val diagnoseId: String,
              val diagnoseText: String,
              val priority: Int,
              val measureText: String,
              val version: String,
              @Id @GeneratedValue(strategy = GenerationType.AUTO)
              val id: Long = -1) {

    override fun toString(): String {
        return "Measure(id=$id, diagnoseId='$diagnoseId', diagnoseText='$diagnoseText', priority='$priority', measureText='$measureText', version='$version')"
    }

}
