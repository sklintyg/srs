package se.inera.intyg.srs.vo

import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Service
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Diagnosstatistik
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Statistikdata
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Statistikstatus
import se.inera.intyg.srs.persistence.InternalStatisticRepository
import se.inera.intyg.srs.persistence.NationalStatisticRepository
import se.riv.clinicalprocess.healthcond.certificate.types.v2.Diagnos
import java.math.BigInteger
import java.util.Locale
import kotlin.collections.HashMap

@Service
class StatisticModule(val internalStatisticRepo: InternalStatisticRepository, val nationalStatisticRepo: NationalStatisticRepository) : InformationModule<Diagnosstatistik> {

    private val MIN_ID_POSITIONS = 3

    override fun getInfoForDiagnosis(diagnosisId: String): Diagnosstatistik = getStatistikForDiagnosis(diagnosisId)

    override fun getInfo(persons: List<Person>, extraParams: Map<String, Map<String, String>>,
                         careUnitHsaId: String, calculateIndividual: Boolean): Map<Person, List<Diagnosstatistik>> {
        log.info("Getting statistics for $persons")
        if (calculateIndividual) {
            throw RuntimeException("calculateIndividual not supported")
        }
        val statistics: HashMap<Person, List<Diagnosstatistik>> = HashMap()
        persons.forEach { person ->
            statistics.put(person, createInfo(person))
        }
        return statistics
    }

    private val log = LogManager.getLogger()

    private fun createInfo(person: Person): List<Diagnosstatistik> {
        val outgoingStatistik = mutableListOf<Diagnosstatistik>()
        person.diagnoses.forEach { diagnose ->
            outgoingStatistik.add(getStatistikForDiagnosis(diagnose.code))
        }
        return outgoingStatistik
    }

    private fun getStatistikForDiagnosis(diagnosisId: String): Diagnosstatistik {
        var currentId = cleanDiagnosisCode(diagnosisId)

        var status: Statistikstatus = Statistikstatus.OK
        var diagnosstatistik = Diagnosstatistik()

        while (currentId.length >= MIN_ID_POSITIONS) {
            diagnosstatistik.inkommandediagnos = buildDiagnosis(diagnosisId)

            val nationalStatistics =
                    nationalStatisticRepo.findByDiagnosisIdOrderByDayIntervalMaxExcl(currentId.substring(0, MIN_ID_POSITIONS))
            if (nationalStatistics != null && nationalStatistics.isNotEmpty()) {
                diagnosstatistik.diagnos = buildDiagnosis(currentId)
                diagnosstatistik.statistikstatus = status
                diagnosstatistik.data.clear()
                nationalStatistics.forEach { natStat ->
                    val statistikData = Statistikdata()
                    statistikData.dagintervallMin = BigInteger.valueOf(natStat.dayIntervalMin.toLong())
                    statistikData.dagintervallMaxExkl = BigInteger.valueOf(natStat.dayIntervalMaxExcl.toLong())
                    statistikData.individer = BigInteger.valueOf(natStat.intervalQuantity.toLong())
                    statistikData.individerAckumulerat = BigInteger.valueOf(natStat.accumulatedQuantity.toLong())
                    diagnosstatistik.data.add(statistikData)
                }
            }
            currentId = currentId.substring(0, currentId.length - 1)
            // Once we have shortened the code, we need to indicate that the info is not on the original level
            status = Statistikstatus.DIAGNOSKOD_PA_HOGRE_NIVA
        }
        if (diagnosstatistik.statistikstatus == null) {
            diagnosstatistik.inkommandediagnos = buildDiagnosis(diagnosisId)
            diagnosstatistik.statistikstatus = Statistikstatus.STATISTIK_SAKNAS
        }
        return diagnosstatistik
    }

    private fun buildDiagnosis(diagnosisId: String): Diagnos {
        val diagnos = Diagnos()
        diagnos.code = diagnosisId
        diagnos.codeSystem = "1.2.752.116.1.1.1.1.3"
        return diagnos
    }

    private fun cleanDiagnosisCode(diagnosisId: String): String = diagnosisId.toUpperCase(Locale.ENGLISH).replace(".", "")
}

