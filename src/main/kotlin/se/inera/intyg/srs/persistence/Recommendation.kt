package se.inera.intyg.srs.persistence

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.*

@Entity
class Recommendation(@Id
                     val id: Long,
                     @Column(name = "recommendation_text", columnDefinition = "clob")
                     val recommendationText: String) {

    override fun toString() =
            "Recommendation(id=$id, recommendationText='$recommendationText')"

}
