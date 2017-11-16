package se.inera.intyg.srs.persistence

import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Atgardstyp
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Id

@Entity
class Recommendation(@Id val id: Long,
                     @Enumerated(EnumType.STRING) val type: Atgardstyp,
                     val recommendationText: String) {

    override fun toString() =
            "Recommendation(id=$id, type=$type, recommendationText='$recommendationText')"
}
