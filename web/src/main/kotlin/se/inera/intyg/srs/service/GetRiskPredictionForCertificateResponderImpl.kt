package se.inera.intyg.srs.service

import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import riv.clinicalprocess.healthcond.srs.types._1.EgenBedomningRiskType

import se.inera.intyg.clinicalprocess.healthcond.srs.getriskpredictionforcertificate.v1.GetRiskPredictionForCertificateRequestType
import se.inera.intyg.clinicalprocess.healthcond.srs.getriskpredictionforcertificate.v1.GetRiskPredictionForCertificateResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.getriskpredictionforcertificate.v1.GetRiskPredictionForCertificateResponseType
import se.inera.intyg.clinicalprocess.healthcond.srs.getriskpredictionforcertificate.v1.RiskPrediktion
import se.inera.intyg.clinicalprocess.healthcond.srs.getriskpredictionforcertificate.v1.Risksignal
import se.inera.intyg.srs.persistence.repository.ProbabilityRepository
import se.inera.intyg.srs.util.PredictionInformationUtil

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

        log.debug("Getting riskprediction for certificates. Number of certificates to fetch: ${p0?.intygsId?.size}")

        // Gets all probabilities for all certificateIds first ordered by certificateId ASC and then internally by timestamp DESC
        // I.e. the first probability for each certificateId has the latest timestamp
        val probabilities = probabilityRepository.findByCertificateIdInAndOrderedByCertificateIdAndTimestamp(p0!!.intygsId)
        val addedCertificateIds = HashSet<String>()

        probabilities.forEach { probability ->
            val rp = RiskPrediktion()
            // We want to add each certificate just once, and we only want to add the first since that has the latest timestamp
            if (!addedCertificateIds.contains(probability.certificateId)) {
                addedCertificateIds.add(probability.certificateId)
                rp.intygsId = probability.certificateId
                val riskSignal = Risksignal()
                riskSignal.riskkategori = probability.riskCategory
                riskSignal.berakningstidpunkt = probability.timestamp
                riskSignal.beskrivning = PredictionInformationUtil.categoryDescriptions[riskSignal.riskkategori]
                if (probability.ownOpinion != null) {
                    riskSignal.lakarbedomningRisk = EgenBedomningRiskType.fromValue(probability.ownOpinion.opinion)
                }
                rp.risksignal = riskSignal
                response.riskPrediktioner.add(rp)
            }
        }

        return response
    }
}
