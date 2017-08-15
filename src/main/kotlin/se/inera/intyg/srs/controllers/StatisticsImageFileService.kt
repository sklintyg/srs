package se.inera.intyg.srs.controllers

import com.google.common.io.ByteStreams
import org.apache.logging.log4j.LogManager

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.ResourceLoader
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import java.io.IOException

@RestController
class StatisticsImageFileService(@Value("\${statistics.image.dir}") val imageDir: String) {
    private val log = LogManager.getLogger()

    private val resourceLoader: ResourceLoader = DefaultResourceLoader()

    @RequestMapping(value = "/image/{imageName}", method = arrayOf(RequestMethod.GET),
            produces = arrayOf(MediaType.IMAGE_JPEG_VALUE))
    @ResponseBody
    fun getImage(@PathVariable(value = "imageName") imageName: String): ByteArray? {
        log.info("Serving: $imageDir/$imageName")
        var bytes: ByteArray? = null
        try {
            val res = resourceLoader.getResource("file:$imageDir/$imageName.jpg")
            val inputStream = res.inputStream
            bytes = ByteStreams.toByteArray(inputStream)
            inputStream.close()
        } catch (e: IOException) {
            log.error("Failed to read file: $e")
        }
        return bytes
    }
}