package se.inera.intyg.srs.persistence.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.util.*
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Temporal

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
        @Temporal(jakarta.persistence.TemporalType.TIMESTAMP)
        var created: Date = Date(),
        @UpdateTimestamp
        @Column(name = "modified", updatable = false)
        @Temporal(jakarta.persistence.TemporalType.TIMESTAMP)
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
