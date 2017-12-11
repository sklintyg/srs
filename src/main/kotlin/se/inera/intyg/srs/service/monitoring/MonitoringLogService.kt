package se.inera.intyg.srs.service.monitoring

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Marker
import org.apache.logging.log4j.MarkerManager

fun logPrediction(input: Map<String, String>, diagnosisCode: String, limit: String, sex: String, ageCategory: String, prediction: String,
                  predictionLevel: Int, statusCode: String, certificateId: String, hsaId: String) {
    val log = LogManager.getLogger()
    log.info(Markers.MONITORING.marker(), "{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{}",
            diagnosisCode,
            limit,
            prediction,
            predictionLevel,
            input["Region"] ?: "",
            sex,
            ageCategory,
            input["SA_SyssStart_fct"] ?: "",
            input["NoCareAtStart"] ?: "",
            input["SA_1_gross"] ?: "",
            input["edu_cat_fct"] ?: "",
            input["Visits_yearBefore_all_r1_median"] ?: "",
            input["birth_cat_fct"] ?: "",
            input["SA_ExtentFirst"] ?: "",
            input["comorbidity"] ?: "",
            input["DP_atStart"] ?: "",
            input["Vtid_yeahBefore_all_r1_Median"] ?: "",
            input["fam_cat_4_cat_fct"] ?: "",
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

