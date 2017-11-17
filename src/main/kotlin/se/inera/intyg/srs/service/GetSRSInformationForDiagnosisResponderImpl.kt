package se.inera.intyg.srs.service

import org.apache.cxf.annotations.SchemaValidation
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Service
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformationfordiagnosis.v1.GetSRSInformationForDiagnosisRequestType
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformationfordiagnosis.v1.GetSRSInformationForDiagnosisResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformationfordiagnosis.v1.GetSRSInformationForDiagnosisResponseType
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Statistik
import se.inera.intyg.srs.vo.MeasureInformationModule
import se.inera.intyg.srs.vo.StatisticModule
import se.riv.clinicalprocess.healthcond.certificate.types.v2.ResultCodeEnum

@Service
@SchemaValidation(type = SchemaValidation.SchemaValidationType.BOTH)
class GetSRSInformationForDiagnosisResponderImpl(val measureModule: MeasureInformationModule,
                                                 val statisticModule: StatisticModule) : GetSRSInformationForDiagnosisResponderInterface {

    private val log = LogManager.getLogger()

    override fun getSRSInformationForDiagnosis(request: GetSRSInformationForDiagnosisRequestType): GetSRSInformationForDiagnosisResponseType {
        log.info("Received request for diagnosis ${request.diagnos.code}")

        val response = GetSRSInformationForDiagnosisResponseType()

        // Åtgärder
        try {
            response.atgardsrekommendation = measureModule.getInfoForDiagnosis(request.diagnos.code)
        } catch (e: Exception) {
            log.error("Statistics could not be produced for diagnosis ${request.diagnos.code}. Please check for error.", e)
        }

        // Statistik
        try {
            val statistikDto = Statistik()
            statistikDto.statistikbild.add(statisticModule.getInfoForDiagnosis(request.diagnos.code))
            response.statistik = statistikDto
        } catch (e: Exception) {
            log.error("Measures could not be produced for diagnosis ${request.diagnos.code}. Please check for error.", e)
        }

        response.resultCode = ResultCodeEnum.OK
        return response
    }
}