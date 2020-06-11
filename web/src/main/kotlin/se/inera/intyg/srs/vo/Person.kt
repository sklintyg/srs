package se.inera.intyg.srs.vo

data class Person(val personId: String, val ageCategory: String, val sex: Sex, val certDiags: List<CertDiagnosis>)

data class CertDiagnosis(val certificateId:String, val code: String, val codeSystem: String = "1.2.752.116.1.1.1.1.3")

enum class Sex(val predictionString: String) {
    MAN("Male"), WOMAN("Female");
}
