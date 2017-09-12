package se.inera.intyg.srs.controllers


import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import se.inera.intyg.srs.service.ModelFileUpdateService
import se.inera.intyg.srs.vo.ConsentModule
import se.inera.intyg.srs.vo.MeasureInformationModule
import se.inera.intyg.srs.vo.TestModule
import java.util.concurrent.CompletableFuture

@RestController
@Profile("it")
class TestController(val consentModule: ConsentModule,
                     val measureModule: MeasureInformationModule,
                     val testModule: TestModule,
                     val fileService: ModelFileUpdateService) {

    data class ConsentRequest(val personnummer: String,
                              val samtycke: Boolean,
                              val vardenhet: String)

    data class MeasureRequest(val diagnosId: String, val diagnosText: String, val rekommendationer: List<String>)

    data class StatisticsRequest(val diagnosId: String, val bildUrl: String)

    data class PredictionQuestion(val question: String, val predictionId: String, val helpText: String, val responses: Collection<PredictionResponse>)

    data class PredictionResponse(val answer: String, val predictionId: String, val default: Boolean)

    data class DiagnosisRequest(val diagnosisId: String, val prevalence: Double, val questions: List<PredictionQuestion>)

    data class ModelRequest(val x99v0: Boolean, val x99v1: Boolean, val z99v0: Boolean, val z99v1: Boolean)

    @PostMapping("/diagnosis")
    fun createDiagnosis(@RequestBody request: DiagnosisRequest) =
            testModule.createPredictionQuestion(request)

    @DeleteMapping("/diagnosis")
    fun deleteAllPredictionQuestions() = testModule.deleteAllPredictionQuestions()

    @GetMapping("/consents")
    fun getConsent(@RequestParam(value = "personnummer") personnummer: String,
                   @RequestParam(value = "vardenhet") vardenhet: String) =
            consentModule.getConsent(personnummer, vardenhet)

    @PostMapping("/consents")
    fun setConsent(@RequestBody consent: ConsentRequest) =
            consentModule.setConsent(consent.personnummer, consent.samtycke, consent.vardenhet)

    @DeleteMapping("/consents")
    fun clearConsents() = testModule.deleteAllConsents()

    @PostMapping("/measures")
    fun createMeasure(@RequestBody measure: MeasureRequest) =
            testModule.createMeasure(measure.diagnosId, measure.diagnosText, measure.rekommendationer)

    @DeleteMapping("/measures/{id}")
    fun deleteMeasure(@PathVariable("id") diagnosisId: String) =
            testModule.deleteMeasure(diagnosisId)

    @GetMapping("/measures/{id}")
    fun getMeasure(@PathVariable("id") diagnosisId: String) =
            measureModule.measureRepo.findByDiagnosisIdStartingWith(diagnosisId)

    @DeleteMapping("/measures")
    fun deleteAllMeasures() {
        testModule.deleteAllPriorities()
        testModule.deleteAllRecommendations()
        testModule.deleteAllMeasures()
    }

    @DeleteMapping("/statistics")
    fun deleteAllStatistics() =
            testModule.deleteAllStatistics()

    @PostMapping("/statistics")
    fun createStatistics(@RequestBody statistics: StatisticsRequest) =
            testModule.createStatistic(statistics.diagnosId, statistics.bildUrl)

    @GetMapping("intyg/{id}")
    fun getIntyg(@RequestParam intygsId: String) =
            null//testModule.getIntyg(intygsId)

    @PostMapping("/await-model-update")
    fun forceModelUpdate() {
        val cf = CompletableFuture<Void>()
        fileService.listeners.add(cf)
        cf.join()
    }


    @PostMapping("/set-models")
    fun setTestModels(@RequestBody models: ModelRequest) =
            testModule.setTestModels(models)


}