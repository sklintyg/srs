package se.inera.intyg.srs.util

import java.math.BigInteger

object PredictionInformationUtil {
    val categoryDescriptions = mapOf(BigInteger.ZERO to "Prediktion saknas.",
            BigInteger.valueOf(1) to "Måttlig risk",
            BigInteger.valueOf(2) to "Hög risk",
            BigInteger.valueOf(3) to "Mycket hög risk")
}
