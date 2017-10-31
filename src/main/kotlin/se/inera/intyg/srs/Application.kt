package se.inera.intyg.srs

import org.apache.cxf.Bus
import org.apache.cxf.jaxws.EndpointImpl
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.web.support.SpringBootServletInitializer
import org.springframework.context.annotation.Bean
import se.inera.intyg.clinicalprocess.healthcond.srs.getconsent.v1.GetConsentResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.getdiagnosiscodes.v1.GetDiagnosisCodesResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.getpredictionquestions.v1.GetPredictionQuestionsResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.getriskpredictionforcertificate.v1.GetRiskPredictionForCertificateResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.GetSRSInformationResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.setconsent.v1.SetConsentResponderInterface
import se.riv.itintegration.monitoring.rivtabp21.v1.PingForConfigurationResponderInterface
import javax.xml.ws.Endpoint

@SpringBootApplication
class Application : SpringBootServletInitializer() {
    private val log = LogManager.getLogger()

    @Autowired
    lateinit var bus: Bus

    @Autowired
    lateinit var pingResponder: PingForConfigurationResponderInterface

    @Autowired
    lateinit var srsResponder: GetSRSInformationResponderInterface

    @Autowired
    lateinit var getRiskPredictionForCertificateResponder: GetRiskPredictionForCertificateResponderInterface

    @Autowired
    lateinit var getConsentResponder: GetConsentResponderInterface

    @Autowired
    lateinit var setConsentResponder: SetConsentResponderInterface

    @Autowired
    lateinit var predictionQuestionsResponder: GetPredictionQuestionsResponderInterface

    @Autowired
    lateinit var diagnosisCodesResponder: GetDiagnosisCodesResponderInterface

    override fun configure(application: SpringApplicationBuilder): SpringApplicationBuilder {
        return application.sources(Application::class.java)
    }

    @Bean
    fun srsInfoEndpoint(): Endpoint {
        val endpoint = EndpointImpl(bus, srsResponder)
        endpoint.schemaLocations = listOf("classpath:core_components/clinicalprocess_healthcond_certificate_types_2.0.xsd",
                "classpath:interactions/GetSRSInformation/GetSRSInformationResponder_1.0.xsd")
        endpoint.publish("/getsrs")
        return endpoint
    }

    @Bean
    fun getConsentEndpoint(): Endpoint {
        val endpoint = EndpointImpl(bus, getConsentResponder)
        endpoint.schemaLocations = listOf("classpath:core_components/clinicalprocess_healthcond_certificate_types_2.0.xsd",
                "classpath:interactions/GetConsent/GetConsentResponder_1.0.xsd")
        endpoint.publish("/get-consent")
        return endpoint
    }

    @Bean
    fun setConsentEndpoint(): Endpoint {
        val endpoint = EndpointImpl(bus, setConsentResponder)
        endpoint.schemaLocations = listOf("classpath:core_components/clinicalprocess_healthcond_certificate_types_2.0.xsd",
                "classpath:interactions/SetConsent/SetConsentResponder_1.0.xsd")
        endpoint.publish("/set-consent")
        return endpoint
    }

    @Bean
    fun riskPredictionForCertificateEndpoint(): Endpoint {
        val endpoint = EndpointImpl(bus, getRiskPredictionForCertificateResponder)
        endpoint.schemaLocations = listOf("classpath:core_components/clinicalprocess_healthcond_certificate_types_2.0.xsd",
                "classpath:interactions/GetRiskPredictionForCertificate/GetRiskPredictionForCertificateResponder_1.0.xsd")
        endpoint.publish("/get-risk-prediction-for-certificate")
        return endpoint
    }

    @Bean
    fun monitoringEndpoint(): Endpoint {
        val endpoint = EndpointImpl(bus, pingResponder)
        endpoint.publish("/ping-for-configuration")
        return endpoint
    }

    @Bean
    fun predictionQuestionsEndpoint(): Endpoint {
        val endpoint = EndpointImpl(bus, predictionQuestionsResponder)
        endpoint.publish("/predictionquestions")
        return endpoint
    }

    @Bean
    fun diagnosisCodesEndpoint(): Endpoint {
        val endpoint = EndpointImpl(bus, diagnosisCodesResponder)
        endpoint.publish("/diagnosiscodes")
        return endpoint
    }

}

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
