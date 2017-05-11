package se.inera.intyg.srs.persistence

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class Recommendation(val recommendationText: String,
                     @Id @GeneratedValue(strategy = GenerationType.AUTO)
                     val id: Long = -1) {

    override fun toString() =
            "Recommendation(id=$id, recommendationText='$recommendationText')"

}
