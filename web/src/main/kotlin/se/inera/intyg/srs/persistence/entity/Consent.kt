package se.inera.intyg.srs.persistence.entity

import java.time.LocalDateTime
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

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