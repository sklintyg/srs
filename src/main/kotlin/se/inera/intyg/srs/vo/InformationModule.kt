package se.inera.intyg.srs.vo

interface InformationModule<T> {

    fun getInfo(persons: List<Person>): Map<Person, T>

}
