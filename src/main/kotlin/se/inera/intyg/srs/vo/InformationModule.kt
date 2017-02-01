package se.inera.intyg.srs.vo

interface InformationModule {

    fun getInfo(persons: List<Person>) : String

}