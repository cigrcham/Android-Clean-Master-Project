package com.phonecleaner.storagecleaner.cache.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.phonecleaner.storagecleaner.cache.base.BaseActivity
import com.phonecleaner.storagecleaner.cache.databinding.ActivityStartViewBinding
import com.phonecleaner.storagecleaner.cache.utils.Constants
import com.phonecleaner.storagecleaner.cache.viewmodel.DataViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StartViewActivity : BaseActivity() {
    private lateinit var binding: ActivityStartViewBinding
    private val dataViewModel: DataViewModel by viewModels()
    private var value: String? = ""
    private var passCode = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        if (intent.extras != null) {
//            if (intent.extras!!.keySet().contains(Constants.ACTION_TYPE)) {
//                value = intent.extras!![Constants.ACTION_TYPE].toString()
//            }
//        }
        binding = ActivityStartViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initObserve()
//        resetAllData()
//        if (!PurchaseManager.getInstance().isRemovedAds(this)) {
//            NativeManager.createNativesAds(this, object : AdNativeListening() {
//                override fun onNativeAdLoaded(p0: NativeAd) {
//                    AppAdmob.listNativeAd.add(p0)
//                }
//            })
//
//            AdsFullManager.loadInterstitial(this)
//            countDownTimer.start()
//        } else {
//            gotoMain()
//        }
    }

    private fun initObserve() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                dataViewModel.passcode.collect { code ->
                    passCode = code
                    gotoMain()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

    }

    private fun gotoMain() {
        if (passCode.isBlank() || passCode.isEmpty()) {
            intent = Intent(this@StartViewActivity, MainActivity::class.java)
            intent.putExtra(Constants.ACTION_TYPE, value)
            startActivity(intent)
            finish()
        } else {
            intent = Intent(this@StartViewActivity, LockActivity::class.java)
            intent.putExtra(Constants.CODE, passCode)
            intent.putExtra(Constants.ACTION_TYPE, value)
            startActivity(intent)
            finish()
        }
    }
}