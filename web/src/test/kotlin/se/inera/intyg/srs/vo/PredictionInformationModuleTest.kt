package se.inera.intyg.srs.vo

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.stubbing.Answer
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v3.Diagnosprediktion
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v3.Diagnosprediktionstatus
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.EgenBedomningRiskType
import se.inera.intyg.srs.persistence.entity.OwnOpinion
import se.inera.intyg.srs.persistence.entity.PatientAnswer
import se.inera.intyg.srs.persistence.entity.PredictionDiagnosis
import se.inera.intyg.srs.persistence.entity.PredictionPriority
import se.inera.intyg.srs.persistence.entity.PredictionQuestion
import se.inera.intyg.srs.persistence.entity.PredictionResponse
import se.inera.intyg.srs.persistence.entity.Probability
import se.inera.intyg.srs.persistence.repository.DiagnosisRepository
import se.inera.intyg.srs.persistence.repository.ProbabilityRepository
import se.inera.intyg.srs.service.LOCATION_KEY
import se.inera.intyg.srs.service.QUESTIONS_AND_ANSWERS_KEY
import se.inera.intyg.srs.service.REGION_KEY
import se.inera.intyg.srs.service.ZIP_CODE_KEY
import se.inera.intyg.srs.util.getModelForDiagnosis
import java.time.LocalDateTime


class PredictionInformationModuleTest {

    private lateinit var consentModule: ConsentModule
    private lateinit var predictionInformationModule: PredictionInformationModule
    private lateinit var diagnosisRepo: DiagnosisRepository
    private lateinit var probabilityRepo: ProbabilityRepository

    private val testPerson = Person("198402289287", "KIDS", Sex.WOMAN,
            listOf(Diagnosis("F438A")),"certId-1")

    @BeforeEach
    fun setup() {
        consentModule = mock()
        diagnosisRepo = mock()
        probabilityRepo = mock()
        predictionInformationModule = PredictionInformationModule(TestPredictionAdapter(),diagnosisRepo,probabilityRepo,mock(), consentModule, mock())
        initData()
    }

    private fun initData() {
        whenever(consentModule.consentNeeded()).thenReturn(false)

        whenever(diagnosisRepo.getModelForDiagnosis(eq("F438A")))
                .thenReturn(PredictionDiagnosis("F43", 0.32,
                        listOf(PredictionPriority(1,
                                PredictionQuestion("question 1?", "help", "frageId-1",
                                        listOf(
                                                PredictionResponse("answer alternative 1-1!", "svarsId-1a", true, 1, null),
                                                PredictionResponse("answer alternative 1-2!", "svarsId-1b", false, 1, null)
                                        )
                                )
                        ),
                                PredictionPriority(2,
                                        PredictionQuestion("question 2?", "help", "frageId-2",
                                                listOf(
                                                        PredictionResponse("answer alternative 2-1!", "svarsId-2c", true, 1, null),
                                                        PredictionResponse("answer alternative 2-2!", "svarsId-2d", false, 1, null)
                                                )
                                        )
                                )
                        )
                ))
        whenever(probabilityRepo.save<Probability>(any())).thenAnswer(
                Answer<Probability>() {
                    it.getArgument(0) as Probability
                }
        )

        whenever(probabilityRepo.findByCertificateIdAndDiagnosisOrderByTimestampDesc(any(), any())).thenAnswer {
            val p: Probability = Probability("cert-1", 0.52, 2, "1.2.752.116.1.1.1.1.3",
                    "F438A", "1.2.752.116.1.1.1.1.3", "F43",
                    Diagnosprediktionstatus.DIAGNOSKOD_PA_HOGRE_NIVA.value(),
                    LocalDateTime.now().minusDays(4), "VAST", "44235")
            with(p) {
                ownOpinion = OwnOpinion("careGiver1", "careunit2", p, EgenBedomningRiskType.LAGRE.value(), LocalDateTime.now().minusDays(4))
                val answer: PatientAnswer = PatientAnswer(1)
                with(answer) {
                    predictionResponse = PredictionResponse(
                            answer = "Svar 1A",
                            predictionId = "svarsId-1a",
                            isDefault = false,
                            priority = 1,
                            question = PredictionQuestion("Fråga 1", "Frågan är svaret", "frageId-1",
                                    listOf(
                                            PredictionResponse("answer alternative 1-1!", "svarsId-1a", true, 1, null),
                                            PredictionResponse("answer alternative 1-2!", "svarsId-1b", false, 1, null)
                                    ), 1
                            )
                    )
                    probability = p
                }
                patientAnswers = listOf(answer)

                return@thenAnswer listOf(p)
            }
        }
    }

    @Test
    fun testCreateInfoWithCalculation() {
        val extraParams = mapOf(
                LOCATION_KEY to mapOf(
                        REGION_KEY to "VAST",
                        ZIP_CODE_KEY to "44235"
                ),
                QUESTIONS_AND_ANSWERS_KEY to mapOf(
                        "frageId-1" to "svarsId-1a",
                        "frageId-2" to "svarsId-2c"
                )
        )

        val info = predictionInformationModule.getInfo(listOf(testPerson), extraParams, "careUnitHsaId", true)

        val diagnosPrediktioner:List<Diagnosprediktion>? = info[testPerson]
        val diagnosPrediktion:Diagnosprediktion = diagnosPrediktioner!![0]
        with (diagnosPrediktion) {
            println("inkommandeDiagnos: $inkommandediagnos \n" +
                    "prevalens: $prevalens \n" +
                    "sannolikhetOverGransvarde: $sannolikhetOvergransvarde \n" +
                    "forklaringsinformation: $forklaringsinformation \n" +
                    "risksignal.beskrivning: ${risksignal.beskrivning} \n" +
                    "risksignal.riskkategori: ${risksignal.riskkategori} \n" +
                    "diagnosPrediktionsStatus:  $diagnosprediktionstatus \n" +
                    "lakarbedomningRisk: $lakarbedomningRisk \n" +
                    "prediktionsfaktorer: $prediktionsfaktorer \n" +
                    "berakningstidpunkt: $berakningstidpunkt")
            assertEquals("F438A", inkommandediagnos.code)
            assertEquals("1.2.752.116.1.1.1.1.3", inkommandediagnos.codeSystem)
            assertEquals(0.32, prevalens)
            assertEquals(0.52, sannolikhetOvergransvarde)
            assertEquals("Hög risk att sjukfallet varar i mer än 90 dagar", risksignal.beskrivning)
            assertEquals(2, risksignal.riskkategori)
            assertEquals(null, lakarbedomningRisk)
            assertNotNull(berakningstidpunkt)
            assertEquals(Diagnosprediktionstatus.OK, diagnosprediktionstatus)

        }
    }

    @Test
    fun testCreateInfoWithHistoricPrediction() {
        val info = predictionInformationModule.getInfo(listOf(testPerson), mapOf(), "careUnitHsaId", false)

        val diagnosPrediktioner:List<Diagnosprediktion>? = info[testPerson]
        val diagnosPrediktion:Diagnosprediktion = diagnosPrediktioner!![0]
        with (diagnosPrediktion) {
            assertEquals("F438A", inkommandediagnos.code)
            assertEquals("1.2.752.116.1.1.1.1.3", inkommandediagnos.codeSystem)
            assertEquals(0.32, prevalens)
            assertEquals(0.52, sannolikhetOvergransvarde)
            assertEquals("Hög risk att sjukfallet varar i mer än 90 dagar", risksignal.beskrivning)
            assertEquals(2, risksignal.riskkategori)
            assertNotNull(berakningstidpunkt)
            assertEquals("F43", diagnos.code)
            assertEquals("1.2.752.116.1.1.1.1.3", diagnos.codeSystem)
            assertEquals(EgenBedomningRiskType.LAGRE, lakarbedomningRisk)
            assertEquals(Diagnosprediktionstatus.DIAGNOSKOD_PA_HOGRE_NIVA, diagnosprediktionstatus)
            assertEquals("44235", prediktionsfaktorer.postnummer)
            assertEquals("Region", prediktionsfaktorer.fragasvar[0].frageidSrs)
            assertEquals("VAST", prediktionsfaktorer.fragasvar[0].svarsidSrs)
            assertEquals("frageId-1", prediktionsfaktorer.fragasvar[1].frageidSrs)
            assertEquals("svarsId-1a", prediktionsfaktorer.fragasvar[1].svarsidSrs)

            println("inkommandeDiagnos: $inkommandediagnos \n" +
                    "prevalens: $prevalens \n" +
                    "sannolikhetOverGransvarde: $sannolikhetOvergransvarde \n" +
                    "forklaringsinformation: $forklaringsinformation \n" +
                    "risksignal.beskrivning: ${risksignal.beskrivning} \n" +
                    "risksignal.riskkategori: ${risksignal.riskkategori} \n" +
                    "diagnosPrediktionsStatus:  $diagnosprediktionstatus \n" +
                    "lakarbedomningRisk: $lakarbedomningRisk \n" +
                    "prediktionsfaktorer: $prediktionsfaktorer \n" +
                    "berakningstidpunkt: $berakningstidpunkt")
        }
    }

    @Test
    fun testPredictionRiskRange() {
        assertEquals(1, predictionInformationModule.calculateRisk(0.38))
        assertEquals(2, predictionInformationModule.calculateRisk(0.39))
        assertEquals(2, predictionInformationModule.calculateRisk(0.40))

        assertEquals(2, predictionInformationModule.calculateRisk(0.61))
        assertEquals(2, predictionInformationModule.calculateRisk(0.6200))
        assertEquals(3, predictionInformationModule.calculateRisk(0.6205))

        assertEquals(0, predictionInformationModule.calculateRisk(Double.NaN))
    }

}
