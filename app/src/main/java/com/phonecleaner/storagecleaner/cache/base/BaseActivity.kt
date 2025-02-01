package com.phonecleaner.storagecleaner.cache.base

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.data.model.entity.DataSave
import com.phonecleaner.storagecleaner.cache.viewmodel.DataViewModel
import com.phonecleaner.storagecleaner.cache.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.Locale
import java.util.Locale.setDefault

@AndroidEntryPoint
open class BaseActivity : AppCompatActivity() {
    private val myTag: String = this::class.java.simpleName
    private val dataViewModel: DataViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()
    private var backPressedTime = 0L
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLanguages()
        try {
            checkDropboxMail()
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        } catch (ex: Exception) {
            Timber.tag(myTag).e("exception: ${ex.message}")
        }
    }

    private fun checkDropboxMail() {
//        lifecycleScope.launch {
//            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                dataViewModel.dropboxMail.collect { mail ->
//                    if (mail.isNotEmpty()) {
//                        SingletonDropboxMail.getInstance().mail = mail
//                    }
//                }
//            }
//        }
    }

    private fun setLanguages() {
        val config = resources.configuration
        val displayLanguage = DataSave.language ?: "en"
        val locale = Locale(displayLanguage)
        setDefault(locale)
        config.locale = locale
        createConfigurationContext(config)
        resources.updateConfiguration(config, resources.displayMetrics)
        dataViewModel.setLanguage(displayLanguage)
    }

    fun addFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .add(R.id.navController, fragment, fragment.id.toString()).commit()
    }

    fun removeFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().remove(fragment).commit()
    }
}