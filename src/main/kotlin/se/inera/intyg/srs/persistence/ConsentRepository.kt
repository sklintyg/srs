package se.inera.intyg.srs.persistence

import org.springframework.data.repository.CrudRepository

interface ConsentRepository : CrudRepository<Consent, Long> {
    fun findConsentByPersonnummerAndVardenhetId(personnummer: String, vardenhetId: String): Consent?
}