package se.inera.intyg.srs.service

import org.apache.cxf.annotations.SchemaValidation
import org.springframework.stereotype.Service
import se.inera.intyg.clinicalprocess.healthcond.srs.getpredictionquestions.v1.GetPredictionQuestionsRequestType
import se.inera.intyg.clinicalprocess.healthcond.srs.getpredictionquestions.v1.GetPredictionQuestionsResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.getpredictionquestions.v1.GetPredictionQuestionsResponseType
import se.inera.intyg.clinicalprocess.healthcond.srs.getpredictionquestions.v1.Prediktionsfraga
import se.inera.intyg.clinicalprocess.healthcond.srs.getpredictionquestions.v1.Svarsalternativ
import se.inera.intyg.srs.persistence.DiagnosisRepository
import se.inera.intyg.srs.persistence.PredictionDiagnosis
import java.math.BigInteger
import java.util.*

@Service
@SchemaValidation(type = SchemaValidation.SchemaValidationType.BOTH)
class GetPredictionQuestionsResponderImpl(val diagnosisRepo: DiagnosisRepository) : GetPredictionQuestionsResponderInterface {

    private val MAX_ID_POSITIONS: Int = 5
    private val MIN_ID_POSITIONS: Int = 3

    override fun getPredictionQuestions(request: GetPredictionQuestionsRequestType): GetPredictionQuestionsResponseType {
        val response = GetPredictionQuestionsResponseType()
        val diagnosis = getModelForDiagnosis(request.diagnos.code) ?: return response

        diagnosis.questions.forEach { savedQuestion ->
            val outboundQuestion = Prediktionsfraga()
            outboundQuestion.frageid = BigInteger.valueOf(savedQuestion.id)
            outboundQuestion.frageidSrs = savedQuestion.question.predictionId
            outboundQuestion.fragetext = savedQuestion.question.question
            outboundQuestion.hjalptext = savedQuestion.question.helpText
            outboundQuestion.prioritet = BigInteger.valueOf(savedQuestion.priority.toLong())
            savedQuestion.question.answers.forEach { savedResponse ->
                val outboundResponse = Svarsalternativ()
                outboundResponse.svarsid = BigInteger.valueOf(savedResponse.id)
                outboundResponse.svarsidSrs = savedResponse.predictionId
                outboundResponse.isDefault = savedResponse.isDefault
                outboundResponse.prioritet = BigInteger.valueOf(savedResponse.priority.toLong())
                outboundResponse.svarstext = savedResponse.answer
                outboundQuestion.svarsalternativ.add(outboundResponse)
            }
            response.prediktionsfraga.add(outboundQuestion)
        }
        return response
    }

    private fun getModelForDiagnosis(diagnosisId: String): PredictionDiagnosis? {
        var currentId = cleanDiagnosisCode(diagnosisId)

        if (currentId.length > MAX_ID_POSITIONS) {
            return null
        }

        while (currentId.length >= MIN_ID_POSITIONS) {
            val diagnosis = diagnosisRepo.findOneByDiagnosisId(currentId)
            if (diagnosis != null) {
                return diagnosis
            }
            currentId = currentId.substring(0, currentId.length - 1)
        }
        return null
    }

    private fun cleanDiagnosisCode(diagnosisId: String): String = diagnosisId.toUpperCase(Locale.ENGLISH).replace(".", "")
}
