package se.inera.intyg.srs.controller

import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.springframework.boot.info.BuildProperties
import org.springframework.core.env.Environment
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.ZoneId

@RestController
class VersionController(private val buildProperties: BuildProperties, private val environment:Environment) {
    private val _log = LogManager.getLogger()

    @RequestMapping(
            value = ["/version"],
            method = [(RequestMethod.GET)],
            produces = [(MediaType.APPLICATION_JSON_VALUE)])
    fun version(): ResponseEntity<VersionInfo> {
        _log.info("Serving version")

        val applicationName = buildProperties.artifact
        val buildVersion = buildProperties.version
        val buildTimestamp = LocalDateTime.ofInstant(buildProperties.time, ZoneId.systemDefault())
        val activeProfiles = StringUtils.join(environment.activeProfiles, ", ")

        return ResponseEntity.ok(VersionInfo(applicationName, buildVersion, buildTimestamp, activeProfiles))
    }
}
