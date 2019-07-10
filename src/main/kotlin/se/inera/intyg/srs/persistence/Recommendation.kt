package se.inera.intyg.srs.persistence

import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Atgardstyp
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.validation.constraints.NotBlank

@Entity
data class Recommendation(
        @Enumerated(EnumType.STRING) val type: Atgardstyp,

        val recommendationTitle: String,

        val recommendationText: String,

        // We do not use generated id:s here since we use the id's from the indata file
        @Id
        val id: Long = 0
)