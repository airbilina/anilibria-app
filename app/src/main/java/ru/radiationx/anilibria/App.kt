package ru.radiationx.anilibria

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.support.multidex.MultiDex
import android.support.v7.app.AppCompatDelegate
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import biz.source_code.miniTemplator.MiniTemplator
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import io.reactivex.disposables.Disposables
import io.reactivex.plugins.RxJavaPlugins
import ru.radiationx.anilibria.di.AppModule
import ru.radiationx.anilibria.di.Scopes
import ru.radiationx.anilibria.di.extensions.DI
import ru.radiationx.anilibria.model.system.SchedulersProvider
import ru.radiationx.anilibria.model.system.messages.SystemMessenger
import toothpick.Toothpick
import toothpick.configuration.Configuration
import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.charset.Charset

/*  Created by radiationx on 05.11.17. */
class App : Application() {
    companion object {

        init {
            //AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }

        lateinit var instance: App
            private set

    }

    private var messengerDisposable = Disposables.disposed()

    lateinit var staticPageTemplate: MiniTemplator
    lateinit var vkCommentsTemplate: MiniTemplator

    val vkCommentCssFixLight: String by lazy {
        assets.open("styles/vk_comments_fix_light.css").bufferedReader().use {
            it.readText()
        }
    }

    val vkCommentCssFixDark: String by lazy {
        assets.open("styles/vk_comments_fix_dark.css").bufferedReader().use {
            it.readText()
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        if (BuildConfig.FLAVOR.equals("appDev")) {
            MultiDex.install(this)
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        //Fabric.with(this, Crashlytics())
        val config = YandexMetricaConfig.newConfigBuilder("48d49aa0-6aad-407e-a738-717a6c77d603").build()
        YandexMetrica.activate(applicationContext, config)
        YandexMetrica.enableActivityAutoTracking(this)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }

        RxJavaPlugins.setErrorHandler { throwable ->
            Log.d("S_DEF_LOG", "RxJavaPlugins errorHandler $throwable")
            throwable.printStackTrace()
        }

        initDependencies()

        findTemplate("static_page")?.let { staticPageTemplate = it }
        findTemplate("vk_comments")?.let { vkCommentsTemplate = it }

        val systemMessenger = DI.get(SystemMessenger::class.java)
        val schedulers = DI.get(SchedulersProvider::class.java)

        /*messengerDisposable = systemMessenger
                .observe()
                .observeOn(schedulers.ui())
                .subscribe {
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                }*/

        initImageLoader(this)
        appVersionCheck()
    }

    private fun initDependencies() {
        Toothpick.setConfiguration(Configuration.forProduction())
        val scope = Toothpick.openScope(Scopes.APP)
        scope.installModules(AppModule(this))

        Log.e("lalala", "initDependencies ${Toothpick.openScope(Scopes.APP)}")
    }

    private fun appVersionCheck() {
        try {
            val prefKey = "app.versions.history"
            val defaultPreferences = DI.get(SharedPreferences::class.java)
            val history = defaultPreferences
                    .getString(prefKey, "")
                    ?.split(";")
                    ?.filter { it.isNotBlank() }
                    ?.map { it.toInt() }
                    ?: emptyList()


            var lastAppCode = 0

            var disorder = false
            history.forEach {
                if (it < lastAppCode) {
                    disorder = true
                }
                lastAppCode = it
            }
            val currentAppCode = ("" + BuildConfig.VERSION_CODE).toInt()

            if (lastAppCode < currentAppCode) {
                if (lastAppCode > 0) {
                    val appMigration = AppMigration(currentAppCode, lastAppCode, history)
                    appMigration.start()
                }

                val list = history.map { it.toString() }.toMutableList()
                list.add(currentAppCode.toString())
                defaultPreferences
                        .edit()
                        .putString(prefKey, TextUtils.join(";", list))
                        .apply()
            }
            if (disorder) {
                val errMsg = "AniLibria: Нарушение порядка версий, программа может работать не стабильно!"
                Toast.makeText(this, errMsg, Toast.LENGTH_SHORT).show()
            }
        } catch (ex: Throwable) {
            ex.printStackTrace()
            val errMsg = "Сбой при проверке локальной версии."
            YandexMetrica.reportError(errMsg, ex)
            val uiErr = "$errMsg\nПрограмма может работать не стабильно! Переустановите программу."
            Toast.makeText(this, uiErr, Toast.LENGTH_LONG).show()
        }
    }

    private fun findTemplate(name: String): MiniTemplator? {
        var template: MiniTemplator? = null
        try {
            val stream = assets.open("templates/$name.html")
            val charset: Charset = Charset.forName("utf-8")
            template = try {
                MiniTemplator.Builder().build(stream, charset)
            } catch (e: Exception) {
                e.printStackTrace()
                MiniTemplator.Builder().build(ByteArrayInputStream("Template error!".toByteArray(charset)), charset)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return template
    }

    private val defaultOptionsUIL: DisplayImageOptions.Builder = DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .resetViewBeforeLoading(true)
            .cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.ARGB_8888)
            .handler(Handler())
            .displayer(FadeInBitmapDisplayer(500, true, true, false))

    private fun initImageLoader(context: Context) {
        val config = ImageLoaderConfiguration.Builder(context)
                .threadPoolSize(5)
                .threadPriority(Thread.MIN_PRIORITY)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(UsingFreqLimitedMemoryCache(5 * 1024 * 1024)) // 5 Mb
                .diskCacheFileNameGenerator(HashCodeFileNameGenerator())
                .defaultDisplayImageOptions(defaultOptionsUIL.build())
                .build()
        ImageLoader.getInstance().init(config)
    }

}
