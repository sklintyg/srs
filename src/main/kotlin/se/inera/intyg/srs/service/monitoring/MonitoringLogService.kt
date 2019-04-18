package se.inera.intyg.srs.service.monitoring

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Marker
import org.apache.logging.log4j.MarkerManager
import se.inera.intyg.srs.service.LOCATION_KEY
import se.inera.intyg.srs.service.QUESTIONS_AND_ANSWERS_KEY
import se.inera.intyg.srs.service.REGION_KEY

fun logPrediction(input: Map<String, Map<String, String>>, diagnosisCode: String, limit: String, sex: String, ageCategory: String, prediction: String,
                  predictionLevel: Int, statusCode: String, certificateId: String, hsaId: String) {
    val log = LogManager.getLogger()
    val location = input[LOCATION_KEY] ?: emptyMap()
    val qna = input[QUESTIONS_AND_ANSWERS_KEY] ?: emptyMap()
    log.info(Markers.MONITORING.marker(), "{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{}",
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

enum class Markers(val logName: String) {
    MONITORING("MONITORING") {
        override fun marker(): Marker = MarkerManager.getMarker(this.logName)
    };

    abstract fun marker(): Marker
}

