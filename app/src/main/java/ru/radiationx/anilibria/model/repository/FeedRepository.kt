package ru.radiationx.anilibria.model.repository

import io.reactivex.Single
import ru.radiationx.anilibria.entity.app.Paginated
import ru.radiationx.anilibria.entity.app.feed.FeedItem
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.entity.app.release.ReleaseUpdate
import ru.radiationx.anilibria.model.data.holders.ReleaseUpdateHolder
import ru.radiationx.anilibria.model.data.remote.api.FeedApi
import ru.radiationx.anilibria.model.system.SchedulersProvider
import javax.inject.Inject

class FeedRepository @Inject constructor(
        private val feedApi: FeedApi,
        private val schedulers: SchedulersProvider,
        private val releaseUpdateHolder: ReleaseUpdateHolder
) {

    fun getFeed(page: Int): Single<List<FeedItem>> = feedApi
            .getFeed(page)
            .doOnSuccess {
                val newItems = mutableListOf<ReleaseItem>()
                val updItems = mutableListOf<ReleaseUpdate>()
                it.filterNot { it.release == null }.map { it.release!! }.forEach { item ->
                    val updItem = releaseUpdateHolder.getRelease(item.id)
                    if (updItem == null) {
                        newItems.add(item)
                    } else {
                        item.isNew = item.torrentUpdate > updItem.lastOpenTimestamp || item.torrentUpdate > updItem.timestamp
                    }
                }
                releaseUpdateHolder.putAllRelease(newItems)
                releaseUpdateHolder.updAllRelease(updItems)
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

}