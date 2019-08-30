package se.inera.intyg.srs.persistence.entity

import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class Consent(
        val personnummer: String,

        val vardenhetId: String,

        var skapatTid: LocalDateTime,

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long = 0
) {

    override fun toString() = "Consent(personnummer: $personnummer, vardenhet: $vardenhetId tidpunkt: $skapatTid)"
}