package se.inera.intyg.srs.vo

interface InformationModule<T> {

    fun getInfo(persons: List<Person>, extraParams: Map<String, String> = mapOf()): Map<Person, List<T>>

    fun getInfoForDiagnosis(diagnosisId: String): T

}
