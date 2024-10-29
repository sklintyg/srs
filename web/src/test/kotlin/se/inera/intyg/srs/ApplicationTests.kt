package se.inera.intyg.srs

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.support.AnnotationConfigContextLoader
import se.inera.intyg.srs.vo.PredictionAdapter
import se.inera.intyg.srs.vo.TestPredictionAdapter
import org.apache.cxf.Bus
import org.apache.cxf.bus.spring.SpringBus;

/**
 * This class contains no real tests, but it still has a purpose: if anything is wrong with the Spring wiring of beans, or if the
 * JPA database inserts are inconsistent, this class will raise errors at build time.
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:application-test.properties")
@ContextConfiguration
open class ApplicationTests {

    @Configuration
    internal class ContextConfiguration {

        @Bean
        fun predictionAdaper(): PredictionAdapter {
            return TestPredictionAdapter()
        }

        @Bean
        fun springBus(): Bus  {
            return SpringBus();
        }
    }

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var bus: Bus

    @BeforeEach
    fun setup() {
        val ctx = AnnotationConfigApplicationContext()
        ctx.environment.setActiveProfiles("test")
        ctx.register(TestPredictionAdapter::class.java)
    }

    @Test
    fun dummyTest() {
    }
}
