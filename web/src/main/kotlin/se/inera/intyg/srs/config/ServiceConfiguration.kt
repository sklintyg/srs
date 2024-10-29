package se.inera.intyg.srs

import org.apache.cxf.Bus
import org.apache.cxf.jaxws.EndpointImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import se.inera.intyg.clinicalprocess.healthcond.srs.getconsent.v1.GetConsentResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.getdiagnosiscodes.v1.GetDiagnosisCodesResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.getpredictionquestions.v1.GetPredictionQuestionsResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.getriskpredictionforcertificate.v1.GetRiskPredictionForCertificateResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v3.GetSRSInformationResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformationfordiagnosis.v1.GetSRSInformationForDiagnosisResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.setconsent.v1.SetConsentResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.setownopinion.v1.SetOwnOpinionResponderInterface
import se.riv.itintegration.monitoring.rivtabp21.v1.PingForConfigurationResponderInterface

@Configuration
open class ServiceConfiguration() {

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

    @Bean
    open fun getSrsEndpoint(): EndpointImpl {
        val endpoint = EndpointImpl(bus, srsResponder)
        endpoint.schemaLocations = listOf("classpath:core_components/clinicalprocess_healthcond_certificate_types_2.0.xsd",
                "classpath:core_components/clinicalprocess_healthcond_srs_1.0.xsd",
                "classpath:interactions/GetSRSInformation/GetSRSInformationResponder_3.0.xsd")
        // Use these to get full logging of input and output at the web service
        //endpoint.inInterceptors.add(LoggingInInterceptor())
        //endpoint.outInterceptors.add(LoggingOutInterceptor())
        endpoint.publish("/getsrs")
        return endpoint
    }

    @Bean
    open fun getSrsForDiagnosisEndpoint(): EndpointImpl {
        val endpoint = EndpointImpl(bus, srsForDiagnosisResponder)
        endpoint.schemaLocations = listOf("classpath:core_components/clinicalprocess_healthcond_certificate_types_2.0.xsd",
                "classpath:core_components/clinicalprocess_healthcond_srs_1.0.xsd",
                "classpath:interactions/GetSRSInformationForDiagnosis/GetSRSInformationForDiagnosisResponder_1.0.xsd")
        endpoint.publish("/getsrsfordiagnosis")
        return endpoint
    }

    @Bean
    open fun getConsentEndpoint(): EndpointImpl {
        val endpoint = EndpointImpl(bus, getConsentResponder)
        endpoint.schemaLocations = listOf("classpath:core_components/clinicalprocess_healthcond_certificate_types_2.0.xsd",
                "classpath:interactions/GetConsent/GetConsentResponder_1.0.xsd")
        endpoint.publish("/get-consent")
        return endpoint
    }

    @Bean
    open fun setConsentEndpoint(): EndpointImpl {
        val endpoint = EndpointImpl(bus, setConsentResponder)
        endpoint.schemaLocations = listOf("classpath:core_components/clinicalprocess_healthcond_certificate_types_2.0.xsd",
                "classpath:interactions/SetConsent/SetConsentResponder_1.0.xsd")
        endpoint.publish("/set-consent")
        return endpoint
    }

    @Bean
    open fun riskPredictionForCertificateEndpoint(): EndpointImpl {
        val endpoint = EndpointImpl(bus, getRiskPredictionForCertificateResponder)
        endpoint.schemaLocations = listOf("classpath:core_components/clinicalprocess_healthcond_certificate_types_2.0.xsd",
                "classpath:core_components/clinicalprocess_healthcond_srs_1.0.xsd",
                "classpath:interactions/GetRiskPredictionForCertificate/GetRiskPredictionForCertificateResponder_1.0.xsd")
        endpoint.publish("/get-risk-prediction-for-certificate/v1.0")
        return endpoint
    }

    @Bean
    open fun setOwnOpinionEndpoint(): EndpointImpl {
        val endpoint = EndpointImpl(bus, setOwnOpinionResponder)
        endpoint.schemaLocations = listOf("classpath:core_components/clinicalprocess_healthcond_certificate_types_2.0.xsd",
                "classpath:core_components/clinicalprocess_healthcond_srs_1.0.xsd",
                "classpath:interactions/SetOwnOpinion/SetOwnOpinionResponder_1.0.xsd")
        endpoint.publish("/set-own-opinion")
        return endpoint
    }

    @Bean
    open fun monitoringEndpoint(): EndpointImpl {
        val endpoint = EndpointImpl(bus, pingResponder)
        endpoint.publish("/ping-for-configuration")
        return endpoint
    }

    @Bean
    open fun predictionQuestionsEndpoint(): EndpointImpl {
        val endpoint = EndpointImpl(bus, predictionQuestionsResponder)
        endpoint.publish("/predictionquestions")
        return endpoint
    }

    @Bean
    open fun diagnosisCodesEndpoint(): EndpointImpl {
        val endpoint = EndpointImpl(bus, diagnosisCodesResponder)
        endpoint.publish("/diagnosiscodes")
        return endpoint
    }
}