package se.inera.intyg.srs

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApplicationTests {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Test
    fun findAll() {
        val content = """[{"firstName":"Glenn","lastName":"Johansson","id":1},{"firstName":"Glenn","lastName":"Andersson","id":2},{"firstName":"Ada","lastName":"Johansson","id":3},{"firstName":"Glenn","lastName":"Nilsson","id":4},{"firstName":"Ada","lastName":"Svensson","id":5}]"""
        assertEquals(content, restTemplate.getForEntity("/customers", String::class.java).body)
    }

}
