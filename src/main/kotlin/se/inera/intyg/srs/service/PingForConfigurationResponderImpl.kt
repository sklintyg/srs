package se.inera.intyg.srs.service

import org.apache.cxf.annotations.SchemaValidation
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.PropertySource
import org.springframework.stereotype.Service
import se.riv.itintegration.monitoring.rivtabp21.v1.PingForConfigurationResponderInterface
import se.riv.itintegration.monitoring.v1.ConfigurationType
import se.riv.itintegration.monitoring.v1.PingForConfigurationResponseType
import se.riv.itintegration.monitoring.v1.PingForConfigurationType
import java.time.LocalDateTime

@Service
@SchemaValidation(type = SchemaValidation.SchemaValidationType.BOTH)
@PropertySource("classpath:version.properties")
class PingForConfigurationResponderImpl : PingForConfigurationResponderInterface {

    @Value("\${project.version}")
    lateinit var projectVersion: String

    @Value("\${buildNumber}")
    lateinit var buildNumberString: String

    @Value("\${buildTime}")
    lateinit var buildTimeString: String

    override fun pingForConfiguration(p0: String?, p1: PingForConfigurationType?): PingForConfigurationResponseType {
        val response = PingForConfigurationResponseType()
        response.pingDateTime = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss").format(LocalDateTime.now())
        response.version = projectVersion
        response.configuration.add(configuration("buildNumber", buildNumberString))
        response.configuration.add(configuration("buildTime", buildTimeString))
        return response
    }
    private fun configuration(name: String, value: String): ConfigurationType {
        val res = ConfigurationType()
        res.name = name
        res.value = value
        return res
    }
}
