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
import org.apache.cxf.interceptor.LoggingInInterceptor
import org.apache.cxf.interceptor.LoggingOutInterceptor
import org.apache.cxf.jaxws.EndpointImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import se.inera.intyg.clinicalprocess.healthcond.srs.getconsent.v1.GetConsentResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.getdiagnosiscodes.v1.GetDiagnosisCodesResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.getpredictionquestions.v1.GetPredictionQuestionsResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.getriskpredictionforcertificate.v1.GetRiskPredictionForCertificateResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v2.GetSRSInformationResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformationfordiagnosis.v1.GetSRSInformationForDiagnosisResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.setconsent.v1.SetConsentResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.setownopinion.v1.SetOwnOpinionResponderInterface
import se.riv.itintegration.monitoring.rivtabp21.v1.PingForConfigurationResponderInterface
import java.text.SimpleDateFormat
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.xml.ws.Endpoint

@SpringBootApplication
open class Application : SpringBootServletInitializer() {

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

    @Autowired
    lateinit var setOwnOpinionResponder: SetOwnOpinionResponderInterface

    override fun configure(application: SpringApplicationBuilder): SpringApplicationBuilder {
        return application.sources(Application::class.java)
    }

    @Bean
    @Profile("!it")
    open fun normalClock(): Clock {
        return Clock.systemDefaultZone();
    }

    @Bean
    @Profile("it")
    open fun integrationTestClock(): Clock {
        return Clock.fixed(
                ZonedDateTime.of(2020, 1, 31, 23,59,0,0,
                        ZoneId.systemDefault()).toInstant(),
                ZoneId.systemDefault())
    }

    @Bean
    open fun getSrsEndpoint(): Endpoint {
        val endpoint = EndpointImpl(bus, srsResponder)
        endpoint.schemaLocations = listOf("classpath:core_components/clinicalprocess_healthcond_certificate_types_2.0.xsd",
                "classpath:core_components/clinicalprocess_healthcond_srs_1.0.xsd",
                "classpath:interactions/GetSRSInformation/GetSRSInformationResponder_2.0.xsd")
        // Use these to get full loging of input and output at the web service
        //endpoint.inInterceptors.add(LoggingInInterceptor())
        //endpoint.outInterceptors.add(LoggingOutInterceptor())
        endpoint.publish("/getsrs")
        return endpoint
    }

    @Bean
    open fun getSrsForDiagnosisEndpoint(): Endpoint {
        val endpoint = EndpointImpl(bus, srsForDiagnosisResponder)
        endpoint.schemaLocations = listOf("classpath:core_components/clinicalprocess_healthcond_certificate_types_2.0.xsd",
                "classpath:core_components/clinicalprocess_healthcond_srs_1.0.xsd",
                "classpath:interactions/GetSRSInformationForDiagnosis/GetSRSInformationForDiagnosisResponder_1.0.xsd")
        endpoint.publish("/getsrsfordiagnosis")
        return endpoint
    }

    @Bean
    open fun getConsentEndpoint(): Endpoint {
        val endpoint = EndpointImpl(bus, getConsentResponder)
        endpoint.schemaLocations = listOf("classpath:core_components/clinicalprocess_healthcond_certificate_types_2.0.xsd",
                "classpath:interactions/GetConsent/GetConsentResponder_1.0.xsd")
        endpoint.publish("/get-consent")
        return endpoint
    }

    @Bean
    open fun setConsentEndpoint(): Endpoint {
        val endpoint = EndpointImpl(bus, setConsentResponder)
        endpoint.schemaLocations = listOf("classpath:core_components/clinicalprocess_healthcond_certificate_types_2.0.xsd",
                "classpath:interactions/SetConsent/SetConsentResponder_1.0.xsd")
        endpoint.publish("/set-consent")
        return endpoint
    }

    @Bean
    open fun riskPredictionForCertificateEndpoint(): Endpoint {
        val endpoint = EndpointImpl(bus, getRiskPredictionForCertificateResponder)
        endpoint.schemaLocations = listOf("classpath:core_components/clinicalprocess_healthcond_certificate_types_2.0.xsd",
                "classpath:core_components/clinicalprocess_healthcond_srs_1.0.xsd",
                "classpath:interactions/GetRiskPredictionForCertificate/GetRiskPredictionForCertificateResponder_1.0.xsd")
        endpoint.publish("/get-risk-prediction-for-certificate/v1.0")
        return endpoint
    }

    @Bean
    open fun setOwnOpinionEndpoint(): Endpoint {
        val endpoint = EndpointImpl(bus, setOwnOpinionResponder)
        endpoint.schemaLocations = listOf("classpath:core_components/clinicalprocess_healthcond_certificate_types_2.0.xsd",
                "classpath:core_components/clinicalprocess_healthcond_srs_1.0.xsd",
                "classpath:interactions/SetOwnOpinion/SetOwnOpinionResponder_1.0.xsd")
        endpoint.publish("/set-own-opinion")
        return endpoint
    }

    @Bean
    open fun monitoringEndpoint(): Endpoint {
        val endpoint = EndpointImpl(bus, pingResponder)
        endpoint.publish("/ping-for-configuration")
        return endpoint
    }

    @Bean
    open fun predictionQuestionsEndpoint(): Endpoint {
        val endpoint = EndpointImpl(bus, predictionQuestionsResponder)
        endpoint.publish("/predictionquestions")
        return endpoint
    }

    @Bean
    open fun diagnosisCodesEndpoint(): Endpoint {
        val endpoint = EndpointImpl(bus, diagnosisCodesResponder)
        endpoint.publish("/diagnosiscodes")
        return endpoint
    }


    @Bean
    open fun objectMapper(): ObjectMapper {
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
