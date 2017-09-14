package se.inera.intyg.srs.persistence

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Column

@Entity
class Recommendation(@Id
                     val id: Long,
                     val recommendationText: String) {

    override fun toString() =
            "Recommendation(id=$id, recommendationText='$recommendationText')"

}
