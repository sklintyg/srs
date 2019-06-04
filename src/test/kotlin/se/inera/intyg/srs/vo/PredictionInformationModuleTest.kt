package se.inera.intyg.srs.vo

import com.nhaarman.mockito_kotlin.mock
import org.junit.Assert
import org.junit.Test


class PredictionInformationModuleTest {

    @Test
    fun testPredictionRiskRange() {
        val predictionInformationModule = PredictionInformationModule(mock(),mock(),mock(),mock(), mock(), mock())
        Assert.assertEquals(1, predictionInformationModule.calculateRisk(0.38))
        Assert.assertEquals(2, predictionInformationModule.calculateRisk(0.39))
        Assert.assertEquals(2, predictionInformationModule.calculateRisk(0.40))

        Assert.assertEquals(2, predictionInformationModule.calculateRisk(0.61))
        Assert.assertEquals(2, predictionInformationModule.calculateRisk(0.6200))
        Assert.assertEquals(3, predictionInformationModule.calculateRisk(0.6205))

        Assert.assertEquals(0, predictionInformationModule.calculateRisk(Double.NaN))

    }

}
