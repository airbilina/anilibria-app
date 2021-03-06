package ru.radiationx.anilibria.presentation.page

import com.arellomobile.mvp.InjectViewState
import ru.radiationx.anilibria.model.data.remote.api.PageApi
import ru.radiationx.anilibria.model.repository.PageRepository
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.terrakok.cicerone.Router
import javax.inject.Inject

/**
 * Created by radiationx on 13.01.18.
 */
@InjectViewState
class PagePresenter @Inject constructor(
        private val pageRepository: PageRepository,
        private val router: Router,
        private val errorHandler: IErrorHandler
) : BasePresenter<PageView>(router) {

    var pageId: String? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        pageId?.let {
            if (PageApi.PAGE_IDS.contains(it)) {
                loadPage(it)
            }
        }
    }

    private fun loadPage(pageId: String) {
        viewState.setRefreshing(true)
        pageRepository
                .getPage(pageId)
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({ page ->
                    viewState.showPage(page)
                }, {
                    errorHandler.handle(it)
                })
                .addToDisposable()
    }
}