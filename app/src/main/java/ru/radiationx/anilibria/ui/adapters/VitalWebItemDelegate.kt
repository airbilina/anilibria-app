package ru.radiationx.anilibria.ui.adapters

import android.graphics.Bitmap
import android.support.v7.widget.RecyclerView
import android.view.MotionEvent
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_vital_web.*
import kotlinx.android.synthetic.main.item_vital_web_card.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.vital.VitalItem
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.utils.Utils


/**
 * Created by radiationx on 13.01.18.
 */
class VitalWebItemDelegate(val inDetail: Boolean = false) : AppAdapterDelegate<VitalWebListItem, ListItem, VitalWebItemDelegate.ViewHolder>(
        R.layout.item_vital_web_card,
        { it is VitalWebListItem },
        { ViewHolder(it, inDetail) }
) {

    override fun bindData(item: VitalWebListItem, holder: ViewHolder) = holder.bind(item.item)

    class ViewHolder(
            override val containerView: View,
            private val inDetail: Boolean
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        private lateinit var currentItem: VitalItem

        init {
            if (inDetail) {
                item_card.cardElevation = 0f
            }
            vitalWebView.settings.apply {
                layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
            }
            vitalWebView.setOnTouchListener { _, event -> event.action == MotionEvent.ACTION_MOVE }
            vitalWebView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    webSwitcher.displayedChild = 0
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    webSwitcher.displayedChild = 1
                }

                @Suppress("OverridingDeprecatedMember")
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    Utils.externalLink(url.toString())
                    return true
                }
            }
        }

        fun bind(item: VitalItem) {
            if (!::currentItem.isInitialized || currentItem != item) {
                currentItem = item
                vitalWebView.easyLoadData(Api.WIDGETS_SITE_URL, item.contentText)
            }
        }
    }
}