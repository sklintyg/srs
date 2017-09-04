package se.inera.intyg.srs.vo

interface InformationModule<T> {

    fun getInfo(persons: List<Person>, extraParams: Map<String, String>): Map<Person, T>

}
