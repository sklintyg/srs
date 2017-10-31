package se.inera.intyg.srs.util

import java.math.BigInteger

object PredictionInformationUtil {
    val categoryDescriptions = mapOf(BigInteger.ONE to "Prediktion saknas.",
            BigInteger.valueOf(2) to "Ingen förhöjd risk detekterad.",
            BigInteger.valueOf(3) to "Förhöjd risk detekterad.",
            BigInteger.valueOf(4) to "Starkt förhöjd risk detekterad.")
}