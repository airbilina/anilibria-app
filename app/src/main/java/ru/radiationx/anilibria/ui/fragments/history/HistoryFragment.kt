package ru.radiationx.anilibria.ui.fragments.history

import android.app.SearchManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.lapism.searchview.SearchBehavior
import com.lapism.searchview.SearchEditText
import com.lapism.searchview.SearchView
import kotlinx.android.synthetic.main.fragment_list.*
import kotlinx.android.synthetic.main.fragment_main_base.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.di.extensions.getDependency
import ru.radiationx.anilibria.di.extensions.injectDependencies
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.extension.dpToPx
import ru.radiationx.anilibria.extension.getColorFromAttr
import ru.radiationx.anilibria.model.data.holders.AppThemeHolder
import ru.radiationx.anilibria.presentation.history.HistoryPresenter
import ru.radiationx.anilibria.presentation.history.HistoryView
import ru.radiationx.anilibria.ui.adapters.PlaceholderListItem
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.fragments.SharedProvider
import ru.radiationx.anilibria.ui.fragments.feed.FeedToolbarShadowController
import ru.radiationx.anilibria.ui.fragments.release.list.ReleasesAdapter
import ru.radiationx.anilibria.ui.widgets.UniversalItemDecoration
import ru.radiationx.anilibria.utils.DimensionHelper
import ru.radiationx.anilibria.utils.ShortcutHelper
import ru.radiationx.anilibria.utils.ToolbarHelper
import javax.inject.Inject

/**
 * Created by radiationx on 18.02.18.
 */
class HistoryFragment : BaseFragment(), HistoryView, SharedProvider, ReleasesAdapter.ItemListener {

    override var sharedViewLocal: View? = null

    override fun getSharedView(): View? {
        val sharedView = sharedViewLocal
        sharedViewLocal = null
        return sharedView
    }


    private var searchView: SearchView? = null

    @Inject
    lateinit var appThemeHolder: AppThemeHolder


    private val adapter = ReleasesAdapter(this, PlaceholderListItem(
            R.drawable.ic_history,
            R.string.placeholder_title_nodata_base,
            R.string.placeholder_desc_nodata_history
    ))

    @InjectPresenter
    lateinit var presenter: HistoryPresenter

    @ProvidePresenter
    fun provideHistoryPresenter(): HistoryPresenter = getDependency(screenScope, HistoryPresenter::class.java)

    override val statusBarVisible: Boolean = true

    override fun getLayoutResource(): Int = R.layout.fragment_list

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies(screenScope)
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchView = SearchView(coordinator_layout.context)
        ToolbarHelper.fixInsets(toolbar)

        toolbar.apply {
            title = "История"
            setNavigationOnClickListener { presenter.onBackPressed() }
            setNavigationIcon(R.drawable.ic_toolbar_arrow_back)
        }

        toolbar.menu.apply {
            add("Поиск")
                    .setIcon(R.drawable.ic_toolbar_search)
                    .setOnMenuItemClickListener {
                        searchView?.open(true, it)
                        false
                    }
                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }

        FeedToolbarShadowController(
                recyclerView,
                appbarLayout
        ) {
            updateToolbarShadow(it)
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = this@HistoryFragment.adapter
            /*addItemDecoration(UniversalItemDecoration()
                    .fullWidth(true)
                    .spacingDp(8f)
            )*/
        }


        coordinator_layout.addView(searchView)
        searchView?.layoutParams = (searchView?.layoutParams as CoordinatorLayout.LayoutParams?)?.apply {
            width = CoordinatorLayout.LayoutParams.MATCH_PARENT
            height = CoordinatorLayout.LayoutParams.WRAP_CONTENT
            behavior = SearchBehavior()
        }
        searchView?.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                z = 16f
            }
            setNavigationIcon(R.drawable.ic_toolbar_arrow_back)
            close(false)
            setVoice(false)
            setShadow(false)
            setDivider(false)
            setTheme(when (appThemeHolder.getTheme()) {
                AppThemeHolder.AppTheme.LIGHT -> SearchView.THEME_LIGHT
                AppThemeHolder.AppTheme.DARK -> SearchView.THEME_DARK
            })
            shouldClearOnClose = true
            version = SearchView.VERSION_MENU_ITEM
            setVersionMargins(SearchView.VERSION_MARGINS_MENU_ITEM)

            hint = "Название релиза"

            /*setOnOpenCloseListener(object : SearchView.OnOpenCloseListener {
                override fun onOpen(): Boolean {
                    return false
                }

                override fun onClose(): Boolean {
                    return false
                }
            })*/


            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    presenter.localSearch(newText.orEmpty())
                    return false
                }
            })
            val cardview = findViewById<CardView>(com.lapism.searchview.R.id.cardView)
            cardview.apply {
                radius = dpToPx(8).toFloat()
                cardElevation = dpToPx(2).toFloat()
                setCardBackgroundColor(context.getColorFromAttr(R.attr.cardBackground))
                layoutParams = (layoutParams as ViewGroup.MarginLayoutParams).apply {
                    marginStart = dpToPx(16)
                    marginEnd = dpToPx(16)
                    bottomMargin = dpToPx(8)
                }
            }


            val searchEditText = findViewById<SearchEditText>(com.lapism.searchview.R.id.searchEditText_input)
            searchEditText.apply {
                layoutParams = (layoutParams as ViewGroup.MarginLayoutParams).apply {
                    marginStart = dpToPx(12)
                }
            }
        }
    }

    override fun updateDimens(dimensions: DimensionHelper.Dimensions) {
        super.updateDimens(dimensions)
        searchView?.layoutParams = (searchView?.layoutParams as CoordinatorLayout.LayoutParams?)?.apply {
            topMargin = dimensions.statusBar
        }
        searchView?.requestLayout()
    }

    private fun closeSearch(): Boolean {
        if (searchView?.isSearchOpen == true) {
            searchView?.close(true)
            return true
        }
        return false
    }

    override fun onBackPressed(): Boolean {
        if (closeSearch())
            return true
        presenter.onBackPressed()
        return true
    }

    override fun onDestroyView() {
        closeSearch()
        super.onDestroyView()
    }

    override fun setRefreshing(refreshing: Boolean) {}

    override fun showReleases(releases: List<ReleaseItem>) {
        adapter.bindItems(releases)
    }

    override fun onItemClick(item: ReleaseItem, position: Int) {
        presenter.onItemClick(item)
    }

    override fun onItemLongClick(item: ReleaseItem): Boolean {
        //presenter.onItemLongClick(item)
        context?.let {
            AlertDialog.Builder(it)
                    .setItems(arrayOf("Добавить на главный экран", "Удалить")) { _, which ->
                        when (which) {
                            0 -> ShortcutHelper.addShortcut(item)
                            1 -> presenter.onDeleteClick(item)
                        }
                    }
                    .show()
        }
        return false
    }

    override fun onLoadMore() {}

    override fun onItemClick(position: Int, view: View) {
        this.sharedViewLocal = view
    }

}