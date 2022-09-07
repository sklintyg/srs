package se.inera.intyg.srs.controller

import org.springframework.context.annotation.Profile
import org.springframework.core.io.ResourceLoader
import org.springframework.http.MediaType
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

@RestController
@Profile("it")
class TestController(val consentModule: ConsentModule,
                     val measureModule: MeasureInformationModule,
                     val testModule: TestModule,
                     val resourceLoader: ResourceLoader,
                     val fileService: ModelFileUpdateService) {

    data class ConsentRequest(val personnummer: String,
                              val samtycke: Boolean,
                              val vardenhet: String)

    data class MeasureRequest(val diagnosId: String, val diagnosText: String, val rekommendationer: List<Pair<String,String>>)

    data class StatisticsRequest(val diagnosId: String,
                                 var dayIntervalMin: Int,
                                 var dayIntervalMaxExcl: Int,
                                 var intervalQuantity: Int,
                                 var accumulatedQuantity: Int)

    data class PredictionQuestion(val question: String, val predictionId: String, val helpText: String, val forSubdiags: Boolean,
                                  val responses: Collection<PredictionResponse>)

    data class PredictionResponse(val answer: String, val predictionId: String, val default: Boolean)

    data class DiagnosisRequest(val diagnosisId: String, val prevalence: Double, val forSubdiags: Boolean,
                                val questions: List<PredictionQuestion>, val modelVersion:String = "3.0")

    data class ModelRequest(val x99v0: Boolean, val x9900v0: Boolean, val x99v1: Boolean, val m797v3: Boolean)

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
            testModule.deleteAllNationalStatistics()

    @PostMapping("/statistics")
    fun createStatistics(@RequestBody statistics: StatisticsRequest) =
            testModule.createNationalStatistic(statistics.diagnosId, statistics.dayIntervalMin, statistics.dayIntervalMaxExcl,
                    statistics.intervalQuantity, statistics.accumulatedQuantity)

    @GetMapping("intyg/{id}")
    fun getIntyg(@PathVariable("id") intygsId: String) =
            testModule.getIntyg(intygsId)

    @DeleteMapping("intyg")
    fun deleteAllIntyg() =
            testModule.deleteAllIntyg()

    @PostMapping("/set-models")
    fun setTestModels(@RequestBody models: ModelRequest) =
            testModule.setTestModels(models)

    // any resource
    @GetMapping("/resource", produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    fun resource(@RequestParam location: String) =
        resourceLoader.getResource(location)

}