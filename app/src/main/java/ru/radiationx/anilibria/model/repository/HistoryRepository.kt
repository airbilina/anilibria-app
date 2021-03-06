package ru.radiationx.anilibria.model.repository

import io.reactivex.Observable
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.model.data.holders.HistoryHolder
import ru.radiationx.anilibria.model.system.SchedulersProvider
import javax.inject.Inject

/**
 * Created by radiationx on 18.02.18.
 */
class HistoryRepository @Inject constructor(
        private val schedulers: SchedulersProvider,
        private val historyStorage: HistoryHolder
) {
    fun observeReleases(): Observable<MutableList<ReleaseItem>> = historyStorage
            .observeEpisodes()
            .map { it.asReversed() }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun putRelease(releaseItem: ReleaseItem) = historyStorage.putRelease(releaseItem)

    fun removeRelease(id: Int) = historyStorage.removerRelease(id)
}