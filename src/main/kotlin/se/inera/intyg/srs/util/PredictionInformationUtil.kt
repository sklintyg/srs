package se.inera.intyg.srs.util

import java.math.BigInteger

object PredictionInformationUtil {
    val categoryDescriptions = mapOf(BigInteger.ONE to "Prediktion saknas.",
            BigInteger.valueOf(2) to "Lätt förhöjd risk",
            BigInteger.valueOf(3) to "Måttligt förhöjd risk",
            BigInteger.valueOf(4) to "Starkt förhöjd risk")
}
