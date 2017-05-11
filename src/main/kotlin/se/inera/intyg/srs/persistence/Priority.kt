package se.inera.intyg.srs.persistence

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne

@Entity
class Priority(val priority: Int,
               @ManyToOne
               val recommendation: Recommendation,
               @Id @GeneratedValue(strategy = GenerationType.AUTO)
               val id: Long = -1) {

    override fun toString() = "Priority(id=$id, priority='$priority', recommendation='$recommendation')"

}
