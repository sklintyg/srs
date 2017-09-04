package se.inera.intyg.srs.service

import org.apache.cxf.annotations.SchemaValidation
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Service
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Bedomningsunderlag
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.GetSRSInformationRequestType
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.GetSRSInformationResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.GetSRSInformationResponseType
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Individ
import se.inera.intyg.srs.vo.Diagnosis
import se.inera.intyg.srs.vo.Extent
import se.inera.intyg.srs.vo.MeasureInformationModule
import se.inera.intyg.srs.vo.Person
import se.inera.intyg.srs.vo.PredictionInformationModule
import se.inera.intyg.srs.vo.Sex
import se.inera.intyg.srs.vo.StatisticModule
import se.riv.clinicalprocess.healthcond.certificate.types.v2.ResultCodeEnum
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
@SchemaValidation(type = SchemaValidation.SchemaValidationType.BOTH)
class GetSRSInformationResponderImpl(val measureModule: MeasureInformationModule,
                                     val predictionModule: PredictionInformationModule,
                                     val statisticModule: StatisticModule) : GetSRSInformationResponderInterface {

    private val log = LogManager.getLogger()

    override fun getSRSInformation(request: GetSRSInformationRequestType): GetSRSInformationResponseType {
        log.info("Received request from ${request.konsumentId.extension}")

        val persons = transform(request.individer.individ)
        val response = GetSRSInformationResponseType()

        if (request.utdatafilter.isPrediktion) {
            try {
                predictionModule.getInfo(persons, mapOf()).forEach { (person, prediction) ->
                    val underlag = response.bedomningsunderlag.find { it.personId == person.personId } ?: createUnderlag(person.personId, response)
                    underlag.prediktion = prediction
                }
            } catch (e: Exception) {
                log.error("Predictions could not be produced. Please check for error.", e)
            }
        }

        if (request.utdatafilter.isAtgardsrekommendation) {
            try {
                measureModule.getInfo(persons, mapOf()).forEach { (person, measure) ->
                    val underlag = response.bedomningsunderlag.find { it.personId == person.personId } ?: createUnderlag(person.personId, response)
                    underlag.atgardsrekommendationer = measure
                }
            } catch (e: Exception) {
                log.error("Measures could not be produced. Please check for error.", e)
            }
        }

        if (request.utdatafilter.isStatistik) {
            try {
                statisticModule.getInfo(persons, mapOf()).forEach { (person, statistic) ->
                    val underlag = response.bedomningsunderlag.find { it.personId == person.personId } ?: createUnderlag(person.personId, response)
                    underlag.statistik = statistic
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

    private fun transform(individer: List<Individ>): List<Person> =
            individer.map { individ ->
                val age = calculateAge(individ.personId)
                val sex = calculateSex(individ.personId)
                val extent = Extent.valueOf(individ.omfattning.value())
                val diagnoses = individ.diagnos.map { diagnos -> Diagnosis(diagnos.code) }
                Person(individ.personId, age, sex, extent, diagnoses)
            }

    private fun calculateAge(personId: String): Int {
        val year = personId.substring(0..3).toInt()
        val month = personId.substring(4..5).toInt()
        val day = personId.substring(6..7).toInt()
        val birthDate = LocalDate.of(year, month, day)
        val today = LocalDate.now()
        return ChronoUnit.YEARS.between(birthDate, today).toInt()
    }

    private fun calculateSex(personId: String) = if (personId[10].toInt() % 2 == 0) Sex.WOMAN else Sex.MAN

}
