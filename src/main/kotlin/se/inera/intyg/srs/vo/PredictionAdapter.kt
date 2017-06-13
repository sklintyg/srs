package se.inera.intyg.srs.vo

interface PredictionAdapter {

    fun getPrediction(person: Person, diagnose: Diagnose): Double
}