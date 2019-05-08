package se.inera.intyg.srs.vo

interface InformationModule<T> {

    fun getInfo(persons: List<Person>, extraParams: Map<String, Map<String, String>> = mapOf(), userHsaId: String = "noInfo", calculateIndividual: Boolean = false): Map<Person, List<T>>

    fun getInfoForDiagnosis(diagnosisId: String): T

}
