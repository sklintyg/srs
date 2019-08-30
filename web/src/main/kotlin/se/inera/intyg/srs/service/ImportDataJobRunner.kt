package se.inera.intyg.srs.service

import org.springframework.stereotype.Component

@Component
class ImportDataJobRunner (
        questionUpdater: ModelVariablesFileUpdateService,
        recAndPrevalenceUpdater: MeasuresAndPrevalenceFileUpdateService,
        statUpdater: StatisticsFileUpdateService
)
{
    init {
        statUpdater.doUpdate()
        questionUpdater.doUpdate()
        recAndPrevalenceUpdater.doUpdate()
    }
}