package se.inera.intyg.srs.vo

import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v2.Diagnosprediktionstatus
import java.time.LocalDateTime

interface PredictionAdapter {

    fun getPrediction(person: Person, diagnosis: Diagnosis, extraParams: Map<String, Map<String, String>>): Prediction
}

class Prediction(val diagnosis: String, val prediction: Double?, val status: Diagnosprediktionstatus, val timestamp: LocalDateTime)
