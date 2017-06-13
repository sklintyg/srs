package se.inera.intyg.srs.vo

data class Person(val personId: String, val age: Int, val sex: Sex, val extent: Extent, val diagnoses: List<Diagnose>)

data class Diagnose(val code: String, val codeSystem: String = "1.2.752.116.1.1.1.1.3")

enum class Extent(val predictionString: String) {
    HELT_NEDSATT("1"), TRE_FJARDEDEL("0.75"), HALFTEN("0.5"), EN_FJARDEDEL("0.25") }

enum class Sex(val predictionString: String) {
    MAN("M"), WOMAN("F")
}
