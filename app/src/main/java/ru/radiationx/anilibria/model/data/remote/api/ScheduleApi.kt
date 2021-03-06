package ru.radiationx.anilibria.model.data.remote.api

import io.reactivex.Single
import org.json.JSONArray
import org.json.JSONObject
import ru.radiationx.anilibria.entity.app.Paginated
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.entity.app.schedule.ScheduleDay
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.model.data.remote.ApiResponse
import ru.radiationx.anilibria.model.data.remote.IClient
import ru.radiationx.anilibria.model.data.remote.parsers.ReleaseParser
import ru.radiationx.anilibria.model.data.remote.parsers.ScheduleParser
import javax.inject.Inject

class ScheduleApi @Inject constructor(
        private val client: IClient,
        private val releaseParser: ReleaseParser,
        private val scheduleParser: ScheduleParser
) {

    fun getSchedule(): Single<List<ScheduleDay>> {
        val args: MutableMap<String, String> = mutableMapOf(
                "query" to "schedule",
                "filter" to "id,torrents,playlist,favorite,moon,blockedInfo",
                "rm" to "true"
        )
        return client.post(Api.API_URL, args)
                .compose(ApiResponse.fetchResult<JSONArray>())
                .map { scheduleParser.schedule(it, releaseParser) }
    }

}