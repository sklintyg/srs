package se.inera.intyg.srs

import org.apache.logging.log4j.LogManager
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Atgardstyp.OBS
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Atgardstyp.REK
import se.inera.intyg.srs.persistence.DiagnosisRepository
import se.inera.intyg.srs.persistence.Measure
import se.inera.intyg.srs.persistence.MeasurePriority
import se.inera.intyg.srs.persistence.MeasurePriorityRepository
import se.inera.intyg.srs.persistence.MeasureRepository
import se.inera.intyg.srs.persistence.PredictionDiagnosis
import se.inera.intyg.srs.persistence.PredictionPriority
import se.inera.intyg.srs.persistence.PredictionPriorityRepository
import se.inera.intyg.srs.persistence.PredictionQuestion
import se.inera.intyg.srs.persistence.PredictionResponse
import se.inera.intyg.srs.persistence.QuestionRepository
import se.inera.intyg.srs.persistence.Recommendation
import se.inera.intyg.srs.persistence.RecommendationRepository
import se.inera.intyg.srs.persistence.ResponseRepository
import se.inera.intyg.srs.persistence.InternalStatisticRepository

@Configuration
@Profile("bootstrap")
class BootstrapData {

        private val log = LogManager.getLogger()

    @Bean
    fun init(
            measureRepo: MeasureRepository, recommendationRepo: RecommendationRepository,
            prioRepo: MeasurePriorityRepository, internalStatisticRepo: InternalStatisticRepository, responseRepo: ResponseRepository,
            questionRepo: QuestionRepository, diagnosisRepo: DiagnosisRepository, predictPrioRepo: PredictionPriorityRepository) = CommandLineRunner {

        log.debug("Bootstrapping data")

        val recommendation01 = recommendationRepo.save(Recommendation( REK,
                "Test: patientens övervägande, testar att ha en ganska lång rubrik här för att se hur applikationen beter sig. " +
                        "Det är inte något som patienten egentligen behöver bry sig om men kan vara bra för användaren.",
                "Patienten bör överväga att kontakta företagshälsovård och arbetsgivare för att avgränsa eller byta arbetsuppgifter, eller t.o.m. byta yrke eller arbetsplats",1))
        val recommendation02 = recommendationRepo.save(Recommendation( REK, "Test: remiss behandling", "Remiss till behandling med psykoterapeutiska metoder", 2))
        val recommendation03 = recommendationRepo.save(Recommendation( REK, "Test: Information till patienten",
                "Ge patienten lättillgänglig information om diagnosen och behandlingsmöjligheter. " +
                "Förlängd text för att testa om det fungerar med den begränsning av antal rader som skall vara synliga från start i användargränssnittet. " +
                "De rader som inte syns kan fällas ut genom att man klickar på visa mer. För att den funktionen skall aktiveras krävs " +
                        "att texten är lite längre än de andra texterna som ligger här.", 3))
        val recommendation04 = recommendationRepo.save(Recommendation(REK,
                "Test: Företagshälsovård","patienten bör överväga att kontakta företagshälsovård och arbetsgivare för att undersöka möjligheter till ergonomisk rådgivning och arbetsanpassning.", 4))
        val recommendation05 = recommendationRepo.save(Recommendation(REK, "Test: Kontaktförmedling","förmedling av kontakt med fysioterapeut", 5))
        val recommendation06 = recommendationRepo.save(Recommendation(REK, "Test: Träning","FaR med konditions- och styrketräning", 6))
        val recommendation07 = recommendationRepo.save(Recommendation(REK, "Test: KBT","remiss till behandling med KBT", 7))
        val recommendation08 = recommendationRepo.save(Recommendation(REK, "Test: rTMS","remiss till behandling med rTMS", 8))
        val recommendation09 = recommendationRepo.save(Recommendation(REK, "Test: Nätterapi","Remiss till Internetförmedlad KBT via Internetbaserat stöd och behandling", 9))
        val recommendation10 = recommendationRepo.save(Recommendation( REK, "Test: SSRI","SSRI-läkemedel", 10))
        val recommendation11 = recommendationRepo.save(Recommendation( REK, "Test: deltidssjukskrivning", "partiell sjukskrivning ".repeat(16), 11))
        val recommendation12 = recommendationRepo.save(Recommendation( REK, "Test: Gymma","FaR med regelbunden styrketräning för att förebygger nya besvär", 12))
        val recommendation13 = recommendationRepo.save(Recommendation( OBS, "Test: Obs","Observation1 <b>Observation1 med bold-tagg</b> Observation1 Observation1 Observation1 Observation1", 13))
        val recommendation14 = recommendationRepo.save(Recommendation( OBS, "Test: Obs2",
                "Observation2 <i>Observation2 med italics-tag</i> Observation2 Observation2 Observation2 Observation2 " +
                        "Observation2 Observation2 Observation2 Observation2 Observation2 Observation2 Observation2 Observation2 " +
                        "Observation2 Observation2 Observation2 ", 14))
        val recommendation15 = recommendationRepo.save(Recommendation( OBS, "Test: Grundlig utredning",
                "Grundlig utredning av patientens hälsa är viktig för att bedöma svårighetsgraden på besvären, ställa " +
                        "diagnos och för att ge adekvat behandling. Det är viktigt att ställa frågor om vad patienten tror är orsak till " +
                        "besvären, hur sömnen fungerar, hur hemsituationen och arbetssituationen ser ut och hur länge patienten har haft " +
                        "problem. Genom att kartlägga detta noggrant säkerställs att patienten får adekvata behandlingsinsatser.", 15))
        val recommendation16 = recommendationRepo.save(Recommendation(OBS, "Test: Grundlig utredning två",
                "Grundlig utredning av patientens hälsa är viktig för att bedöma svårighetsgraden på besvären, " +
                        "ställa diagnos och för att ge adekvat behandling. Det är viktigt att ställa frågor om vad patienten tror är " +
                        "orsak till besvären, hur sömnen fungerar, hur hemsituationen och arbetssituationen ser ut och hur länge " +
                        "patienten har haft problem. Genom att kartlägga detta noggrant säkerställs att patienten får " +
                        "adekvata behandlingsinsatser.", 16))
        val recommendation17 = recommendationRepo.save(Recommendation(REK, "Test: Ha de gött",
                "Ät gott, lev gott, må bra", 17))
        val recommendation18 = recommendationRepo.save(Recommendation(REK,
                "Test: Sov", "Patienten behöver sova", 18))

        val m1 = measureRepo.save(Measure("F438A", "Utmattningssyndrom", "1.0" ))
        prioRepo.save(MeasurePriority(1, recommendation01, m1))
        prioRepo.save(MeasurePriority(2, recommendation02, m1))
        prioRepo.save(MeasurePriority(3, recommendation03, m1))
        prioRepo.save(MeasurePriority(4, recommendation15, m1))
        prioRepo.save(MeasurePriority(5, recommendation16, m1))
        prioRepo.save(MeasurePriority(5, recommendation17, m1))
        prioRepo.save(MeasurePriority(5, recommendation18, m1))

        val m2 = measureRepo.save(Measure("M75", "Sjukdomstillstånd i skulderled", "1.0"))
        prioRepo.save(MeasurePriority(1, recommendation04, m2))
        prioRepo.save(MeasurePriority(2, recommendation05, m2))

        val m3 = measureRepo.save(Measure("F32", "Depressiv episod", "1.0"))
        prioRepo.save(MeasurePriority(1, recommendation06, m3))
        prioRepo.save(MeasurePriority(2, recommendation07, m3))
        prioRepo.save(MeasurePriority(3, recommendation08, m3))

        val m4 = measureRepo.save(Measure("F41", "Andra ångestsyndrom", "1.0"))
        prioRepo.save(MeasurePriority(1, recommendation07, m4))
        prioRepo.save(MeasurePriority(2, recommendation09, m4))
        prioRepo.save(MeasurePriority(3, recommendation10, m4))

        val m5 = measureRepo.save(Measure("M54", "Ryggvärk", "1.0"))
        prioRepo.save(MeasurePriority(1, recommendation11, m5))
        prioRepo.save(MeasurePriority(2, recommendation12, m5))

        val m6 = measureRepo.save(Measure("M79", "Reumatism, ospecificerad", "1.0"))
        prioRepo.save(MeasurePriority(1, recommendation11, m6))
        prioRepo.save(MeasurePriority(2, recommendation12, m6))
        prioRepo.save(MeasurePriority(1, recommendation13, m6))
        prioRepo.save(MeasurePriority(2, recommendation14, m6))

        val question01 = questionRepo.save(PredictionQuestion(1,
                "Sysselsättningsstatus",
                "Vilken är din nuvarande sysselsättning?",
                "SA_SyssStart_fct",
                listOf(responseRepo.save(PredictionResponse(1, "Yrkesarbetar", "work", true, 1)),
                        responseRepo.save(PredictionResponse(2, "Arbetslös", "unemp", false, 2)),
                        responseRepo.save(PredictionResponse(3, "Föräldraledig", "p.leave", false, 3)),
                        responseRepo.save(PredictionResponse(4, "Studerar", "stud", false, 4)))))

        val question02 = questionRepo.save(PredictionQuestion(2,
                "Påbörjades det pågående sjukskrivningsfallet inom primärvården?",
                "Sjukskrevs du från primärvården i början av fallet?",
                "NoCareAtStart",
                listOf(responseRepo.save(PredictionResponse(5, "Ja", "NoCareAtStart", true, 1)),
                        responseRepo.save(PredictionResponse(6, "Nej", "CareAtStart", false, 2)))))

        val question03 = questionRepo.save(PredictionQuestion(3,
                "Tidigare sjukskrivning",
                "Ungefär hur många dagar har du varit sjukskriven under de senaste 12 månaderna. (Alternativ formulering: Om du tänker tillbaka till juni förra året, ungefär hur många dagar har du varit sjukskriven sedan dess?) (dvs specificera samma månad som besöket sker)",
                "SA_1_gross",
                listOf(responseRepo.save(PredictionResponse(7, "0 - eller korta fall", "0", true, 1)),
                        responseRepo.save(PredictionResponse(8, "15-90 dagar", "(0,90]", false, 2)),
                        responseRepo.save(PredictionResponse(9, "91-180", "(90,180]", false, 3)),
                        responseRepo.save(PredictionResponse(10, "181-365", "(180,366]", false, 4)))))

        val question04 = questionRepo.save(PredictionQuestion(4,
                "Högsta utbildningsnivå",
                "Har du gått på universitet eller högskola? (inklusive ha gått kurser)",
                "edu_cat_fct",
                listOf(responseRepo.save(PredictionResponse(11, "Ja", ">12 years", false, 1)),
                        responseRepo.save(PredictionResponse(12, "Nej", "No university", true, 2)))))

        val question05 = questionRepo.save(PredictionQuestion(5,
                "Läkarbesök, sjukhus senaste 12 månaderna - ej PV",
                "Har du haft mer än ett läkarbesök utanför primärvården de senaste 12 månaderna? (t.ex. på sjukhus)",
                "Visits_yearBefore_all_r1_median",
                listOf(responseRepo.save(PredictionResponse(13, "Ja", "aboveMedian", false, 1)),
                        responseRepo.save(PredictionResponse(14, "Nej", "LessT2V", true, 2)))))

        val question06 = questionRepo.save(PredictionQuestion(6,
                "Född i Sv",
                "Är du född i Sverige?",
                "birth_cat_fct",
                listOf(responseRepo.save(PredictionResponse(15, "Ja", "SW", true, 1)),
                        responseRepo.save(PredictionResponse(16, "Nej", "World", false, 2)))))

        val question07 = questionRepo.save(PredictionQuestion(7,
                "Grad av sjukskrivning tidigare i fallet?",
                "Var du först sjukskriven på hel- eller deltid i det här fallet?",
                "SA_ExtentFirst",
                listOf(responseRepo.save(PredictionResponse(17, "100%", "1", true, 1)),
                        responseRepo.save(PredictionResponse(18, "75%", "0.75", false, 2)),
                        responseRepo.save(PredictionResponse(19, "50%", "0.5", false, 3)),
                        responseRepo.save(PredictionResponse(20, "25%", "0.25", false, 4)))))

        val question08 = questionRepo.save(PredictionQuestion(8,
                "Samsjuklighet",
                "Hur många andra långvariga sjukdomar har du?",
                "comorbidity",
                listOf(responseRepo.save(PredictionResponse(21, "0 eller 1", "no", true, 1)),
                        responseRepo.save(PredictionResponse(22, "2 eller fler", "yes", false, 2)))))

        val question09 = questionRepo.save(PredictionQuestion(9,
                "Partiell sjuk- eller aktivitetsersättning",
                "Har du deltids förtidspension (sjukersättning alt aktivitetsersättning) nu?",
                "DP_atStart",
                listOf(responseRepo.save(PredictionResponse(23, "Nej", "0", true, 1)),
                        responseRepo.save(PredictionResponse(24, "Ja", "1", false, 2)))))

        val question10 = questionRepo.save(PredictionQuestion(10,
                "Inlagd sjukhus",
                "Har du varit inlagd på sjukhus mer än en dag de senaste 12 månaderna? (räkna ej pga okomplicerad förlossning)",
                "Vtid_yearBefore_all_r1_median",
                listOf(responseRepo.save(PredictionResponse(25, "Nej", "LessT2V", true, 1)),
                        responseRepo.save(PredictionResponse(26, "Ja", "aboveMedian", false, 2)))))

        val question11 = questionRepo.save(PredictionQuestion(11,
                "Familjesituation",
                "Är du ensamstående (Alt:  är du sambo/gift)?",
//                "fam_cat_split",
                "fam_cat_4_cat_fct",
                listOf(responseRepo.save(PredictionResponse(27, "sambo/gift", "Married", true, 1)),
                        responseRepo.save(PredictionResponse(28, "Singel", "Unmarried", false, 2)))))

        diagnosisRepo.save(PredictionDiagnosis(1, "M75", 0.25,
                listOf(predictPrioRepo.save(PredictionPriority(1, question01)),
                        predictPrioRepo.save(PredictionPriority(2, question02)),
                        predictPrioRepo.save(PredictionPriority(3, question03)),
                        predictPrioRepo.save(PredictionPriority(4, question04)),
                        predictPrioRepo.save(PredictionPriority(5, question05)),
                        predictPrioRepo.save(PredictionPriority(6, question06)))))
        diagnosisRepo.save(PredictionDiagnosis(2, "M54", 0.26,
                listOf(predictPrioRepo.save(PredictionPriority(1, question01)),
                        predictPrioRepo.save(PredictionPriority(2, question03)),
                        predictPrioRepo.save(PredictionPriority(3, question07)),
                        predictPrioRepo.save(PredictionPriority(4, question08)),
                        predictPrioRepo.save(PredictionPriority(5, question05)),
                        predictPrioRepo.save(PredictionPriority(6, question02)))))
        diagnosisRepo.save(PredictionDiagnosis(3, "F41", 0.48,
                listOf(predictPrioRepo.save(PredictionPriority(1, question01)),
                        predictPrioRepo.save(PredictionPriority(2, question03)),
                        predictPrioRepo.save(PredictionPriority(3, question05)),
                        predictPrioRepo.save(PredictionPriority(4, question07)),
                        predictPrioRepo.save(PredictionPriority(5, question04)),
                        predictPrioRepo.save(PredictionPriority(6, question09)))))
        diagnosisRepo.save(PredictionDiagnosis(4, "F32", 0.52,
                listOf(predictPrioRepo.save(PredictionPriority(1, question01)),
                        predictPrioRepo.save(PredictionPriority(2, question03)),
                        predictPrioRepo.save(PredictionPriority(3, question07)),
                        predictPrioRepo.save(PredictionPriority(4, question05)),
                        predictPrioRepo.save(PredictionPriority(5, question04)),
                        predictPrioRepo.save(PredictionPriority(6, question08)))))
        diagnosisRepo.save(PredictionDiagnosis(5, "F43", 0.37,
                listOf(predictPrioRepo.save(PredictionPriority(1, question01)),
                        predictPrioRepo.save(PredictionPriority(2, question03)),
                        predictPrioRepo.save(PredictionPriority(3, question07)),
                        predictPrioRepo.save(PredictionPriority(4, question04)),
                        predictPrioRepo.save(PredictionPriority(5, question05)),
                        predictPrioRepo.save(PredictionPriority(6, question06)))))
        diagnosisRepo.save(PredictionDiagnosis(6, "M79", 0.37,
                listOf(predictPrioRepo.save(PredictionPriority(1, question01)),
                        predictPrioRepo.save(PredictionPriority(2, question03)),
                        predictPrioRepo.save(PredictionPriority(3, question07)),
                        predictPrioRepo.save(PredictionPriority(4, question08)),
                        predictPrioRepo.save(PredictionPriority(5, question02)),
                        predictPrioRepo.save(PredictionPriority(6, question09)))))
        diagnosisRepo.save(PredictionDiagnosis(7, "M53", 0.46,
                listOf(predictPrioRepo.save(PredictionPriority(1, question01)),
                        predictPrioRepo.save(PredictionPriority(2, question03)),
                        predictPrioRepo.save(PredictionPriority(3, question07)),
                        predictPrioRepo.save(PredictionPriority(4, question05)),
                        predictPrioRepo.save(PredictionPriority(5, question09)),
                        predictPrioRepo.save(PredictionPriority(6, question08)))))
        diagnosisRepo.save(PredictionDiagnosis(8, "M17", 0.53,
                listOf(predictPrioRepo.save(PredictionPriority(1, question01)),
                        predictPrioRepo.save(PredictionPriority(2, question02)),
                        predictPrioRepo.save(PredictionPriority(3, question03)),
                        predictPrioRepo.save(PredictionPriority(4, question10)),
                        predictPrioRepo.save(PredictionPriority(5, question04)),
                        predictPrioRepo.save(PredictionPriority(6, question05)))))
        diagnosisRepo.save(PredictionDiagnosis(9, "M16", 0.59,
                listOf(predictPrioRepo.save(PredictionPriority(1, question01)),
                        predictPrioRepo.save(PredictionPriority(2, question02)),
                        predictPrioRepo.save(PredictionPriority(3, question03)),
                        predictPrioRepo.save(PredictionPriority(4, question04)),
                        predictPrioRepo.save(PredictionPriority(5, question05)),
                        predictPrioRepo.save(PredictionPriority(6, question10)))))
        diagnosisRepo.save(PredictionDiagnosis(10, "M19", 0.46,
                listOf(predictPrioRepo.save(PredictionPriority(1, question01)),
                        predictPrioRepo.save(PredictionPriority(2, question02)),
                        predictPrioRepo.save(PredictionPriority(3, question03)),
                        predictPrioRepo.save(PredictionPriority(4, question07)),
                        predictPrioRepo.save(PredictionPriority(5, question04)),
                        predictPrioRepo.save(PredictionPriority(6, question10)))))
        diagnosisRepo.save(PredictionDiagnosis(11, "M77", 0.21,
                listOf(predictPrioRepo.save(PredictionPriority(1, question01)),
                        predictPrioRepo.save(PredictionPriority(2, question03)),
                        predictPrioRepo.save(PredictionPriority(3, question02)),
                        predictPrioRepo.save(PredictionPriority(4, question07)),
                        predictPrioRepo.save(PredictionPriority(5, question06)),
                        predictPrioRepo.save(PredictionPriority(6, question11)))))
        diagnosisRepo.save(PredictionDiagnosis(12, "M23", 0.21,
                listOf(predictPrioRepo.save(PredictionPriority(1, question01)),
                        predictPrioRepo.save(PredictionPriority(2, question03)),
                        predictPrioRepo.save(PredictionPriority(3, question07)),
                        predictPrioRepo.save(PredictionPriority(4, question02)),
                        predictPrioRepo.save(PredictionPriority(5, question05)),
                        predictPrioRepo.save(PredictionPriority(6, question04)))))
        diagnosisRepo.save(PredictionDiagnosis(13, "G56", 0.09,
                listOf(predictPrioRepo.save(PredictionPriority(1, question01)),
                        predictPrioRepo.save(PredictionPriority(2, question02)),
                        predictPrioRepo.save(PredictionPriority(3, question04)),
                        predictPrioRepo.save(PredictionPriority(4, question06)),
                        predictPrioRepo.save(PredictionPriority(5, question03)),
                        predictPrioRepo.save(PredictionPriority(6, question07)))))
        diagnosisRepo.save(PredictionDiagnosis(14, "S82", 0.31,
                listOf(predictPrioRepo.save(PredictionPriority(1, question01)),
                        predictPrioRepo.save(PredictionPriority(2, question04)),
                        predictPrioRepo.save(PredictionPriority(3, question10)),
                        predictPrioRepo.save(PredictionPriority(4, question02)),
                        predictPrioRepo.save(PredictionPriority(5, question05)),
                        predictPrioRepo.save(PredictionPriority(6, question03)))))
        diagnosisRepo.save(PredictionDiagnosis(15, "S83", 0.23,
                listOf(predictPrioRepo.save(PredictionPriority(1, question01)),
                        predictPrioRepo.save(PredictionPriority(2, question03)),
                        predictPrioRepo.save(PredictionPriority(3, question02)),
                        predictPrioRepo.save(PredictionPriority(4, question05)),
                        predictPrioRepo.save(PredictionPriority(5, question11)),
                        predictPrioRepo.save(PredictionPriority(6, question06)))))
        diagnosisRepo.save(PredictionDiagnosis(16, "S62", 0.11,
                listOf(predictPrioRepo.save(PredictionPriority(1, question01)),
                        predictPrioRepo.save(PredictionPriority(2, question04)),
                        predictPrioRepo.save(PredictionPriority(3, question06)),
                        predictPrioRepo.save(PredictionPriority(4, question05)),
                        predictPrioRepo.save(PredictionPriority(5, question03)),
                        predictPrioRepo.save(PredictionPriority(6, question10)))))
        diagnosisRepo.save(PredictionDiagnosis(17, "S52", 0.17,
                listOf(predictPrioRepo.save(PredictionPriority(1, question01)),
                        predictPrioRepo.save(PredictionPriority(2, question04)),
                        predictPrioRepo.save(PredictionPriority(3, question10)),
                        predictPrioRepo.save(PredictionPriority(4, question06)),
                        predictPrioRepo.save(PredictionPriority(5, question07)),
                        predictPrioRepo.save(PredictionPriority(6, question05)))))
        diagnosisRepo.save(PredictionDiagnosis(18, "M51", 0.46,
                listOf(predictPrioRepo.save(PredictionPriority(1, question01)),
                        predictPrioRepo.save(PredictionPriority(2, question03)),
                        predictPrioRepo.save(PredictionPriority(3, question02)),
                        predictPrioRepo.save(PredictionPriority(4, question06)),
                        predictPrioRepo.save(PredictionPriority(5, question08)),
                        predictPrioRepo.save(PredictionPriority(6, question04)))))
        diagnosisRepo.save(PredictionDiagnosis(19, "F31", 0.65,
                listOf(predictPrioRepo.save(PredictionPriority(1, question01)),
                        predictPrioRepo.save(PredictionPriority(2, question03)),
                        predictPrioRepo.save(PredictionPriority(3, question11)),
                        predictPrioRepo.save(PredictionPriority(4, question09)),
                        predictPrioRepo.save(PredictionPriority(5, question04)),
                        predictPrioRepo.save(PredictionPriority(6, question05)))))
        diagnosisRepo.save(PredictionDiagnosis(20, "R52", 0.35,
                listOf(predictPrioRepo.save(PredictionPriority(1, question01)),
                        predictPrioRepo.save(PredictionPriority(2, question03)),
                        predictPrioRepo.save(PredictionPriority(3, question07)),
                        predictPrioRepo.save(PredictionPriority(4, question08)),
                        predictPrioRepo.save(PredictionPriority(5, question05)),
                        predictPrioRepo.save(PredictionPriority(6, question04)))))
        diagnosisRepo.save(PredictionDiagnosis(21, "F33", 0.52,
                listOf(predictPrioRepo.save(PredictionPriority(1, question01)),
                        predictPrioRepo.save(PredictionPriority(2, question03)),
                        predictPrioRepo.save(PredictionPriority(3, question07)),
                        predictPrioRepo.save(PredictionPriority(4, question05)),
                        predictPrioRepo.save(PredictionPriority(5, question04)),
                        predictPrioRepo.save(PredictionPriority(6, question08)))))
        diagnosisRepo.save(PredictionDiagnosis(22, "R53", 0.35,
                listOf(predictPrioRepo.save(PredictionPriority(1, question01)),
                        predictPrioRepo.save(PredictionPriority(2, question03)),
                        predictPrioRepo.save(PredictionPriority(3, question07)),
                        predictPrioRepo.save(PredictionPriority(4, question08)),
                        predictPrioRepo.save(PredictionPriority(5, question05)),
                        predictPrioRepo.save(PredictionPriority(6, question04)))))
    }

}
