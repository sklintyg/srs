package se.inera.intyg.srs.vo

data class Person(val personId: String, val age: Int, val sex: Sex, val diagnoses: List<Diagnosis>)

data class Diagnosis(val code: String, val codeSystem: String = "1.2.752.116.1.1.1.1.3")

enum class Sex(val predictionString: String) {
    MAN("M"), WOMAN("F")
}
