package se.inera.intyg.srs.vo

import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.*
import se.inera.intyg.srs.persistence.InternalStatistic
import se.inera.intyg.srs.persistence.StatisticRepository
import java.util.*
@Service
class StatisticModule(@Autowired val statistikRepo: StatisticRepository): InformationModule<Statistik> {

    private val MIN_ID_POSITIONS = 3

    override fun getInfo(persons: List<Person>): Map<Person, Statistik> {
        val statistics: HashMap<Person, Statistik> = HashMap<Person, Statistik>()
        persons.forEach { person ->
            statistics.put(person, createInfo(person))
        }
        return statistics
    }

    private val log = LogManager.getLogger()

    private fun createInfo(person: Person): Statistik {
        val outgoingStatistik = Statistik()
        person.diagnoses.forEach { diagnose ->
            outgoingStatistik.statistikbild.add(getStatistikbildForDiagnosis(diagnose.code))
        }
        return outgoingStatistik
    }

    private fun getStatistikbildForDiagnosis(diagnosisId: String): Statistikbild? {
        var currentId = cleanDiagnosisCode(diagnosisId)
        val possibleStatistics = statistikRepo.findByDiagnosisId(currentId.substring(0, MIN_ID_POSITIONS))

        var status: Statistikstatus = Statistikstatus.OK

        while (currentId.length >= MIN_ID_POSITIONS) {
            val statistikbild = measureForCode(possibleStatistics, currentId)
            if (statistikbild.andringstidpunkt != null && statistikbild.bildadress != null) {
                statistikbild.statistikstatus = status

                return statistikbild
            }
            currentId = currentId.substring(0, currentId.length - 1)
            // Once we have shortened the code, we need to indicate that the info is not on the original level
            status = Statistikstatus.DIAGNOSKOD_PA_HOGRE_NIVA
        }

        val nothingFound = Statistikbild()
        nothingFound.statistikstatus = Statistikstatus.STATISTIK_SAKNAS
        nothingFound.diagnos = buildDiagnosis(diagnosisId)
        return nothingFound
    }

    private fun measureForCode(statistics: List<InternalStatistic>, diagnosisId: String): Statistikbild {
        val internal: InternalStatistic? = statistics.find { cleanDiagnosisCode(it.diagnosisId) == diagnosisId }
        val ret = Statistikbild()
        ret.diagnos = buildDiagnosis(diagnosisId)
        if (internal != null) {
            ret.andringstidpunkt = internal.timestamp
            ret.bildadress = internal.pictureUrl
        }
        return ret
    }

    private fun buildDiagnosis(diagnosisId: String): Diagnos {
        val diagnos = Diagnos()
        diagnos.code = diagnosisId
        diagnos.codeSystem = "1.2.752.116.1.1.1.1.3"
        return diagnos
    }

    private fun cleanDiagnosisCode(diagnosisId: String): String = diagnosisId.toUpperCase(Locale.ENGLISH).replace(".", "")
}

