package se.inera.intyg.srs.vo

import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Service
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Statistik
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Statistikbild
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Statistikstatus
import se.inera.intyg.srs.persistence.InternalStatistic
import se.inera.intyg.srs.persistence.StatisticRepository
import se.riv.clinicalprocess.healthcond.certificate.types.v2.Diagnos
import java.util.Locale

@Service
class StatisticModule(val statisticRepo: StatisticRepository) : InformationModule<Statistik> {

    private val MIN_ID_POSITIONS = 3

    override fun getInfo(persons: List<Person>, extraParams: Map<String, String>): Map<Person, Statistik> {
        log.info("Getting statistics for $persons")
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
        val possibleStatistics = statisticRepo.findByDiagnosisId(currentId.substring(0, MIN_ID_POSITIONS))

        var status: Statistikstatus = Statistikstatus.OK
        var statistikbild = Statistikbild()

        while (currentId.length >= MIN_ID_POSITIONS) {
            statistikbild.inkommandediagnos = buildDiagnosis(diagnosisId)
            statistikbild = measureForCode(statistikbild, possibleStatistics, currentId)

            if (statistikbild.andringstidpunkt != null && statistikbild.bildadress != null) {
                statistikbild.statistikstatus = status
                return statistikbild
            }
            currentId = currentId.substring(0, currentId.length - 1)
            // Once we have shortened the code, we need to indicate that the info is not on the original level
            status = Statistikstatus.DIAGNOSKOD_PA_HOGRE_NIVA
        }

        statistikbild.inkommandediagnos = buildDiagnosis(diagnosisId)
        statistikbild.statistikstatus = Statistikstatus.STATISTIK_SAKNAS
        return statistikbild
    }

    private fun measureForCode(ret: Statistikbild, statistics: List<InternalStatistic>, diagnosisId: String): Statistikbild {
        val internal: InternalStatistic? = statistics.find { cleanDiagnosisCode(it.diagnosisId) == diagnosisId }
        if (internal != null) {
            ret.diagnos = buildDiagnosis(diagnosisId)
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

