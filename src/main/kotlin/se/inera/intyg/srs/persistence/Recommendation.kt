package se.inera.intyg.srs.persistence

import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany

@Entity
class Recommendation(val recommendationText: String,
                     @OneToMany(fetch = FetchType.EAGER)
                     var recommendations: Collection<Priority> = mutableListOf(),
                     @Id @GeneratedValue(strategy = GenerationType.AUTO)
                     val id: Long = -1) {

    override fun toString() =
            "Recommendation(id=$id, recommendationText='$recommendationText')"

}
