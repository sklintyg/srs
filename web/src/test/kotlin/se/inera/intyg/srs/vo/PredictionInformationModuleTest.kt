package se.inera.intyg.srs.vo

import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


class PredictionInformationModuleTest {

    @Test
    fun testPredictionRiskRange() {
        val predictionInformationModule = PredictionInformationModule(mock(),mock(),mock(),mock(), mock(), mock())
        assertEquals(1, predictionInformationModule.calculateRisk(0.38))
        assertEquals(2, predictionInformationModule.calculateRisk(0.39))
        assertEquals(2, predictionInformationModule.calculateRisk(0.40))

        assertEquals(2, predictionInformationModule.calculateRisk(0.61))
        assertEquals(2, predictionInformationModule.calculateRisk(0.6200))
        assertEquals(3, predictionInformationModule.calculateRisk(0.6205))

        assertEquals(0, predictionInformationModule.calculateRisk(Double.NaN))

    }

}
