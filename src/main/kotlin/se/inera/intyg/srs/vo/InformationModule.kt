package se.inera.intyg.srs.vo

interface InformationModule<T> {

    fun getInfo(persons: List<Person>, extraParams: Map<String, String> = mapOf(), userHsaId: String = "noInfo"): Map<Person, T>

}
