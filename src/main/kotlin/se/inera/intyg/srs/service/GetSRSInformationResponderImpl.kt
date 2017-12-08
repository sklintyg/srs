package se.inera.intyg.srs.service

import org.apache.cxf.annotations.SchemaValidation
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Service
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Atgardsrekommendationer
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Bedomningsunderlag
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.GetSRSInformationRequestType
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.GetSRSInformationResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.GetSRSInformationResponseType
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Individ
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Prediktion
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Prediktionsfaktorer
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Statistik
import se.inera.intyg.srs.vo.Diagnosis
import se.inera.intyg.srs.vo.MeasureInformationModule
import se.inera.intyg.srs.vo.Person
import se.inera.intyg.srs.vo.PredictionInformationModule
import se.inera.intyg.srs.vo.Sex
import se.inera.intyg.srs.vo.StatisticModule
import se.riv.clinicalprocess.healthcond.certificate.types.v2.ResultCodeEnum
import java.time.LocalDate
import java.time.temporal.ChronoUnit

val REGION = "Region"
val STHLM = "Stockholm"
val NORD = "Nord"
val VAST = "Vast"
val MITT = "Mitt"
val SYD = "Syd"

val TODDLERS = "17-29"
val KIDS = "30-39"
val YOUTHS = "40-49"
val ADULTS = "50-56"
val AT_DEATHS_DOOR = "57-63"

@Service
@SchemaValidation(type = SchemaValidation.SchemaValidationType.BOTH)
class GetSRSInformationResponderImpl(val measureModule: MeasureInformationModule,
                                     val predictionModule: PredictionInformationModule,
                                     val statisticModule: StatisticModule) : GetSRSInformationResponderInterface {

    private val log = LogManager.getLogger()

    override fun getSRSInformation(request: GetSRSInformationRequestType): GetSRSInformationResponseType {
        log.info("Received request from ${request.konsumentId.extension}")

        val persons = transformIndividuals(request.individer.individ)
        val extraInfo: Map<String, String> =
                if (request.prediktionsfaktorer != null)
                    transformPredictionFactors(request.prediktionsfaktorer)
                else mapOf()

        val response = GetSRSInformationResponseType()

        if (request.utdatafilter.isPrediktion) {
            try {
                predictionModule.getInfo(persons, extraInfo, request.anvandareId.extension).forEach { (person, prediction) ->
                    val dtoPredictionList = Prediktion()
                    dtoPredictionList.diagnosprediktion.addAll(prediction)
                    val underlag = response.bedomningsunderlag.find { it.personId == person.personId } ?: createUnderlag(person.personId, response)
                    underlag.prediktion = dtoPredictionList
                }
            } catch (e: Exception) {
                log.error("Predictions could not be produced. Please check for error.", e)
            }
        }

        if (request.utdatafilter.isAtgardsrekommendation) {
            try {
                measureModule.getInfo(persons).forEach { (person, measure) ->
                    val underlag = response.bedomningsunderlag.find { it.personId == person.personId } ?: createUnderlag(person.personId, response)
                    val dtoAtgardsrekommendationList = Atgardsrekommendationer()
                    dtoAtgardsrekommendationList.rekommendation.addAll(measure)
                    underlag.atgardsrekommendationer = dtoAtgardsrekommendationList
                }
            } catch (e: Exception) {
                log.error("Measures could not be produced. Please check for error.", e)
            }
        }

        if (request.utdatafilter.isStatistik) {
            try {
                statisticModule.getInfo(persons).forEach { (person, statistic) ->
                    val underlag = response.bedomningsunderlag.find { it.personId == person.personId } ?: createUnderlag(person.personId, response)
                    val dtoStatistikList = Statistik()
                    dtoStatistikList.statistikbild.addAll(statistic)
                    underlag.statistik = dtoStatistikList
                }
            } catch (e: Exception) {
                log.error("Statistics could not be produced. Please check for error.", e)
            }
        }

        response.resultCode = ResultCodeEnum.OK
        return response
    }

    private fun createUnderlag(personId: String, response: GetSRSInformationResponseType): Bedomningsunderlag {
        val underlag = Bedomningsunderlag()
        underlag.personId = personId
        response.bedomningsunderlag.add(underlag)
        return underlag
    }

    private fun transformIndividuals(individer: List<Individ>) =
            individer.map { individ ->
                val age = calculateAge(individ.personId)
                val sex = calculateSex(individ.personId)
                val diagnoses = individ.diagnos.map { diagnos -> Diagnosis(diagnos.code) }
                val certificateId = individ.intygId.extension
                Person(individ.personId, age, sex, diagnoses, certificateId)
            }

    private fun calculateAge(personId: String): String {
        val year = personId.substring(0..3).toInt()
        val month = personId.substring(4..5).toInt()
        val day = personId.substring(6..7).toInt()
        val birthDate = LocalDate.of(year, month, day)
        val today = LocalDate.now()
        return calculateAgeCategory(ChronoUnit.YEARS.between(birthDate, today).toInt())
    }

    private fun calculateAgeCategory(age: Int) =
            when (age) {
                in 0..29 -> TODDLERS
                in 30..39 -> KIDS
                in 40..49 -> YOUTHS
                in 50..56 -> ADULTS
                else -> AT_DEATHS_DOOR
            }

    private fun calculateSex(personId: String) = if (personId[10].toInt() % 2 == 0) Sex.WOMAN else Sex.MAN

    private fun transformPredictionFactors(factors: Prediktionsfaktorer): Map<String, String> {
        val returnMap: MutableMap<String, String> = mutableMapOf()
        returnMap[REGION] = calculateRegion(factors.postnummer)
        factors.fragasvar.forEach { returnMap[it.frageidSrs] = it.svarsidSrs }
        return returnMap
    }

    private fun calculateRegion(zipCode: String?) =
            if (zipCode != null && zipCode.length >= 2 && zipCode.substring(0, 2).matches(Regex("\\d\\d")))
                when (zipCode.substring(0, 2).toInt()) {
                    in 80..98 -> NORD
                    in 30..31 -> VAST
                    in 40..47 -> VAST
                    in 50..54 -> VAST
                    in 65..71 -> VAST
                    in 77..79 -> VAST
                    in 19..19 -> MITT
                    in 58..61 -> MITT
                    in 63..64 -> MITT
                    in 72..75 -> MITT
                    in 10..19 -> STHLM
                    in 62..76 -> STHLM
                    in 20..29 -> SYD
                    in 33..39 -> SYD
                    in 55..57 -> SYD
                    else -> ""
                } else ""

}
