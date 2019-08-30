package se.inera.intyg.srs.persistence.repository

import org.springframework.data.repository.CrudRepository
import se.inera.intyg.srs.persistence.entity.Consent

interface ConsentRepository : CrudRepository<Consent, Long> {
    fun findConsentByPersonnummerAndVardenhetId(personnummer: String, vardenhetId: String): Consent?
}