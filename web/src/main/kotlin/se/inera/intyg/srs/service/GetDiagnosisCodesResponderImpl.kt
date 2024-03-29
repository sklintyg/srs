package se.inera.intyg.srs.service

import org.apache.cxf.annotations.SchemaValidation
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import se.inera.intyg.clinicalprocess.healthcond.srs.getdiagnosiscodes.v1.Diagnos
import se.inera.intyg.clinicalprocess.healthcond.srs.getdiagnosiscodes.v1.GetDiagnosisCodesRequestType
import se.inera.intyg.clinicalprocess.healthcond.srs.getdiagnosiscodes.v1.GetDiagnosisCodesResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.getdiagnosiscodes.v1.GetDiagnosisCodesResponseType
import se.inera.intyg.srs.persistence.repository.DiagnosisRepository

val CODE_SYSTEM = "1.2.752.116.1.1.1.1.3"

@Service
@SchemaValidation(type = SchemaValidation.SchemaValidationType.BOTH)
class GetDiagnosisCodesResponderImpl(
    val diagnosisRepo: DiagnosisRepository,
    @Value("\${model.currentVersion}") val currentModelVersion: String
) : GetDiagnosisCodesResponderInterface {
    override fun getDiagnosisCodes(request: GetDiagnosisCodesRequestType): GetDiagnosisCodesResponseType {
        val response = GetDiagnosisCodesResponseType()
        val predictionModelVersion = request.prediktionsmodellversion ?: currentModelVersion
        response.diagnos.addAll(diagnosisRepo.findByModelVersion(predictionModelVersion).map { savedDiagnosis ->
            val outgoingDiagnosis = Diagnos()
            outgoingDiagnosis.codeSystem = CODE_SYSTEM
            outgoingDiagnosis.code = savedDiagnosis.diagnosisId
            outgoingDiagnosis
        })
        response.prediktionsmodellversion = predictionModelVersion
        return response
    }
}
