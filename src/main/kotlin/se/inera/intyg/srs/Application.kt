package se.inera.intyg.srs

import org.apache.cxf.Bus
import org.apache.cxf.jaxws.EndpointImpl
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import se.inera.intyg.srs.service.GetSRSInformationResponderImpl
import javax.xml.ws.Endpoint

@SpringBootApplication
class Application {
    private val log = LogManager.getLogger()

    @Autowired
    lateinit var bus: Bus

    @Bean
    fun endpoint(): Endpoint {
        val endpoint = EndpointImpl(bus, GetSRSInformationResponderImpl())
        endpoint.publish("/getsrs")
        return endpoint
    }

}

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
