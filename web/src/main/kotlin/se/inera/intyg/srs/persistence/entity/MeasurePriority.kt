package se.inera.intyg.srs.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne

@Entity
class MeasurePriority(

        val priority: Int,

        @ManyToOne()
        val recommendation: Recommendation,

        @ManyToOne
        var measure: Measure? =null,

        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long = -1

) {

    override fun toString() = "MeasurePriority(id=$id, priority='$priority', recommendation='$recommendation')"

}
