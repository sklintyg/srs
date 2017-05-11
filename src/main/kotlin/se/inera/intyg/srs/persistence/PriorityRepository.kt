package se.inera.intyg.srs.persistence

import org.springframework.data.repository.CrudRepository

interface PriorityRepository : CrudRepository<Priority, Long> {

}
