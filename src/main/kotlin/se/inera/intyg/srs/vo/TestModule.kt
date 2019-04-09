package se.inera.intyg.srs.vo

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Atgardstyp
import se.inera.intyg.srs.controllers.TestController
import se.inera.intyg.srs.persistence.*
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
                 private val statisticsRepo: StatisticRepository,
                 private val diagnosisRepo: DiagnosisRepository,
                 private val predictPrioRepo: PredictionPriorityRepository,
                 private val questionRepo: QuestionRepository,
                 private val responseRepo: ResponseRepository,
                 private val probabilityRepo: ProbabilityRepository,
                 private val modelFileUpdateService: ModelFileUpdateService,
                 private val resourceLoader: ResourceLoader,
                 @Value("\${resources.folder}") private val resourcesFolder: String) {

    private val uniqueId = AtomicLong(1000)

    fun createMeasure(diagnosisId: String, diagnosisText: String, recommendations: List<String>): Measure =
            measureRepo.save(Measure(uniqueId.incrementAndGet(), diagnosisId, diagnosisText, "1.0", mapToMeasurePriorities(recommendations)))

    private fun mapToMeasurePriorities(recommendations: List<String>) =
            recommendations
                    .map { recText -> Recommendation(uniqueId.incrementAndGet(), Atgardstyp.REK, recText) }
                    .map { rec -> recommendationRepo.save(rec) }
                    .mapIndexed { i, rec -> MeasurePriority(i + 1, rec) }
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

    fun deleteMeasure(diagnosisId: String) = measureRepo.delete(measureRepo.findByDiagnosisIdStartingWith(diagnosisId))

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

        val f = "$resourcesFolder/testmodel"
        if (models.x99v0) {
            resources.add(resourceLoader.getResource("$f/PM_X99_v0.0.RData"))
        }
        if (models.x99v1) {
            resources.add(resourceLoader.getResource("$f/PM_X99_v1.0.RData"))
        }
        if (models.x9900v0) {
            resources.add(resourceLoader.getResource("$f/PM_X9900_v0.0.RData"))
        }

        modelFileUpdateService.applyModels(resources)
    }

    fun getIntyg(intygsId: String) =
        probabilityRepo.findByCertificateId(intygsId)
}
