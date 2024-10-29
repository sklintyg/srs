package se.inera.intyg.srs.persistence.entity

import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Atgardstyp
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id

@Entity
data class Recommendation(
        @Enumerated(EnumType.STRING) val type: Atgardstyp,

        val recommendationTitle: String,

        val recommendationText: String,

        // We do not use generated id:s here since we use the id's from the indata file
        @Id
        val id: Long = 0
)