package se.inera.intyg.srs

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.support.AnnotationConfigContextLoader
import se.inera.intyg.srs.vo.PredictionAdapter
import se.inera.intyg.srs.vo.TestPredictionAdapter

/**
 * This class contains no real tests, but it still has a purpose: if anything is wrong with the Spring wiring of beans, or if the
 * JPA database inserts are inconsistent, this class will raise errors at build time.
 */
@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = arrayOf("classpath:test.properties"))
@ContextConfiguration(loader= AnnotationConfigContextLoader::class)
class ApplicationTests {

    @Configuration
    internal class ContextConfiguration {

        @Bean
        fun predictionAdaper(): PredictionAdapter {
            return TestPredictionAdapter()
        }
    }

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Before
    fun setup() {
        val ctx = AnnotationConfigApplicationContext()
        ctx.environment.setActiveProfiles("test")
        ctx.register(TestPredictionAdapter::class.java)
    }

    @Test
    fun dummyTest() {
    }
}
