package ru.radiationx.anilibria.model.data.remote.parsers

import org.json.JSONArray
import ru.radiationx.anilibria.entity.app.release.GenreItem
import ru.radiationx.anilibria.entity.app.release.YearItem
import ru.radiationx.anilibria.entity.app.search.SuggestionItem
import ru.radiationx.anilibria.extension.nullString
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.model.data.remote.IApiUtils
import javax.inject.Inject

class SearchParser @Inject constructor(
        private val apiUtils: IApiUtils
) {

    fun fastSearch(jsonResponse: JSONArray): List<SuggestionItem> {
        val result: MutableList<SuggestionItem> = mutableListOf()
        for (i in 0 until jsonResponse.length()) {
            val jsonItem = jsonResponse.getJSONObject(i)
            val item = SuggestionItem()

            item.id = jsonItem.getInt("id")
            item.code = jsonItem.getString("code")
            item.names.addAll(jsonItem.getJSONArray("names").let { names ->
                (0 until names.length()).map {
                    apiUtils.escapeHtml(names.getString(it)).toString()
                }
            })
            item.poster = Api.BASE_URL_IMAGES + jsonItem.nullString("poster")
            result.add(item)
        }
        return result
    }

    fun years(jsonResponse: JSONArray): List<YearItem> {
        val result: MutableList<YearItem> = mutableListOf()
        for (i in 0 until jsonResponse.length()) {
            val yearText = jsonResponse.getString(i)
            val genreItem = YearItem().apply {
                title = yearText
                value = yearText
            }
            result.add(genreItem)
        }
        return result
    }

    fun genres(jsonResponse: JSONArray): List<GenreItem> {
        val result: MutableList<GenreItem> = mutableListOf()
        for (i in 0 until jsonResponse.length()) {
            val genreText = jsonResponse.getString(i)
            val genreItem = GenreItem().apply {
                title = genreText.capitalize()
                value = genreText
            }
            result.add(genreItem)
        }
        return result
    }

}