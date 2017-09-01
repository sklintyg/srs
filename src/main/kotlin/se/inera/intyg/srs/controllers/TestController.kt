package se.inera.intyg.srs.controllers


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.*
import se.inera.intyg.srs.vo.ConsentModule
import se.inera.intyg.srs.vo.MeasureInformationModule
import se.inera.intyg.srs.vo.TestModule

@RestController
@Profile("it")
class TestController(@Autowired val consentModule: ConsentModule,
                     @Autowired val measureModule: MeasureInformationModule,
                     @Autowired val testModule: TestModule) {

    data class ConsentRequest(val personnummer: String,
                       val samtycke: Boolean,
                       val vardenhet: String)

    data class MeasureRequest(val diagnosId: String, val diagnosText: String, val rekommendationer: List<String>)

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
    fun deleteAllMeasures() = testModule.deleteAllMeasures()
}