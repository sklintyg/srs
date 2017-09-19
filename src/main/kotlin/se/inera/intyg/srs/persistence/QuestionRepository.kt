package se.inera.intyg.srs.persistence

import org.springframework.data.repository.CrudRepository

interface QuestionRepository : CrudRepository<PredictionQuestion, Long>
