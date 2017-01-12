package se.inera.intyg.srs

import org.springframework.data.repository.CrudRepository

interface CustomerRepository : CrudRepository<Customer, Long> {

    fun findByLastNameIgnoreCase(lastName: String): Iterable<Customer>

    fun findByFirstNameAndLastNameIgnoreCase(firstName: String, lastName: String): Iterable<Customer>

}
