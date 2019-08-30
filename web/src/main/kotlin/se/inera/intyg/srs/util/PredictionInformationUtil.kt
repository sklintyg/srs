package se.inera.intyg.srs.util

object PredictionInformationUtil {
    val categoryDescriptions = mapOf(0 to "Prediktion saknas.",
            1 to "Måttlig risk att sjukfallet varar i mer än 90 dagar",
            2 to "Hög risk att sjukfallet varar i mer än 90 dagar",
            3 to "Mycket hög risk att sjukfallet varar i mer än 90 dagar")
}
