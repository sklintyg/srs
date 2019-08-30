package se.inera.intyg.srs.persistence.entity

import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToOne

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