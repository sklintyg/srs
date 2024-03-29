package se.inera.intyg.srs.vo

interface InformationModule<T> {

    fun getInfo(persons: List<Person>, extraParams: Map<String, Map<String, String>> = mapOf(),
                careUnitHsaId: String = "noInfo", calculateIndividual: Boolean = false,
                daysIntoSickLeave:Int = 15): Map<Person, List<T>>

    fun getInfoForDiagnosis(diagnosisId: String): T

}
