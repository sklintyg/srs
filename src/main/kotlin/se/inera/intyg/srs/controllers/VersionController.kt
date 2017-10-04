package se.inera.intyg.srs.controllers
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class VersionController {
    private val LOG = LogManager.getLogger()

    @Value("\${spring.profiles.active}")
    lateinit var activeProfiles: String

    @Value("\${project.version}")
    lateinit var buildVersion: String

    @RequestMapping(value = "/version", method = arrayOf(RequestMethod.GET), produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun version(): Map<String, String> {
        LOG.info("Serving version")
        val versionMap = HashMap<String, String>()

        versionMap.put("Build version", buildVersion)
        versionMap.put("Profiles", activeProfiles)

        return versionMap
    }
}