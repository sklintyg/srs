package se.inera.intyg.srs.persistence

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne

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
