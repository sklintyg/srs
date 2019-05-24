package se.inera.intyg.srs.vo

import com.nhaarman.mockito_kotlin.mock
import org.junit.Assert
import org.junit.Test
import java.math.BigInteger


class PredictionInformationModuleTest {

    @Test
    fun testPredictionRiskRange() {
        val predictionInformationModule = PredictionInformationModule(mock(),mock(),mock(),mock(), mock(), mock())
        Assert.assertEquals(BigInteger.ONE, predictionInformationModule.calculateRisk(0.38))
        Assert.assertEquals(BigInteger.valueOf(2), predictionInformationModule.calculateRisk(0.39))
        Assert.assertEquals(BigInteger.valueOf(2), predictionInformationModule.calculateRisk(0.40))

        Assert.assertEquals(BigInteger.valueOf(2), predictionInformationModule.calculateRisk(0.61))
        Assert.assertEquals(BigInteger.valueOf(2), predictionInformationModule.calculateRisk(0.6200))
        Assert.assertEquals(BigInteger.valueOf(3), predictionInformationModule.calculateRisk(0.6205))

//        Assert.assertEquals(BigInteger.ZERO, predictionInformationModule.calculateRisk(0.0))
//        Assert.assertEquals(BigInteger.ZERO, predictionInformationModule.calculateRisk(null))

    }

}
