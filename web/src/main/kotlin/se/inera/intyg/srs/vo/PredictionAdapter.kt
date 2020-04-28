package se.inera.intyg.srs.vo

import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v3.Diagnosprediktionstatus
import java.time.LocalDateTime

interface PredictionAdapter {

    /**
     * Get a prediction of the probability that the sick leave will last longer than X days.
     * See implementing class for more specific details
     * @param person The person on sick leave
     * @param diagnosis The main diagnosis
     * @param extraParams A map of other parameters for the prediction, see implementing class for explanation, e.g. RAdapter.kt
     * @param daysIntoSickLeave Number of days into the sick leave when th prediction is made, the first calculation is done based on day 15
     */
    fun getPrediction(person: Person, diagnosis: Diagnosis, extraParams: Map<String, Map<String, String>>, daysIntoSickLeave: Int=15): Prediction
}

class Prediction(val diagnosis: String, val prediction: Double?, val status: Diagnosprediktionstatus, val timestamp: LocalDateTime)
