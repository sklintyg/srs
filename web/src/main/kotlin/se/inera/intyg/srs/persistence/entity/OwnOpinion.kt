package se.inera.intyg.srs.persistence.entity

import java.time.LocalDateTime
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne

@Entity
class OwnOpinion(val careGiverId: String,
                 val careUnitId: String,
                 @OneToOne
                 @JoinColumn(name="probability_id", referencedColumnName = "id")
                 val probability: Probability,
                 var opinion: String,
                 var createdTime: LocalDateTime,
                 @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
                 val id: Long = -1) {

    override fun toString() = "OwnOpinion(careGiverId: $careGiverId, careUnitId: $careUnitId, " +
            "probabilityId: ${probability.id}, opinion: $opinion, tidpunkt: $createdTime)"
}