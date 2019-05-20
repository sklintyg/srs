package se.inera.intyg.srs.persistence

import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Atgardstyp
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Id
import javax.validation.constraints.NotBlank

@Entity
data class Recommendation(
        @Id val id: Long,

//        @get: NotBlank
        @Enumerated(EnumType.STRING) val type: Atgardstyp,

        val recommendationTitle: String,
//        @get: NotBlank
        val recommendationText: String
)
//{
//
//    override fun toString() =
//            "Recommendation(id=$id, type=$type, recommendationText='$recommendationText')"
//}