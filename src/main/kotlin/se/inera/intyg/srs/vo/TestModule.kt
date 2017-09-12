package se.inera.intyg.srs.vo

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
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
import se.inera.intyg.srs.persistence.QuestionRepository
import se.inera.intyg.srs.persistence.Recommendation
import se.inera.intyg.srs.persistence.RecommendationRepository
import se.inera.intyg.srs.persistence.ResponseRepository
import se.inera.intyg.srs.persistence.StatisticRepository
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
                 private val statisticsRepo: StatisticRepository,
                 private val diagnosisRepo: DiagnosisRepository,
                 private val predictPrioRepo: PredictionPriorityRepository,
                 private val questionRepo: QuestionRepository,
                 private val responseRepo: ResponseRepository,
                 @Value("\${model.dir}") private val modelDir: String) {

    private val uniqueId = AtomicLong(1000)

    private val testModels = mapOf(
            Pair("x99v0",   Pair("$modelDir/../testmodel/Model1.RData", "$modelDir/PM_X99_v0.0.RData")),
            Pair("x9900v0", Pair("$modelDir/../testmodel/Model2.RData", "$modelDir/PM_X9900_v0.0.RData")),
            Pair("x99v1",   Pair("$modelDir/../testmodel/Model2.RData", "$modelDir/PM_X99_v1.0.RData"))
    )

    fun createMeasure(diagnosisId: String, diagnosisText: String, recommendations: List<String>): Measure =
            measureRepo.save(Measure(uniqueId.incrementAndGet(), diagnosisId, diagnosisText, "1.0", mapToMeasurePriorities(recommendations)))

    private fun mapToMeasurePriorities(recommendations: List<String>) =
            recommendations
                    .map { recText -> Recommendation(uniqueId.incrementAndGet(), recText) }
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

    fun deleteAllPredictionQuestions() {
        responseRepo.deleteAll()
        predictPrioRepo.deleteAll()
        questionRepo.deleteAll()
        diagnosisRepo.deleteAll()
    }

    fun setTestModels(models: TestController.ModelRequest) {
        Files.walk(Paths.get(modelDir))
                .filter { it.getName(it.nameCount - 1).toString().contains("X99") }
                .forEach { Files.delete(it) }

        val copyPaths = mutableListOf<Pair<String, String>>()

        if (models.x99v0) testModels["x99v0"]?.let { copyPaths.add(it) }
        if (models.x99v0) testModels["x9900v0"]?.let { copyPaths.add(it) }
        if (models.x99v1) testModels["x99v1"]?.let { copyPaths.add(it) }

        copyPaths.forEach { (from, to) -> Files.copy(Paths.get(from), Paths.get(to), StandardCopyOption.REPLACE_EXISTING) }
    }
}