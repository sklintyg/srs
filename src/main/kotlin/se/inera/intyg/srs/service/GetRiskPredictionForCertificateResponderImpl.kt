package se.inera.intyg.srs.service

import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import se.inera.intyg.clinicalprocess.healthcond.srs.getriskpredictionforcertificate.v1.GetRiskPredictionForCertificateRequestType
import se.inera.intyg.clinicalprocess.healthcond.srs.getriskpredictionforcertificate.v1.GetRiskPredictionForCertificateResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.getriskpredictionforcertificate.v1.GetRiskPredictionForCertificateResponseType
import se.inera.intyg.clinicalprocess.healthcond.srs.getriskpredictionforcertificate.v1.RiskPrediktion
import se.inera.intyg.clinicalprocess.healthcond.srs.getriskpredictionforcertificate.v1.Risksignal
import se.inera.intyg.srs.persistence.ProbabilityRepository
import se.inera.intyg.srs.util.PredictionInformationUtil
import java.math.BigInteger

@Service
@org.apache.cxf.annotations.SchemaValidation(type = org.apache.cxf.annotations.SchemaValidation.SchemaValidationType.BOTH)
class GetRiskPredictionForCertificateResponderImpl(@Autowired val probabilityRepository: ProbabilityRepository) :
        GetRiskPredictionForCertificateResponderInterface {
    private val log = LogManager.getLogger()

    override fun getRiskPredictionForCertificate(p0: GetRiskPredictionForCertificateRequestType?) :
            GetRiskPredictionForCertificateResponseType {

        val response = GetRiskPredictionForCertificateResponseType()

        if (p0?.intygsId?.size == 0) {
            return response
        }

        log.info("Getting riskprediction for certificate")

        p0?.intygsId?.forEach { intygId ->
            val probability = probabilityRepository.findByCertificateId(intygId).maxBy { it.timestamp }

            if (probability != null) {
                System.err.println(probability.timestamp)
                val rp = RiskPrediktion()
                rp.intygsId = probability.certificateId
                val riskSignal = Risksignal()
                riskSignal.riskkategori = BigInteger.valueOf(probability.riskCategory.toLong())
                riskSignal.beskrivning = PredictionInformationUtil.categoryDescriptions[riskSignal.riskkategori]

                rp.risksignal = riskSignal
                response.riskPrediktioner.add(rp)
            }
        }

        return response
    }
}
