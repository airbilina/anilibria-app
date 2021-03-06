package ru.radiationx.anilibria.model.data.storage

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import ru.radiationx.anilibria.model.data.holders.AuthHolder
import javax.inject.Inject

/**
 * Created by radiationx on 30.12.17.
 */
class AuthStorage @Inject constructor() : AuthHolder {

    private val vkAuthRelay = PublishRelay.create<Boolean>()

    override fun observeVkAuthChange(): Observable<Boolean> = vkAuthRelay.hide()

    override fun changeVkAuth(value: Boolean) {
        vkAuthRelay.accept(value)
    }
}