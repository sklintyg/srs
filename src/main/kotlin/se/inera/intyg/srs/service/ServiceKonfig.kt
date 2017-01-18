package se.inera.intyg.srs.service

import org.apache.cxf.Bus
import org.apache.cxf.jaxws.EndpointImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import javax.xml.ws.Endpoint

@Configuration
@ComponentScan("se.inera.intyg.srs.service, se.inera.intyg.src.customer")
class ServiceConfig {

    @Autowired
    lateinit var bus: Bus

    @Bean
    fun endpoint(): Endpoint {
        val endpoint = EndpointImpl(bus, GetSRSInformationResponderImpl())
        endpoint.publish("/Hello")
        return endpoint
    }
}


