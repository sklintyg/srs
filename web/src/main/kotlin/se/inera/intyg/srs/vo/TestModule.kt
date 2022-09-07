package se.inera.intyg.srs.vo

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Atgardstyp
import se.inera.intyg.srs.controller.TestController
import se.inera.intyg.srs.persistence.repository.ConsentRepository
import se.inera.intyg.srs.persistence.repository.DiagnosisRepository
import se.inera.intyg.srs.persistence.entity.Measure
import se.inera.intyg.srs.persistence.entity.MeasurePriority
import se.inera.intyg.srs.persistence.repository.MeasurePriorityRepository
import se.inera.intyg.srs.persistence.repository.MeasureRepository
import se.inera.intyg.srs.persistence.entity.NationalStatistic
import se.inera.intyg.srs.persistence.repository.NationalStatisticRepository
import se.inera.intyg.srs.persistence.entity.PredictionDiagnosis
import se.inera.intyg.srs.persistence.entity.PredictionPriority
import se.inera.intyg.srs.persistence.repository.PredictionPriorityRepository
import se.inera.intyg.srs.persistence.entity.PredictionQuestion
import se.inera.intyg.srs.persistence.entity.PredictionResponse
import se.inera.intyg.srs.persistence.repository.ProbabilityRepository
import se.inera.intyg.srs.persistence.repository.QuestionRepository
import se.inera.intyg.srs.persistence.entity.Recommendation
import se.inera.intyg.srs.persistence.repository.RecommendationRepository
import se.inera.intyg.srs.persistence.repository.ResponseRepository
import se.inera.intyg.srs.service.ModelFileUpdateService
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicLong

@Suppress("LeakingThis")
@Service
@Profile("it")
class TestModule(private val consentRepo: ConsentRepository,
                 private val measureRepo: MeasureRepository,
                 private val priorityRepo: MeasurePriorityRepository,
                 private val recommendationRepo: RecommendationRepository,
                 private val nationalStatisticRepo: NationalStatisticRepository,
                 private val diagnosisRepo: DiagnosisRepository,
                 private val predictPrioRepo: PredictionPriorityRepository,
                 private val questionRepo: QuestionRepository,
                 private val responseRepo: ResponseRepository,
                 private val probabilityRepo: ProbabilityRepository,
                 private val modelFileUpdateService: ModelFileUpdateService,
                 private val resourceLoader: ResourceLoader,
                 @Value("\${resources.folder}") private val resourcesFolder: String) {

    private val log = LoggerFactory.getLogger(javaClass);
    private val uniqueId = AtomicLong(1000)

    fun createMeasure(diagnosisId: String, diagnosisText: String, recommendations: List<Pair<String,String>>): Measure {
        val m = measureRepo.save(Measure(diagnosisId, diagnosisText, "1.0"))
        mapToMeasurePriorities(recommendations, m)
        return m
    }

    private fun mapToMeasurePriorities(recommendations: List<Pair<String, String>>, m: Measure) =
            recommendations
                    .map { (recTitle, recText) -> Recommendation(Atgardstyp.REK, recTitle, recText, uniqueId.incrementAndGet()) }
                    .map { rec -> recommendationRepo.save(rec) }
                    .mapIndexed { i, rec ->
                        MeasurePriority(i + 1, rec, m)
                    }
                    .map { priority -> priorityRepo.save(priority) }
                    .toMutableList()

    fun createNationalStatistic(diagnosisId: String, dayIntervalMin: Int,
                                dayIntervalMaxExcl: Int, intervalQuantity: Int,
                                accumulatedQuantity: Int): NationalStatistic =
            nationalStatisticRepo.save(NationalStatistic(diagnosisId, dayIntervalMin, dayIntervalMaxExcl,
                    intervalQuantity, accumulatedQuantity, LocalDateTime.now()))

    fun createPredictionQuestion(request: TestController.DiagnosisRequest): PredictionDiagnosis =
        diagnosisRepo.save(PredictionDiagnosis(request.diagnosisId, request.prevalence, 3, request.modelVersion,
            request.forSubdiags,
            mapToPredictions(request.questions)))

    private fun mapToPredictions(questions: List<TestController.PredictionQuestion>): List<PredictionPriority> =
        questions
                .mapIndexed { i, question ->
                    PredictionPriority(i + 1, "TEST_1.0", question.forSubdiags, mapToQuestion(question))
                }
                .map { predictPrioRepo.save(it) }

    private fun mapToQuestion(question: TestController.PredictionQuestion): PredictionQuestion {
        var predictionQuestion:PredictionQuestion = questionRepo.save(PredictionQuestion(
                question.question, question.helpText,
                question.predictionId, "TEST_1.0", question.forSubdiags))
        predictionQuestion.answers = mapToResponses(question.responses, predictionQuestion)
        return predictionQuestion
    }

    private fun mapToResponses(responses: Collection<TestController.PredictionResponse>, predictionQuestion:PredictionQuestion) =
        responses
                .mapIndexed { i, (answer, predictionId, default) ->
                    PredictionResponse(answer, predictionId, default, i + 1,  "TEST_1.0",
                        predictionQuestion.forSubdiagnosis, predictionQuestion)
                }
                .map {
                    responseRepo.save(it)
                }

    fun deleteMeasure(diagnosisId: String) = measureRepo.deleteAll(measureRepo.findByDiagnosisIdStartingWith(diagnosisId))

    fun deleteAllConsents() = consentRepo.deleteAll()

    fun deleteAllMeasures() = measureRepo.deleteAll()

    fun deleteAllRecommendations() = recommendationRepo.deleteAll()

    fun deleteAllPriorities() = priorityRepo.deleteAll()

    fun deleteAllNationalStatistics() = nationalStatisticRepo.deleteAll()

    fun deleteAllIntyg() = probabilityRepo.deleteAll()

    fun deleteAllPredictionQuestions() {
        responseRepo.deleteAll()
        predictPrioRepo.deleteAll()
        questionRepo.deleteAll()
        diagnosisRepo.deleteAll()
    }

    fun setTestModels(models: TestController.ModelRequest) {
        val resources = mutableListOf<Resource>()

        fun load(loc: String) = resources.add(resourceLoader.getResource("$resourcesFolder/testmodel/$loc"))
        if (models.x99v0) {
            load("PM_X99_v0.0.rds")
        }
        if (models.x99v1) {
            load("PM_X99_v1.0.rds")
        }
        if (models.x9900v0) {
            load("PM_X9900_v0.0.rds")
        }

        modelFileUpdateService.applyModels(resources)
    }

    fun getIntyg(intygsId: String) =
        probabilityRepo.findByCertificateId(intygsId)
}
