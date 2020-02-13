package se.inera.intyg.srs.service.monitoring

import org.slf4j.LoggerFactory
import se.inera.intyg.srs.service.LOCATION_KEY
import se.inera.intyg.srs.service.QUESTIONS_AND_ANSWERS_KEY
import se.inera.intyg.srs.service.REGION_KEY

/**
 * Was used for monitoring predictions in an pre-OpenShift version where the ELK stack wasn't used for monitoring.
 * At the moment it only prints a row in the log
 */
fun logPrediction(input: Map<String, Map<String, String>>, diagnosisCode: String, limit: String, sex: String, ageCategory: String, prediction: String,
                  predictionLevel: Int, statusCode: String, certificateId: String, hsaId: String) {

    val LOG = LoggerFactory.getLogger("se.inera.intyg.srs.service.monitoring.MonitoringLogServiceKt")
    val location = input[LOCATION_KEY] ?: emptyMap()
    val qna = input[QUESTIONS_AND_ANSWERS_KEY] ?: emptyMap()

    LOG.info("{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{}",
            diagnosisCode,
            limit,
            prediction,
            predictionLevel,
            location[REGION_KEY] ?: "",
            sex,
            ageCategory,
            qna["SA_SyssStart_fct"] ?: "",
            qna["NoCareAtStart"] ?: "",
            qna["SA_1_gross"] ?: "",
            qna["edu_cat_fct"] ?: "",
            qna["Visits_yearBefore_all_r1_median"] ?: "",
            qna["birth_cat_fct"] ?: "",
            qna["SA_ExtentFirst"] ?: "",
            qna["comorbidity"] ?: "",
            qna["DP_atStart"] ?: "",
            qna["Vtid_yeahBefore_all_r1_Median"] ?: "",
            qna["fam_cat_4_cat_fct"] ?: "",
            statusCode,
            certificateId,
            hsaId)

}

