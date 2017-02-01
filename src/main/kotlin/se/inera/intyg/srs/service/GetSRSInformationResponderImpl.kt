package se.inera.intyg.srs.service

import org.apache.cxf.annotations.SchemaValidation
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Service
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.GetSRSInformationRequestType
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.GetSRSInformationResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.GetSRSInformationResponseType
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Individ
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.ResultCodeEnum
import se.inera.intyg.srs.vo.Diagnos
import se.inera.intyg.srs.vo.Extent
import se.inera.intyg.srs.vo.FmbInformationModule
import se.inera.intyg.srs.vo.Person
import se.inera.intyg.srs.vo.PrediktionInformationModule
import se.inera.intyg.srs.vo.Sex
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
@SchemaValidation(type = SchemaValidation.SchemaValidationType.BOTH)
class GetSRSInformationResponderImpl : GetSRSInformationResponderInterface {
    private val log = LogManager.getLogger()

    override fun getSRSInformation(request: GetSRSInformationRequestType): GetSRSInformationResponseType {
        log.info("Received request from ${request.konsumentId.extension}")

        val persons = transform(request.individer.individ)
        val results = mutableListOf<String>()

        if (request.utdatafilter.isFmbinformation) {
            results.add(FmbInformationModule().getInfo(persons))
        }
        if (request.utdatafilter.isPrediktion) {
            results.add(PrediktionInformationModule().getInfo(persons))
        }

        val response = GetSRSInformationResponseType()
        response.resultCode = ResultCodeEnum.OK
        return response
    }

    private fun transform(individer: List<Individ>): List<Person> =
            individer.map { individ ->
                val age = calulateAge(individ.personId)
                val sex = calculateSex(individ.personId)
                val extent = if (individ.omfattning != null) Extent.valueOf(individ.omfattning) else null
                val diagnoses = individ.diagnos!!.map { diagnos -> Diagnos(diagnos.code) }
                Person(individ.personId, age, sex, extent, diagnoses)
            }

    fun calulateAge(personId: String): Int {
        val year = personId.substring(0..3).toInt()
        val month = personId.substring(4..5).toInt()
        val day = personId.substring(6..7).toInt()
        val birthDate = LocalDate.of(year, month, day)
        val today = LocalDate.now()
        return ChronoUnit.YEARS.between(birthDate, today).toInt()
    }

    fun calculateSex(personId: String) = if (personId.substring(10).toInt() % 2 == 0) Sex.WOMAN else Sex.MAN


}
