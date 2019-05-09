package se.inera.intyg.srs.controllers

import com.google.common.io.ByteStreams
import org.apache.logging.log4j.LogManager

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.io.IOException

@RestController
class StatisticsImageFileService(@Value("\${statistics.image.location-pattern}") val imageLocationPattern: String) {
    private val log = LogManager.getLogger()

    private val resourceLoader: ResourceLoader = DefaultResourceLoader()

    @RequestMapping(value = ["/image/{imageName}"], method = [RequestMethod.GET],
            produces = [MediaType.IMAGE_JPEG_VALUE])
    @ResponseBody
    fun getImage(@PathVariable(value = "imageName") imageName: String): ByteArray? {
        log.info("Serving: $imageName using patter $imageLocationPattern")

        var bytes: ByteArray? = null
        try {
            val res = ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(imageLocationPattern)
                    .find { it.filename.equals("$imageName.jpg") }
            val inputStream = res?.inputStream
            bytes = ByteStreams.toByteArray(inputStream)
            inputStream?.close()
        } catch (e: IOException) {
            log.error("Failed to read file: $e")
        }
        return bytes
    }
}
