package ru.radiationx.anilibria.ui.fragments

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.annotation.LayoutRes
import android.support.design.widget.CollapsingToolbarLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.arellomobile.mvp.MvpAppCompatFragment
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_main_base.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.di.Scopes
import ru.radiationx.anilibria.di.extensions.getDependency
import ru.radiationx.anilibria.extension.addTo
import ru.radiationx.anilibria.extension.gone
import ru.radiationx.anilibria.extension.visible
import ru.radiationx.anilibria.ui.common.BackButtonListener
import ru.radiationx.anilibria.ui.common.ScopeProvider
import ru.radiationx.anilibria.utils.DimensionHelper
import ru.radiationx.anilibria.utils.DimensionsProvider

/* Created by radiationx on 18.11.17. */

abstract class BaseFragment : MvpAppCompatFragment(), ScopeProvider, BackButtonListener {

    companion object {
        const val ARG_SCREEN_SCOPE = "arg_screen_scope"
    }

    private val dimensionsProvider = getDependency(DimensionsProvider::class.java)
    private val disposables = CompositeDisposable()

    protected open val needToolbarShadow = true
    protected open val statusBarVisible = false

    @LayoutRes
    protected open fun getLayoutResource(): Int = View.NO_ID

    @LayoutRes
    protected open fun getBaseLayout(): Int = R.layout.fragment_main_base

    override val screenScope: String by lazy {
        arguments?.getString(ARG_SCREEN_SCOPE, null) ?: Scopes.APP
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val newView: View? = inflater.inflate(getBaseLayout(), container, false)
        if (getLayoutResource() != View.NO_ID) {
            inflater.inflate(getLayoutResource(), newView?.findViewById(R.id.fragment_content), true)
        }
        return newView
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //updateToolbarShadow(true)
    }

    protected fun updateToolbarShadow(isVisible: Boolean) {
        toolbar_shadow_prelp?.visible(isVisible && needToolbarShadow)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setStatusBarVisibility(statusBarVisible)
        dimensionsProvider
                .observe()
                .subscribe { dimension ->
                    toolbar?.post {
                        toolbar?.let {
                            localUpdateDimens(dimension)
                        }
                    }
                    localUpdateDimens(dimension)
                }
                .addTo(disposables)
    }

    private fun localUpdateDimens(dimensions: DimensionHelper.Dimensions) {
        val correctedDimens = dimensions.copy()
        correctedDimens.statusBar = if (baseStatusBar?.visibility == View.VISIBLE) {
            0
        } else {
            correctedDimens.statusBar
        }
        updateDimens(correctedDimens)
    }

    open fun updateDimens(dimensions: DimensionHelper.Dimensions) {
        toolbar?.layoutParams = (toolbar?.layoutParams as CollapsingToolbarLayout.LayoutParams?)?.apply {
            topMargin = dimensions.statusBar
        }
    }

    fun setStatusBarColor(color: Int) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            return
        }
        baseStatusBar?.setBackgroundColor(color)
    }

    fun setStatusBarVisibility(isVisible: Boolean) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            baseStatusBar?.gone()
            return
        }
        baseStatusBar?.visible(isVisible)
    }

    protected fun hideSoftwareKeyboard() {
        activity?.also {
            val inputManager = it.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            it.currentFocus?.let {
                inputManager?.hideSoftInputFromWindow(it.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        }
    }

    protected fun showSoftwareKeyboard(editText: EditText) {
        activity?.also {
            val inputManager = it.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            inputManager?.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }
}
