package ru.radiationx.anilibria.data.api

/**
 * Created by mintrocket on 04.12.2017.
 */
class Paginated<T>(val data: T) {
    var current: Int = 1
    var allPages = 1
    var itemsPerPage = 10
    var total = 0

    fun isEnd(): Boolean {
        return current >= allPages
    }
}
