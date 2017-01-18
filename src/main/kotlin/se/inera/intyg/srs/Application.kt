package se.inera.intyg.srs

import org.apache.cxf.Bus
import org.apache.cxf.jaxws.EndpointImpl
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import se.inera.intyg.srs.customer.Customer
import se.inera.intyg.srs.customer.CustomerRepository
import se.inera.intyg.srs.service.GetSRSInformationResponderImpl
import javax.xml.ws.Endpoint

@SpringBootApplication
class Application {
    private val log = LoggerFactory.getLogger(this.javaClass.name)

    @Autowired
    lateinit var bus: Bus

    @Bean
    fun init(repository: CustomerRepository) = CommandLineRunner {
        repository.save(Customer("Glenn", "Johansson"))
        repository.save(Customer("Glenn", "Andersson"))
        repository.save(Customer("Ada", "Johansson"))
        repository.save(Customer("Glenn", "Nilsson"))
        repository.save(Customer("Ada", "Svensson"))

        log.info("Customers found with findAll():")
        log.info("-------------------------------")
        for (customer in repository.findAll()) {
            log.info(customer.toString())
        }
        log.info("")

        val customer = repository.findOne(1L)
        log.info("Customer found with findOne(1L):")
        log.info("--------------------------------")
        log.info(customer.toString())
        log.info("")

        log.info("Customer found with findByLastName('Johansson'):")
        log.info("--------------------------------------------")
        for (Johansson in repository.findByLastNameIgnoreCase("Johansson")) {
            log.info(Johansson.toString())
        }
        log.info("")
    }

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
