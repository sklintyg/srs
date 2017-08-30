package se.inera.intyg.srs.controllers


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.*
import se.inera.intyg.srs.vo.ConsentModule

@RestController
@Profile("it")
class TestController(@Autowired val consentModule : ConsentModule) {

    data class Consent(val personnummer: String, val samtycke: Boolean, val vardenhet: String)

    @GetMapping("/consents")
    @ResponseBody
    fun getConsent(@RequestParam(value = "personnummer") personnummer: String,
                   @RequestParam(value = "vardenhet") vardenhet: String) =
        consentModule.getConsent(personnummer, vardenhet)

    @PostMapping("/consents")
    @ResponseBody
    fun setConsent(@RequestBody consent: Consent) = consentModule.setConsent(consent.personnummer, consent.samtycke, consent.vardenhet)

}