package se.inera.intyg.srs

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.text.SimpleDateFormat
import java.time.*

@Configuration
open class Configuration() {

    @Bean
    @Profile("!it")
    open fun normalClock(): Clock {
        return Clock.systemDefaultZone();
    }

    @Bean
    @Profile("it")
    open fun integrationTestClock(): Clock {
        return Clock.fixed(
                ZonedDateTime.of(2020, 1, 31, 23,59,0,0,
                        ZoneId.systemDefault()).toInstant(),
                ZoneId.systemDefault())
    }

    @Bean
    open fun objectMapper(): ObjectMapper {
        val m = ObjectMapper()
        m.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        m.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        m.registerModule(Jdk8Module())
        m.registerModule(KotlinModule())
        m.registerModule(TemporalSerializer())
        m.setDateFormat(SimpleDateFormat("yyyy-MM-dd"))
        return m
    }

}

private class TemporalSerializer: SimpleModule() {
    init {
        addSerializer(LocalDateTime::class.java, LocalDateTimeSerializer.INSTANCE)
        addDeserializer(LocalDateTime::class.java, LocalDateTimeDeserializer.INSTANCE)

        addSerializer(LocalDate::class.java, LocalDateSerializer.INSTANCE)
        addDeserializer(LocalDate::class.java, LocalDateDeserializer.INSTANCE)

        addSerializer(LocalTime::class.java, LocalTimeSerializer.INSTANCE)
        addDeserializer(LocalTime::class.java, LocalTimeDeserializer.INSTANCE)
    }
}