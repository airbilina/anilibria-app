package ru.radiationx.anilibria.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.support.v4.content.pm.ShortcutInfoCompat
import android.support.v4.content.pm.ShortcutManagerCompat
import android.support.v4.graphics.drawable.IconCompat
import android.util.Log
import android.view.View
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.extension.centerCrop
import ru.radiationx.anilibria.extension.createAvatar
import ru.radiationx.anilibria.ui.activities.main.IntentActivity
import kotlin.math.min

object ShortcutHelper {

    fun addShortcut(data: ReleaseItem) {
        ImageLoader.getInstance().loadImage(data.poster, object : SimpleImageLoadingListener() {
            override fun onLoadingComplete(imageUri: String?, view: View?, loadedImage: Bitmap) {
                val minSize = min(loadedImage.width, loadedImage.height)
                val bmp = loadedImage.centerCrop(minSize, minSize).createAvatar(isCircle = true)
                ShortcutHelper.addShortcut(data, bmp)
            }
        })
    }

    fun addShortcut(data: ReleaseItem, bitmap: Bitmap) = addShortcut(
            App.instance,
            data.code ?: "release_${data.id}",
            (data.title ?: data.titleEng).toString(),
            data.names.joinToString(" / ") { it },
            data.link.orEmpty(),
            bitmap
    )

    private fun addShortcut(
            context: Context,
            id: String,
            shortLabel: String,
            longLabel: String,
            url: String,
            bitmap: Bitmap
    ) {
        Log.e("lalala", "addShortcut $id, $shortLabel, $longLabel, $url")
        val shortcut = ShortcutInfoCompat.Builder(context, id)
                .setShortLabel(shortLabel)
                .setLongLabel(longLabel)
                .setIcon(IconCompat.createWithBitmap(bitmap))
                /*.setIntent(Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(url)
                ))*/
                .setIntent(Intent(App.instance.applicationContext, IntentActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    data = Uri.parse(url)
                })
                .build()

        if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            val callbackIntent = ShortcutManagerCompat.createShortcutResultIntent(context, shortcut)

            val successCallback = PendingIntent.getBroadcast(context, 0, callbackIntent, 0)

            ShortcutManagerCompat.requestPinShortcut(context, shortcut, successCallback.intentSender)
        }
    }

}