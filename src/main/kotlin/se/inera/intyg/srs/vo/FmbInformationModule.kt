package se.inera.intyg.srs.vo

import org.springframework.stereotype.Service
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Fmbinformation
import java.util.*

@Service
class FmbInformationModule : InformationModule<Fmbinformation> {

    override fun getInfo(persons: List<Person>): Map<Person, Fmbinformation> {
        return HashMap<Person, Fmbinformation>()
    }

}
