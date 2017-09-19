package se.inera.intyg.srs.persistence

import org.springframework.data.repository.CrudRepository

interface PredictionPriorityRepository : CrudRepository<PredictionPriority, Long>
