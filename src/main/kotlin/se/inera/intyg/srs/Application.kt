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
import se.inera.intyg.srs.db.Measure
import se.inera.intyg.srs.db.MeasureRepository
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
    fun init(repository: MeasureRepository) = CommandLineRunner {
        repository.save(Measure("F43.8A", "Utmattningssyndrom", 1, "patienten bör överväga att kontakta företagshälsovård och arbetsgivare för att avgränsa eller byta arbetsuppgifter, eller t.o.m. byta yrke eller arbetsplats", "1.0"))
        repository.save(Measure("F43.8A", "Utmattningssyndrom", 2, "remiss till behandling med psykoterapeutiska metoder", "1.0"))
        repository.save(Measure("F43.8A", "Utmattningssyndrom", 3, "ge patienten lättillgänglig information om diagnosen och behandlingsmöjligheter", "1.0"))
        repository.save(Measure("M75", "Sjukdomstillstånd i skulderled", 1, "patienten bör överväga att kontakta företagshälsovård och arbetsgivare för att undersöka möjligheter till ergonomisk rådgivning och arbetsanpassning.", "1.0"))
        repository.save(Measure("M75", "Sjukdomstillstånd i skulderled", 2, "förmedling av kontakt med fysioterapeut", "1.0"))
        repository.save(Measure("F32", "Depressiv episod", 1, "FaR med konditions- och styrketräning", "1.0"))
        repository.save(Measure("F32", "Depressiv episod", 2, "remiss till behandling med KBT", "1.0"))
        repository.save(Measure("F32", "Depressiv episod", 3, "remiss till behandling med rTMS", "1.0"))
        repository.save(Measure("F41", "Andra ångestsyndrom", 1, "Remiss till behandling med KBT", "1.0"))
        repository.save(Measure("F41", "Andra ångestsyndrom", 2, "Remiss till Internetförmedlad KBT via Internetbaserat stöd och behandling", "1.0"))
        repository.save(Measure("F41", "Andra ångestsyndrom", 3, "SSRI-läkemedel", "1.0"))
        repository.save(Measure("M54", "Ryggvärk", 1, "partiell sjukskrivning", "1.0"))
        repository.save(Measure("M54", "Ryggvärk", 2, "FaR med regelbunden styrketräning för att förebygger nya besvär", "1.0"))
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
