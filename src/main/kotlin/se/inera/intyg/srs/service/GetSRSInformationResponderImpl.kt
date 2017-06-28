package se.inera.intyg.srs.service

import org.apache.cxf.annotations.SchemaValidation
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
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
import se.riv.clinicalprocess.healthcond.certificate.types.v2.ResultCodeEnum
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
@SchemaValidation(type = SchemaValidation.SchemaValidationType.BOTH)
class GetSRSInformationResponderImpl(@Autowired val measureModule: MeasureInformationModule,
                                     @Autowired val predictionModule: PredictionInformationModule) : GetSRSInformationResponderInterface {

    private val log = LogManager.getLogger()

    override fun getSRSInformation(request: GetSRSInformationRequestType): GetSRSInformationResponseType {
        log.info("Received request from ${request.konsumentId.extension}")

        val persons = transform(request.individer.individ)
        val response = GetSRSInformationResponseType()

        persons.forEach { person ->
            val underlag = Bedomningsunderlag()
            underlag.personId = person.personId
            response.bedomningsunderlag.add(underlag)
        }

        if (request.utdatafilter.isPrediktion) {
            try {
                val predictions = predictionModule.getInfo(persons)
                predictions.forEach { prediction ->
                    val underlag = response.bedomningsunderlag.find { it.personId == prediction.key.personId }
                    underlag!!.prediktion = prediction.value
                }
            } catch (e: Exception) {
                log.error("Predictions could not be produced. Please check for errror.", e)
            }
        }

        if (request.utdatafilter.isAtgardsrekommendation) {
            try {
                val measures = measureModule.getInfo(persons)
                measures.forEach { measure ->
                    val underlag = response.bedomningsunderlag.find { it.personId == measure.key.personId }
                    underlag!!.atgardsrekommendationer = measure.value
                }
            } catch (e: Exception) {
                log.error("Mesaures could not be produced. Please check for errror.", e)
            }
        }

        response.resultCode = ResultCodeEnum.OK
        return response
    }

    private fun transform(individer: List<Individ>): List<Person> =
            individer.map { individ ->
                val age = calulateAge(individ.personId)
                val sex = calculateSex(individ.personId)
                val extent = Extent.valueOf(individ.omfattning.value())
                val diagnoses = individ.diagnos!!.map { diagnos -> Diagnosis(diagnos.code) }
                Person(individ.personId, age, sex, extent, diagnoses)
            }

    private fun calulateAge(personId: String): Int {
        val year = personId.substring(0..3).toInt()
        val month = personId.substring(4..5).toInt()
        val day = personId.substring(6..7).toInt()
        val birthDate = LocalDate.of(year, month, day)
        val today = LocalDate.now()
        return ChronoUnit.YEARS.between(birthDate, today).toInt()
    }

    private fun calculateSex(personId: String) = if (personId[10].toInt() % 2 == 0) Sex.WOMAN else Sex.MAN

}
