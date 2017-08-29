package se.inera.intyg.srs.persistence

import org.springframework.data.repository.CrudRepository

interface ConsentRepository : CrudRepository<Consent, Long> {
    fun findConsentByPersonnummerAndVardenhet(personnummer: String, vardenhet: String): Consent
}