package se.inera.intyg.srs.config

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.FilterConfig
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.context.support.SpringBeanAutowiringSupport

const val SESSION_ID_KEY = "session.id"
const val TRACE_ID_KEY = "trace.id"

@Component
class MdcServletFilter : Filter {

    @Autowired
    private lateinit var mdcHelper: MdcHelper

    private val logger = LoggerFactory.getLogger(MdcServletFilter::class.java)

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        logger.info("Entering MdcServletFilter doFilter method")
        try {
            if (request is HttpServletRequest) {
                MDC.put(SESSION_ID_KEY, mdcHelper.sessionId(request))
                MDC.put(TRACE_ID_KEY, mdcHelper.traceId(request))
            }
            chain.doFilter(request, response)
        } finally {
            MDC.clear()
        }
    }

    override fun init(filterConfig: FilterConfig) {
        logger.info("Initializing MdcServletFilter")
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(
                this,
                filterConfig.servletContext
        )
    }
}
