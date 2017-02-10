package se.inera.intyg.srs.vo

data class Person(val personId: String, val age: Int, val sex: Sex, val extent: Extent?, val diagnoses: List<Diagnose>)

data class Diagnose(val code: String, val codeSystem: String = "1.2.752.116.1.1.1.1.3")

enum class Extent { HELT_NEDSATT, TRE_FJARDEDEL, HALFTEN, EN_FJARDEDEL }

enum class Sex { MAN, WOMAN }
