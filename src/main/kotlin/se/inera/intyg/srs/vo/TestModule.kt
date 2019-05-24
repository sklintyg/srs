package se.inera.intyg.srs.vo

import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Atgardstyp
import se.inera.intyg.srs.controllers.TestController
import se.inera.intyg.srs.persistence.ConsentRepository
import se.inera.intyg.srs.persistence.DiagnosisRepository
import se.inera.intyg.srs.persistence.InternalStatistic
import se.inera.intyg.srs.persistence.Measure
import se.inera.intyg.srs.persistence.MeasurePriority
import se.inera.intyg.srs.persistence.MeasurePriorityRepository
import se.inera.intyg.srs.persistence.MeasureRepository
import se.inera.intyg.srs.persistence.PredictionDiagnosis
import se.inera.intyg.srs.persistence.PredictionPriority
import se.inera.intyg.srs.persistence.PredictionPriorityRepository
import se.inera.intyg.srs.persistence.PredictionQuestion
import se.inera.intyg.srs.persistence.PredictionResponse
import se.inera.intyg.srs.persistence.ProbabilityRepository
import se.inera.intyg.srs.persistence.QuestionRepository
import se.inera.intyg.srs.persistence.Recommendation
import se.inera.intyg.srs.persistence.RecommendationRepository
import se.inera.intyg.srs.persistence.ResponseRepository
import se.inera.intyg.srs.persistence.InternalStatisticRepository
import se.inera.intyg.srs.service.ModelFileUpdateService
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicLong

@Suppress("LeakingThis")
@Service
@Profile("it")
class TestModule(private val consentRepo: ConsentRepository,
                 private val measureRepo: MeasureRepository,
                 private val priorityRepo: MeasurePriorityRepository,
                 private val recommendationRepo: RecommendationRepository,
                 private val statisticsRepo: InternalStatisticRepository,
                 private val diagnosisRepo: DiagnosisRepository,
                 private val predictPrioRepo: PredictionPriorityRepository,
                 private val questionRepo: QuestionRepository,
                 private val responseRepo: ResponseRepository,
                 private val probabilityRepo: ProbabilityRepository,
                 private val modelFileUpdateService: ModelFileUpdateService,
                 private val resourceLoader: ResourceLoader,
                 @Value("\${resources.folder}") private val resourcesFolder: String) {

    private val log = LogManager.getLogger();
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

    fun createStatistic(diagnosisId: String, pictureUrl: String): InternalStatistic =
            statisticsRepo.save(InternalStatistic(diagnosisId, pictureUrl, LocalDateTime.now(), uniqueId.incrementAndGet()))

    fun createPredictionQuestion(request: TestController.DiagnosisRequest): PredictionDiagnosis =
        diagnosisRepo.save(PredictionDiagnosis(uniqueId.incrementAndGet(),
                request.diagnosisId, request.prevalence, mapToPredictions(request.questions)))

    private fun mapToPredictions(questions: List<TestController.PredictionQuestion>): List<PredictionPriority> =
        questions
                .mapIndexed { i, question ->
                    PredictionPriority(i + 1, mapToQuestion(question), uniqueId.incrementAndGet()) }
                .map { predictPrioRepo.save(it) }

    private fun mapToQuestion(question: TestController.PredictionQuestion): PredictionQuestion =
            questionRepo.save(PredictionQuestion(
                    uniqueId.incrementAndGet(), question.question, question.helpText,
                    question.predictionId, mapToResponses(question.responses)))

    private fun mapToResponses(responses: Collection<TestController.PredictionResponse>) =
        responses
                .mapIndexed { i, (answer, predictionId, default) ->
                    PredictionResponse(uniqueId.incrementAndGet(), answer, predictionId, default, i + 1) }
                .map { responseRepo.save(it) }

    fun deleteMeasure(diagnosisId: String) = measureRepo.deleteAll(measureRepo.findByDiagnosisIdStartingWith(diagnosisId))

    fun deleteAllConsents() = consentRepo.deleteAll()

    fun deleteAllMeasures() = measureRepo.deleteAll()

    fun deleteAllRecommendations() = recommendationRepo.deleteAll()

    fun deleteAllPriorities() = priorityRepo.deleteAll()

    fun deleteAllStatistics() = statisticsRepo.deleteAll()

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
