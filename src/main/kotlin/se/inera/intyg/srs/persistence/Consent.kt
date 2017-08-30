package se.inera.intyg.srs.persistence

import org.hibernate.annotations.Type
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class Consent(val personnummer: String,
              val samtycke: Boolean,
              val vardgivareId: String,
              @Type(type = "org.jadira.usertype.dateandtime.threeten.PersistentLocalDateTime")
              val skapatTid: LocalDateTime,
              @Id @GeneratedValue(strategy = GenerationType.AUTO)
              val id: Long = -1) {

    override fun toString() = "Consent(personnummer: $personnummer, vardenhet: $vardgivareId, samtycke: $samtycke," +
            " tidpunkt: $skapatTid)"
}