package ru.radiationx.anilibria.ui.fragments.search

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import kotlinx.android.synthetic.main.fragment_main_base.*
import kotlinx.android.synthetic.main.fragment_releases.*
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.release.GenreItem
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.entity.app.search.SearchItem
import ru.radiationx.anilibria.entity.app.vital.VitalItem
import ru.radiationx.anilibria.presentation.search.SearchPresenter
import ru.radiationx.anilibria.presentation.search.SearchView
import ru.radiationx.anilibria.ui.adapters.PlaceholderListItem
import ru.radiationx.anilibria.ui.common.RouterProvider
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.fragments.SharedProvider
import ru.radiationx.anilibria.ui.fragments.release.list.ReleasesAdapter
import ru.radiationx.anilibria.ui.widgets.UniversalItemDecoration
import ru.radiationx.anilibria.utils.ShortcutHelper


class SearchFragment : BaseFragment(), SearchView, SharedProvider, ReleasesAdapter.ItemListener {

    companion object {
        const val ARG_QUERY_TEXT: String = "query"
        const val ARG_GENRE: String = "genre"
    }

    private var searchView: com.lapism.searchview.SearchView? = null
    private lateinit var genresDialog: GenresDialog
    private lateinit var searchMenuItem: MenuItem
    private val adapter = SearchAdapter(this, PlaceholderListItem(
            R.drawable.ic_toolbar_search,
            R.string.placeholder_title_nodata_base,
            R.string.placeholder_desc_nodata_search
    ))
    private val fastAdapter = FastSearchAdapter()
    private var currentTitle: String? = "Поиск"
    private var wasOpenOnPause = true

    @InjectPresenter
    lateinit var presenter: SearchPresenter

    @ProvidePresenter
    fun provideSearchPresenter(): SearchPresenter {
        return SearchPresenter(
                App.injections.searchRepository,
                (parentFragment as RouterProvider).getRouter(),
                App.injections.errorHandler,
                App.injections.releaseUpdateStorage
        )
    }

    override var sharedViewLocal: View? = null

    override fun getSharedView(): View? {
        val sharedView = sharedViewLocal
        sharedViewLocal = null
        return sharedView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.also { bundle ->
            presenter.currentQuery = bundle.getString(ARG_QUERY_TEXT, null)
            bundle.getString(ARG_GENRE, null)?.also {
                presenter.onChangeGenres(listOf(it))
            }
        }
    }

    override fun getLayoutResource(): Int = R.layout.fragment_releases

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        genresDialog = context?.let {
            GenresDialog(it, object : GenresDialog.ClickListener {
                override fun onHide() {
                    presenter.onCloseDialog()
                }

                override fun onCheckedItems(items: List<String>) {
                    Log.e("lululu", "onCheckedItems ${items.size}")
                    presenter.onChangeGenres(items)
                }
            })
        } ?: throw RuntimeException("Burn in hell google! Wtf, why nullable?! Fags...")

        refreshLayout.setOnRefreshListener { presenter.refreshReleases() }

        recyclerView.apply {
            adapter = this@SearchFragment.adapter
            layoutManager = LinearLayoutManager(this.context)
            addItemDecoration(UniversalItemDecoration()
                    .fullWidth(true)
                    .spacingDp(8f)
            )
        }

        //ToolbarHelper.fixInsets(toolbar)
        with(toolbar) {
            title = currentTitle
            /*setNavigationOnClickListener({ presenter.onBackPressed() })
            setNavigationIcon(R.drawable.ic_toolbar_arrow_back)*/
        }

        with(toolbar.menu) {
            searchMenuItem = add("Search")
                    .setIcon(R.drawable.ic_toolbar_search)
                    .setOnMenuItemClickListener {
                        searchView?.open(true)
                        false
                    }
                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)

            add("Settings")
                    .setIcon(R.drawable.ic_toolbar_settings)
                    .setOnMenuItemClickListener {
                        presenter.showDialog()
                        false
                    }
                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }

        searchView = com.lapism.searchview.SearchView(coordinator_layout.context)
        toolbar.addView(searchView)
        searchView?.apply {
            setNavigationIcon(R.drawable.ic_toolbar_arrow_back)
            setOnOpenCloseListener(object : com.lapism.searchview.SearchView.OnOpenCloseListener {
                override fun onOpen(): Boolean {
                    Log.e("kulolo", "onOpen")
                    searchMenuItem.isVisible = false
                    //toolbar?.navigationIcon = null
                    toolbar?.apply {
                        title = null
                        subtitle = null
                    }
                    return false
                }

                override fun onClose(): Boolean {
                    Log.e("kulolo", "onClose")
                    searchMenuItem.isVisible = true
                    //toolbar?.setNavigationIcon(R.drawable.ic_toolbar_arrow_back)
                    toolbar?.apply {
                        title = currentTitle
                    }
                    return false
                }
            })
            setVoice(false)
            setShadow(false)
            version = com.lapism.searchview.SearchView.VERSION_MENU_ITEM
            setVersionMargins(com.lapism.searchview.SearchView.VERSION_MARGINS_MENU_ITEM)
            setOnQueryTextListener(object : com.lapism.searchview.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let {
                        presenter.currentQuery = it
                        presenter.refreshReleases()
                    }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    //newText?.let { presenter.fastSearch(it) }
                    return false
                }
            })
            hint = "Поиск"

            //releaseAdapter = fastAdapter
        }


    }

    override fun onResume() {
        super.onResume()
        Log.e("kulolo", "onResume")
        if (wasOpenOnPause) {
            searchView?.postDelayed({
                if (presenter.isEmpty() && !(parentFragment?.isHidden == true)) {
                    searchView?.open(true, searchMenuItem)
                }
            }, 500)
        }
    }

    override fun onPause() {
        super.onPause()
        wasOpenOnPause = searchView?.isSearchOpen == true
        searchView?.close(false)
    }

    override fun onBackPressed(): Boolean {
        presenter.onBackPressed()
        return true
    }

    override fun showDialog() {
        genresDialog.showDialog()
    }

    override fun showVitalBottom(vital: VitalItem) {

    }

    override fun showVitalItems(vital: List<VitalItem>) {

    }

    override fun setEndless(enable: Boolean) {
        adapter.endless = enable
    }

    override fun showFastItems(items: List<SearchItem>) {
        searchView?.showSuggestions()
        items.forEach {
            Log.e("S_DEF_LOG", "FAST ITEM: ${it.title} : ${it.title}")
        }
        fastAdapter.bindItems(items)
    }

    override fun showGenres(genres: List<GenreItem>) {
        genresDialog.setItems(genres)
    }

    override fun selectGenres(genres: List<String>) {
        genresDialog.setChecked(genres)
    }

    override fun showReleases(releases: List<ReleaseItem>) {
        currentTitle = if (presenter.currentQuery.orEmpty().isEmpty()) {
            "Поиск"
        } else {
            "Поиск: " + presenter.currentQuery
        }
        if (searchMenuItem.isVisible) {
            toolbar.apply {
                title = currentTitle
            }
        }
        adapter.bindItems(releases)
    }

    override fun insertMore(releases: List<ReleaseItem>) {
        adapter.insertMore(releases)
    }

    override fun updateReleases(releases: List<ReleaseItem>) {
        adapter.updateItems(releases)
    }

    override fun onLoadMore() {
        presenter.loadMore()
    }

    override fun setRefreshing(refreshing: Boolean) {
        refreshLayout.isRefreshing = refreshing
    }

    override fun onItemClick(position: Int, view: View) {
        sharedViewLocal = view
    }

    override fun onItemClick(item: ReleaseItem, position: Int) {
        presenter.onItemClick(item)
    }

    override fun onItemLongClick(item: ReleaseItem): Boolean {
        presenter.onItemLongClick(item)
        context?.let {
            AlertDialog.Builder(it)
                    .setItems(arrayOf("Добавить на главный экран")) { dialog, which ->
                        when (which) {
                            0 -> ShortcutHelper.addShortcut(item)
                        }
                    }
                    .show()
        }
        return false
    }

}
