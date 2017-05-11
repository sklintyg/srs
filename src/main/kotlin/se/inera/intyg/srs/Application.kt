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
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.GetSRSInformationResponderInterface
import se.inera.intyg.srs.persistence.Measure
import se.inera.intyg.srs.persistence.MeasureRepository
import se.inera.intyg.srs.persistence.Priority
import se.inera.intyg.srs.persistence.PriorityRepository
import se.inera.intyg.srs.persistence.Recommendation
import se.inera.intyg.srs.persistence.RecommendationRepository
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
    fun monitoringEndpoint(): Endpoint {
        val endpoint = EndpointImpl(bus, pingResponder)
        endpoint.publish("/ping-for-configuration")
        return endpoint
    }

    @Bean
    fun init(measureRepo: MeasureRepository, recommendationRepo: RecommendationRepository, priorityRepo: PriorityRepository) = CommandLineRunner {

        val recommendation01 = recommendationRepo.save(Recommendation("patienten bör överväga att kontakta företagshälsovård och arbetsgivare för att avgränsa eller byta arbetsuppgifter, eller t.o.m. byta yrke eller arbetsplats"))
        val recommendation02 = recommendationRepo.save(Recommendation("remiss till behandling med psykoterapeutiska metoder"))
        val recommendation03 = recommendationRepo.save(Recommendation("ge patienten lättillgänglig information om diagnosen och behandlingsmöjligheter"))
        val recommendation04 = recommendationRepo.save(Recommendation("patienten bör överväga att kontakta företagshälsovård och arbetsgivare för att undersöka möjligheter till ergonomisk rådgivning och arbetsanpassning."))
        val recommendation05 = recommendationRepo.save(Recommendation("förmedling av kontakt med fysioterapeut"))
        val recommendation06 = recommendationRepo.save(Recommendation("FaR med konditions- och styrketräning"))
        val recommendation07 = recommendationRepo.save(Recommendation("remiss till behandling med KBT"))
        val recommendation08 = recommendationRepo.save(Recommendation("remiss till behandling med rTMS"))
        val recommendation09 = recommendationRepo.save(Recommendation("Remiss till Internetförmedlad KBT via Internetbaserat stöd och behandling"))
        val recommendation10 = recommendationRepo.save(Recommendation("SSRI-läkemedel"))
        val recommendation11 = recommendationRepo.save(Recommendation("partiell sjukskrivning"))
        val recommendation12 = recommendationRepo.save(Recommendation("FaR med regelbunden styrketräning för att förebygger nya besvär"))

        val measure1 = measureRepo.save(Measure("F43.8A", "Utmattningssyndrom", 1, "1.0"))
        val measure2 = measureRepo.save(Measure("M75", "Sjukdomstillstånd i skulderled", 1, "1.0"))
        val measure3 = measureRepo.save(Measure("F32", "Depressiv episod", 1, "1.0"))
        val measure4 = measureRepo.save(Measure("F41", "Andra ångestsyndrom", 1, "1.0"))
        val measure5 = measureRepo.save(Measure("M54", "Ryggvärk", 1, "1.0"))

        val priority01 = priorityRepo.save(Priority(1, measure1, recommendation01))
        val priority02 = priorityRepo.save(Priority(2, measure1, recommendation02))
        val priority03 = priorityRepo.save(Priority(3, measure1, recommendation03))
        val priority04 = priorityRepo.save(Priority(1, measure2, recommendation04))
        val priority05 = priorityRepo.save(Priority(2, measure2, recommendation05))
        val priority06 = priorityRepo.save(Priority(1, measure3, recommendation06))
        val priority07 = priorityRepo.save(Priority(2, measure3, recommendation07))
        val priority08 = priorityRepo.save(Priority(3, measure3, recommendation08))
        val priority09 = priorityRepo.save(Priority(1, measure4, recommendation07))
        val priority10 = priorityRepo.save(Priority(2, measure4, recommendation09))
        val priority11 = priorityRepo.save(Priority(3, measure4, recommendation10))
        val priority12 = priorityRepo.save(Priority(1, measure5, recommendation11))
        val priority13 = priorityRepo.save(Priority(2, measure5, recommendation12))

        measure1.recommendations.add(priority01)
        measureRepo.save(measure1)
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
