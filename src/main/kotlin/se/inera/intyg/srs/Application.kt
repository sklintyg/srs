package se.inera.intyg.srs

import org.apache.cxf.Bus
import org.apache.cxf.jaxws.EndpointImpl
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.web.support.SpringBootServletInitializer
import org.springframework.context.annotation.Bean
import se.inera.intyg.clinicalprocess.healthcond.srs.getconsent.v1.GetConsentResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.getpredictionquestions.v1.GetPredictionQuestionsResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.GetSRSInformationResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.setconsent.v1.SetConsentResponderInterface
import se.inera.intyg.srs.persistence.DiagnosisRepository
import se.inera.intyg.srs.persistence.Measure
import se.inera.intyg.srs.persistence.MeasureRepository
import se.inera.intyg.srs.persistence.PredictionDiagnosis
import se.inera.intyg.srs.persistence.PredictionPriority
import se.inera.intyg.srs.persistence.PredictionPriorityRepository
import se.inera.intyg.srs.persistence.PredictionQuestion
import se.inera.intyg.srs.persistence.PredictionResponse
import se.inera.intyg.srs.persistence.MeasurePriority
import se.inera.intyg.srs.persistence.MeasurePriorityRepository
import se.inera.intyg.srs.persistence.QuestionRepository
import se.inera.intyg.srs.persistence.Recommendation
import se.inera.intyg.srs.persistence.RecommendationRepository
import se.inera.intyg.srs.persistence.ResponseRepository
import se.inera.intyg.srs.persistence.StatisticRepository
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
    lateinit var getConsentResponder: GetConsentResponderInterface

    @Autowired
    lateinit var setConsentResponder: SetConsentResponderInterface

    @Autowired
    lateinit var predictionQuestionsResponder: GetPredictionQuestionsResponderInterface

    override fun configure(application: SpringApplicationBuilder): SpringApplicationBuilder {
        return application.sources(Application::class.java)
    }

    @Bean
    fun endpoint(): Endpoint {
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
    fun init(measureRepo: MeasureRepository, recommendationRepo: RecommendationRepository,
             prioRepo: MeasurePriorityRepository, statisticRepo: StatisticRepository, responseRepo: ResponseRepository,
             questionRepo: QuestionRepository, diagnosisRepo: DiagnosisRepository, predictPrioRepo: PredictionPriorityRepository) = CommandLineRunner {

        val recommendation01 = recommendationRepo.save(Recommendation(1, "patienten bör överväga att kontakta företagshälsovård och arbetsgivare för att avgränsa eller byta arbetsuppgifter, eller t.o.m. byta yrke eller arbetsplats"))
        val recommendation02 = recommendationRepo.save(Recommendation(2, "remiss till behandling med psykoterapeutiska metoder"))
        val recommendation03 = recommendationRepo.save(Recommendation(3, "ge patienten lättillgänglig information om diagnosen och behandlingsmöjligheter"))
        val recommendation04 = recommendationRepo.save(Recommendation(4, "patienten bör överväga att kontakta företagshälsovård och arbetsgivare för att undersöka möjligheter till ergonomisk rådgivning och arbetsanpassning."))
        val recommendation05 = recommendationRepo.save(Recommendation(5, "förmedling av kontakt med fysioterapeut"))
        val recommendation06 = recommendationRepo.save(Recommendation(6, "FaR med konditions- och styrketräning"))
        val recommendation07 = recommendationRepo.save(Recommendation(7, "remiss till behandling med KBT"))
        val recommendation08 = recommendationRepo.save(Recommendation(8, "remiss till behandling med rTMS"))
        val recommendation09 = recommendationRepo.save(Recommendation(9, "Remiss till Internetförmedlad KBT via Internetbaserat stöd och behandling"))
        val recommendation10 = recommendationRepo.save(Recommendation(10, "SSRI-läkemedel"))
        val recommendation11 = recommendationRepo.save(Recommendation(11, "partiell sjukskrivning"))
        val recommendation12 = recommendationRepo.save(Recommendation(12, "FaR med regelbunden styrketräning för att förebygger nya besvär"))

        measureRepo.save(Measure(1, "F43.8A", "Utmattningssyndrom", "1.0",
                listOf(prioRepo.save(MeasurePriority(1, recommendation01)),
                        prioRepo.save(MeasurePriority(2, recommendation02)),
                        prioRepo.save(MeasurePriority(3, recommendation03)))))

        measureRepo.save(Measure(2, "M75", "Sjukdomstillstånd i skulderled", "1.0",
                listOf(prioRepo.save(MeasurePriority(1, recommendation04)),
                        prioRepo.save(MeasurePriority(2, recommendation05)))))

        measureRepo.save(Measure(3, "F32", "Depressiv episod", "1.0",
                listOf(prioRepo.save(MeasurePriority(1, recommendation06)),
                        prioRepo.save(MeasurePriority(2, recommendation07)),
                        prioRepo.save(MeasurePriority(3, recommendation08)))))

        measureRepo.save(Measure(4, "F41", "Andra ångestsyndrom", "1.0",
                listOf(prioRepo.save(MeasurePriority(1, recommendation07)),
                        prioRepo.save(MeasurePriority(2, recommendation09)),
                        prioRepo.save(MeasurePriority(3, recommendation10)))))

        measureRepo.save(Measure(5, "M54", "Ryggvärk", "1.0",
                listOf(prioRepo.save(MeasurePriority(1, recommendation11)),
                        prioRepo.save(MeasurePriority(2, recommendation12)))))

        val question01 = questionRepo.save(PredictionQuestion(1, "Sysselsättningsstatus", "Vilken är din nuvarande sysselsättning?", "SA_SyssStart_fct",
                listOf(responseRepo.save(PredictionResponse(1, "Yrkesarbetar", "work", true, 1)), responseRepo.save(PredictionResponse(2, "Studerar", "study", false, 2)))))

        diagnosisRepo.save(PredictionDiagnosis(1, "F23", listOf(predictPrioRepo.save(PredictionPriority(1, question01)))))
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
