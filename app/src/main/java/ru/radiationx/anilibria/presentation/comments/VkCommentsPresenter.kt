package ru.radiationx.anilibria.presentation.comments

import com.arellomobile.mvp.InjectViewState
import ru.radiationx.anilibria.entity.app.page.VkComments
import ru.radiationx.anilibria.entity.app.release.ReleaseFull
import ru.radiationx.anilibria.model.data.holders.AuthHolder
import ru.radiationx.anilibria.model.interactors.ReleaseInteractor
import ru.radiationx.anilibria.model.repository.PageRepository
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.terrakok.cicerone.Router
import javax.inject.Inject

@InjectViewState
class VkCommentsPresenter @Inject constructor(
        private val pageRepository: PageRepository,
        private val releaseInteractor: ReleaseInteractor,
        private val authHolder: AuthHolder,
        private val router: Router,
        private val errorHandler: IErrorHandler
) : BasePresenter<VkCommentsView>(router) {

    private var currentData: ReleaseFull? = null
    private var currentVkComments: VkComments? = null
    var releaseId = -1
    var releaseIdCode: String? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        val releaseItem = releaseInteractor.getItem(releaseId, releaseIdCode)
        if (releaseItem == null) {
            loadRelease()
        } else {
            currentData = ReleaseFull(releaseItem)
        }
        loadData()
        authHolder.observeVkAuthChange()
                .subscribe {
                    updateComments()
                }
                .addToDisposable()
    }

    fun authRequest(url: String) {
        router.navigateTo(Screens.Auth(Screens.AuthVk(url)))
    }

    private fun loadData() {
        pageRepository
                .getComments()
                .subscribe({
                    currentVkComments = it
                    updateComments()
                }, {
                    errorHandler.handle(it)
                })
                .addToDisposable()
    }

    private fun loadRelease() {
        releaseInteractor
                .loadRelease(releaseId, releaseIdCode)
                .subscribe({ release ->
                    currentData = release
                    updateComments()
                }) {
                    errorHandler.handle(it)
                }
                .addToDisposable()
    }

    private fun updateComments() {
        if (currentData != null && currentVkComments != null) {
            currentVkComments?.also {
                viewState.showBody(VkComments(
                        "${it.baseUrl}release/${currentData?.code}.html",
                        it.script
                ))
            }
        }
    }
}