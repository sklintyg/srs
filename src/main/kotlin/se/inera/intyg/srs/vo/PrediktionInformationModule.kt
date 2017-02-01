package se.inera.intyg.srs.vo

import org.apache.logging.log4j.LogManager


class PrediktionInformationModule : InformationModule {

    private val log = LogManager.getLogger()

    override fun getInfo(persons: List<Person>): String {
        log.info(persons)
        return "yes"
    }

}
