package se.inera.intyg.srs.config

import jakarta.servlet.http.HttpServletRequest
import java.nio.CharBuffer
import java.util.concurrent.ThreadLocalRandom
import org.springframework.stereotype.Component

@Component
class MdcHelper {
    companion object {
        const val LOG_TRACE_ID_HEADER = "x-trace-id"
        const val LOG_SESSION_ID_HEADER = "x-session-id"
        private const val LENGTH_LIMIT = 8
        private val BASE62CHARS =
                "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray()
    }

    fun sessionId(http: HttpServletRequest): String {
        return http.getHeader(LOG_SESSION_ID_HEADER) ?: "-"
    }

    fun traceId(http: HttpServletRequest): String {
        return http.getHeader(LOG_TRACE_ID_HEADER) ?: generateId()
    }

    fun traceId(): String {
        return generateId()
    }

    private fun generateId(): String {
        val charBuffer = CharBuffer.allocate(LENGTH_LIMIT)
        val random = ThreadLocalRandom.current()
        repeat(LENGTH_LIMIT) {
            val value = random.nextInt(BASE62CHARS.size)
            charBuffer.append(BASE62CHARS[value])
        }
        return charBuffer.rewind().toString()
    }
}
