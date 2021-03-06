package ru.radiationx.anilibria.model.data.remote.parsers

import org.json.JSONArray
import ru.radiationx.anilibria.entity.app.feed.ScheduleItem
import ru.radiationx.anilibria.entity.app.schedule.ScheduleDay
import ru.radiationx.anilibria.entity.app.search.SuggestionItem
import ru.radiationx.anilibria.model.data.remote.IApiUtils
import javax.inject.Inject

class ScheduleParser @Inject constructor(
        private val apiUtils: IApiUtils
) {

    fun schedule(jsonResponse: JSONArray, releaseParser: ReleaseParser): List<ScheduleDay> {
        val result = mutableListOf<ScheduleDay>()
        for (i in 0 until jsonResponse.length()) {
            val jsonItem = jsonResponse.getJSONObject(i)
            val releases = releaseParser.releases(jsonItem.getJSONArray("items"))
            val strDay = jsonItem.getString("day")
            val item = ScheduleDay(
                    ScheduleDay.toCalendarDay(strDay),
                    releases.map { ScheduleItem(it) }
            )
            result.add(item)
        }
        return result
    }
}