package se.inera.intyg.srs.persistence.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.Temporal

@Entity
data class PredictionResponse(
        val answer: String,
        val predictionId: String,
        val isDefault: Boolean,
        val priority: Int?,
        val modelVersion: String,
        val forSubdiagnosis: Boolean,
        @ManyToOne
        @JoinColumn(name = "question_id")
        @JsonIgnore
        var question: PredictionQuestion?,
        val automaticSelectionDiagnosisCode: String? = null,
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long = -1,
        @CreationTimestamp
        @Column(name = "created", updatable = false)
        @Temporal(javax.persistence.TemporalType.TIMESTAMP)
        var created: Date = Date(),
        @UpdateTimestamp
        @Column(name = "modified", updatable = false)
        @Temporal(javax.persistence.TemporalType.TIMESTAMP)
        var modified: Date = Date()
) {

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "predictionResponse")
    @JsonIgnore
    var patientAnswers: Collection<PatientAnswer> = emptyList()

    override fun toString(): String {
        return "PredictionResponse(id=$id, answer='$answer', predictionId='$predictionId', isDefault=$isDefault, priority=$priority, " +
                "automaticSelectionDiagnosisCode=$automaticSelectionDiagnosisCode, modelVersion=$modelVersion, forSubdiagnosis=$forSubdiagnosis)"
    }
}
