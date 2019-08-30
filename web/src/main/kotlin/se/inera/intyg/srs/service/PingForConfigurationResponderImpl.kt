package se.inera.intyg.srs.service

import org.apache.cxf.annotations.SchemaValidation
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.PropertySource
import org.springframework.stereotype.Service
import se.riv.itintegration.monitoring.rivtabp21.v1.PingForConfigurationResponderInterface
import se.riv.itintegration.monitoring.v1.ConfigurationType
import se.riv.itintegration.monitoring.v1.PingForConfigurationResponseType
import se.riv.itintegration.monitoring.v1.PingForConfigurationType
import java.time.LocalDateTime
import java.time.ZoneId

@Service
@SchemaValidation(type = SchemaValidation.SchemaValidationType.BOTH)
class PingForConfigurationResponderImpl(val buildProperties: BuildProperties) : PingForConfigurationResponderInterface {

    override fun pingForConfiguration(p0: String?, p1: PingForConfigurationType?): PingForConfigurationResponseType {
        val response = PingForConfigurationResponseType()
        val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")
        val buildTimestamp = LocalDateTime.ofInstant(buildProperties.time, ZoneId.systemDefault()).format(formatter)
        response.pingDateTime = formatter.format(LocalDateTime.now())
        response.version = buildProperties.version
        response.configuration.add(configuration("buildNumber", "N/A"))
        response.configuration.add(configuration("buildTime", buildTimestamp))
        return response
    }

    private fun configuration(name: String, value: String): ConfigurationType {
        val res = ConfigurationType()
        res.name = name
        res.value = value
        return res
    }

}
