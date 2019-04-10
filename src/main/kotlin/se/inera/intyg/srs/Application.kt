package se.inera.intyg.srs

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.apache.cxf.Bus
import org.apache.cxf.jaxws.EndpointImpl
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.context.annotation.Bean
import se.inera.intyg.clinicalprocess.healthcond.srs.getconsent.v1.GetConsentResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.getdiagnosiscodes.v1.GetDiagnosisCodesResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.getpredictionquestions.v1.GetPredictionQuestionsResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.getriskpredictionforcertificate.v1.GetRiskPredictionForCertificateResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.GetSRSInformationResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformationfordiagnosis.v1.GetSRSInformationForDiagnosisResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.setconsent.v1.SetConsentResponderInterface
import se.riv.itintegration.monitoring.rivtabp21.v1.PingForConfigurationResponderInterface
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
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
    lateinit var srsForDiagnosisResponder: GetSRSInformationForDiagnosisResponderInterface

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
    fun getSrsEndpoint(): Endpoint {
        val endpoint = EndpointImpl(bus, srsResponder)
        endpoint.schemaLocations = listOf("classpath:core_components/clinicalprocess_healthcond_certificate_types_2.0.xsd",
                "classpath:interactions/GetSRSInformation/GetSRSInformationResponder_1.0.xsd")
        endpoint.publish("/getsrs")
        return endpoint
    }

    @Bean
    fun getSrsForDiagnosisEndpoint(): Endpoint {
        val endpoint = EndpointImpl(bus, srsForDiagnosisResponder)
        endpoint.schemaLocations = listOf("classpath:core_components/clinicalprocess_healthcond_certificate_types_2.0.xsd",
                "classpath:interactions/GetSRSInformationForDiagnosis/GetSRSInformationForDiagnosisResponder_1.0.xsd")
        endpoint.publish("/getsrsfordiagnosis")
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
        endpoint.publish("/get-risk-prediction-for-certificate/v1.0")
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


    @Bean
    fun objectMapper(): ObjectMapper {
        val m = ObjectMapper()
        m.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        m.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        m.registerModule(Jdk8Module());
        m.registerModule(KotlinModule())
        m.registerModule(TemporalSerializer())
        m.setDateFormat(SimpleDateFormat("yyyy-MM-dd"))
        return m
    }

}

private class TemporalSerializer: SimpleModule() {
    init {
        addSerializer(LocalDateTime::class.java, LocalDateTimeSerializer.INSTANCE)
        addDeserializer(LocalDateTime::class.java, LocalDateTimeDeserializer.INSTANCE)

        addSerializer(LocalDate::class.java, LocalDateSerializer.INSTANCE)
        addDeserializer(LocalDate::class.java, LocalDateDeserializer.INSTANCE)

        addSerializer(LocalTime::class.java, LocalTimeSerializer.INSTANCE)
        addDeserializer(LocalTime::class.java, LocalTimeDeserializer.INSTANCE)
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
