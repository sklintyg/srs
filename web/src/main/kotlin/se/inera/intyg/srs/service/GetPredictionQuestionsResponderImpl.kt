package se.inera.intyg.srs.service

import org.apache.cxf.annotations.SchemaValidation
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import se.inera.intyg.clinicalprocess.healthcond.srs.getpredictionquestions.v1.GetPredictionQuestionsRequestType
import se.inera.intyg.clinicalprocess.healthcond.srs.getpredictionquestions.v1.GetPredictionQuestionsResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.getpredictionquestions.v1.GetPredictionQuestionsResponseType
import se.inera.intyg.clinicalprocess.healthcond.srs.getpredictionquestions.v1.Prediktionsfraga
import se.inera.intyg.clinicalprocess.healthcond.srs.getpredictionquestions.v1.Svarsalternativ
import java.math.BigInteger

@Service
@SchemaValidation(type = SchemaValidation.SchemaValidationType.BOTH)
class GetPredictionQuestionsResponderImpl(
    @Autowired val diagnosisService: DiagnosisServiceImpl,
    @Value("\${model.currentVersion}") val currentModelVersion: String
) : GetPredictionQuestionsResponderInterface {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun getPredictionQuestions(request: GetPredictionQuestionsRequestType): GetPredictionQuestionsResponseType {
        val response = GetPredictionQuestionsResponseType()
//      val predictionModelVersion = request.prediktionsmodellversion ?: currentModelVersion
        val predictionModelVersion = currentModelVersion
        log.debug("getting prediction questions for ${request.diagnos.code} with and model version ${predictionModelVersion}")
        val diagnosis = diagnosisService.getModelForDiagnosis(request.diagnos.code, predictionModelVersion) ?: return response

        if (log.isDebugEnabled) {
            log.debug("getPredictionQuestions diagnos:${request.diagnos.code}, diagnosisis: $diagnosis")
        }

        diagnosis.questions
            .filter { q -> // only return those that questions that aren't answered automatically using diagnosis code
                // check if we have at least one answer that don't have an automatic selection,
                // All answers to automatic questions must have such a value so this comparison is quicker than the other way around
                q.question.answers.find { a -> a.automaticSelectionDiagnosisCode.isNullOrBlank() } != null
            }
            .forEach { savedQuestion ->
                log.debug("Adding question to response, savedQuestion: $savedQuestion")
                val outboundQuestion = Prediktionsfraga()
                outboundQuestion.frageid = BigInteger.valueOf(savedQuestion.id)
                outboundQuestion.frageidSrs = savedQuestion.question.predictionId
                outboundQuestion.fragetext = savedQuestion.question.question
                outboundQuestion.hjalptext = savedQuestion.question.helpText
                outboundQuestion.prioritet = BigInteger.valueOf(savedQuestion.priority.toLong())
//                outboundQuestion.prediktionsmodellversion = predictionModelVersion
                savedQuestion.question.answers
                        .forEach { savedResponse ->
                            log.debug("Adding answer to response, savedQuestion: $savedResponse")
                            val outboundResponse = Svarsalternativ()
                            outboundResponse.svarsid = BigInteger.valueOf(savedResponse.id)
                            outboundResponse.svarsidSrs = savedResponse.predictionId
                            outboundResponse.isDefault = savedResponse.isDefault
                            if (savedResponse.priority != null) {
                                outboundResponse.prioritet = BigInteger.valueOf(savedResponse.priority.toLong())
                            }
                            outboundResponse.svarstext = savedResponse.answer
                            outboundQuestion.svarsalternativ.add(outboundResponse)
                        }
                response.prediktionsfraga.add(outboundQuestion)
            }
        return response
    }
}
