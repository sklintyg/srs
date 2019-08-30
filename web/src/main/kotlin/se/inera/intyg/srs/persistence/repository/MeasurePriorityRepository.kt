package se.inera.intyg.srs.persistence.repository

import org.springframework.data.repository.CrudRepository
import se.inera.intyg.srs.persistence.entity.MeasurePriority

interface MeasurePriorityRepository : CrudRepository<MeasurePriority, Long>
