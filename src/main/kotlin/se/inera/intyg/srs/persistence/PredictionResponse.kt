package se.inera.intyg.srs.persistence

import javax.persistence.Entity
import javax.persistence.Id

@Entity
class PredictionResponse(@Id
                         val id: Long,
                         val answer: String,
                         val predictionId: String,
                         val isDefault: Boolean,
                         val priority: Int) {

}
