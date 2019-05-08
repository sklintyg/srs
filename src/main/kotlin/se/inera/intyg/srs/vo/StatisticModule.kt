package se.inera.intyg.srs.vo

import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Service
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Statistikbild
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Statistikdata
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Statistikstatus
import se.inera.intyg.srs.persistence.InternalStatistic
import se.inera.intyg.srs.persistence.InternalStatisticRepository
import se.inera.intyg.srs.persistence.NationalStatisticRepository
import se.riv.clinicalprocess.healthcond.certificate.types.v2.Diagnos
import java.math.BigInteger
import java.util.Locale
import kotlin.collections.HashMap

@Service
class StatisticModule(val internalStatisticRepo: InternalStatisticRepository, val nationalStatisticRepo: NationalStatisticRepository) : InformationModule<Statistikbild> {

    private val MIN_ID_POSITIONS = 3

    override fun getInfoForDiagnosis(diagnosisId: String): Statistikbild = getStatistikbildForDiagnosis(diagnosisId)

    override fun getInfo(persons: List<Person>, extraParams: Map<String, Map<String, String>>, userHsaId: String, calculateIndividual: Boolean): Map<Person, List<Statistikbild>> {
        log.info("Getting statistics for $persons")
        if (calculateIndividual) {
            throw RuntimeException("calculateIndividual not supported")
        }
        val statistics: HashMap<Person, List<Statistikbild>> = HashMap()
        persons.forEach { person ->
            statistics.put(person, createInfo(person))
        }
        return statistics
    }

    private val log = LogManager.getLogger()

    private fun createInfo(person: Person): List<Statistikbild> {
        val outgoingStatistik = mutableListOf<Statistikbild>()
        person.diagnoses.forEach { diagnose ->
            outgoingStatistik.add(getStatistikbildForDiagnosis(diagnose.code))
        }
        return outgoingStatistik
    }

    private fun getStatistikbildForDiagnosis(diagnosisId: String): Statistikbild {
        var currentId = cleanDiagnosisCode(diagnosisId)
        val possibleStatistics = internalStatisticRepo.findByDiagnosisId(currentId.substring(0, MIN_ID_POSITIONS))
        val nationalStatistics = nationalStatisticRepo.findByDiagnosisIdOrderByDayIntervalMaxExcl(currentId.substring(0, MIN_ID_POSITIONS))

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
        nationalStatistics.forEach { natStat ->
                val statistikData = Statistikdata()
                statistikData.dagintervallMin = BigInteger.valueOf(natStat.dayIntervalMin.toLong())
                statistikData.dagintervallMaxExkl = BigInteger.valueOf(natStat.dayIntervalMaxExcl.toLong())
                statistikData.individer = BigInteger.valueOf(natStat.intervalQuantity.toLong())
                statistikData.individerAckumulerat = BigInteger.valueOf(natStat.accumulatedQuantity.toLong())
                statistikbild.data.add(statistikData)
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

