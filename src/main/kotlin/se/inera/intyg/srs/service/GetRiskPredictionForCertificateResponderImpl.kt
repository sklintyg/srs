package se.inera.intyg.srs.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import se.inera.intyg.clinicalprocess.healthcond.srs.getriskpredictionforcertificate.v1.GetRiskPredictionForCertificateRequestType
import se.inera.intyg.clinicalprocess.healthcond.srs.getriskpredictionforcertificate.v1.GetRiskPredictionForCertificateResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.getriskpredictionforcertificate.v1.GetRiskPredictionForCertificateResponseType
import se.inera.intyg.clinicalprocess.healthcond.srs.getriskpredictionforcertificate.v1.RiskPrediktion
import se.inera.intyg.clinicalprocess.healthcond.srs.getriskpredictionforcertificate.v1.Risksignal
import se.inera.intyg.srs.persistence.ProbabilityRepository
import java.math.BigInteger

@Service
@org.apache.cxf.annotations.SchemaValidation(type = org.apache.cxf.annotations.SchemaValidation.SchemaValidationType.BOTH)
class GetRiskPredictionForCertificateResponderImpl(@Autowired val probabilityRepository: ProbabilityRepository) : GetRiskPredictionForCertificateResponderInterface {
    override fun getRiskPredictionForCertificate(p0: GetRiskPredictionForCertificateRequestType?): GetRiskPredictionForCertificateResponseType {

        val response = GetRiskPredictionForCertificateResponseType()
        if (p0?.intygsId?.size == 0) {
            return response
        }

        p0?.intygsId?.forEach { intygId ->
            val probabilities = probabilityRepository.findByCertificateId(intygId)

            val optional = probabilities.stream().findFirst()
            if (optional.isPresent()) {
                val probability = optional.get()

                val rp = RiskPrediktion()
                rp.intygsId = probability.certificateId
                val riskSignal = Risksignal()
                riskSignal.riskkategori = BigInteger.valueOf(probability.riskCategory.toLong())
                riskSignal.beskrivning = "TODO this"

                rp.risksignal = riskSignal
                response.riskPrediktioner.add(rp)
            }
        }

        return response
    }
}
