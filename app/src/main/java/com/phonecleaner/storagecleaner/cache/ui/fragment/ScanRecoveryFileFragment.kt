package com.phonecleaner.storagecleaner.cache.ui.fragment

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.databinding.FragmentScanRecoveryFileBinding
import com.phonecleaner.storagecleaner.cache.base.BaseFragment
import com.phonecleaner.storagecleaner.cache.data.model.liveData.State
import com.phonecleaner.storagecleaner.cache.extension.toast
import com.phonecleaner.storagecleaner.cache.viewmodel.FileRecoveryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.random.Random

@AndroidEntryPoint
class ScanRecoveryFileFragment : BaseFragment() {
    private lateinit var binding: FragmentScanRecoveryFileBinding
    private val viewModel: FileRecoveryViewModel by navGraphViewModels(R.id.clean_graph_xml) {
        defaultViewModelProviderFactory
    }
    private var recoveryStatus = State.DataStatus.LOADING
    private var forceCancelAnim = false
    private lateinit var anim: ValueAnimator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentScanRecoveryFileBinding.inflate(inflater, container, false)
        return return binding.root
    }

    override fun doWork() {
        super.doWork()
        viewModel.loadFileRecovery()
    }

    override fun initUi() {
        super.initUi()
        var process = 0
        val stopPercentage = Random.nextInt(10) + 90
        context?.let {
            binding.scanningView.setImage(
                BitmapFactory.decodeResource(it.resources, R.drawable.ic_scan_file_recovery)
            )
            binding.scanningView.setRepeatAnimCount(ValueAnimator.INFINITE)

            anim = ValueAnimator.ofInt(1, 100).apply {
                addUpdateListener {
                    if (recoveryStatus == State.DataStatus.SUCCESS) {
                        process += 1
                    } else {
                        if (!isPause) {
                            val isDelay = Random.nextInt(12) > 10
                            if (isDelay) {
                                isPause = true
                                Handler(Looper.getMainLooper()).postDelayed({
                                    isPause = false
                                }, Random.nextInt(1000, 2000).toLong())
                            } else {
                                if (process < stopPercentage) {
                                    process += 1
                                }
                            }
                        }
                    }
                    if (process > 100) {
                        process = 100
                    }
                    binding.tvProgress.text = String.format("%d%%", process)
                    binding.progress.progress = process
                    if (process >= 100) {
                        anim.cancel()
                    }
                }

                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {

                    }

                    override fun onAnimationEnd(animation: Animator) {
                        if (!forceCancelAnim) {
                            if (recoveryStatus == State.DataStatus.SUCCESS || recoveryStatus == State.DataStatus.ERROR) {
                                Handler(Looper.getMainLooper()).postDelayed({
                                    findNavController().navigate(R.id.listFileRecoveryFragment)
                                }, 1000)
                            }
                        }
                    }

                    override fun onAnimationCancel(animation: Animator) {
                    }

                    override fun onAnimationRepeat(animation: Animator) {
                    }
                })

                repeatCount = ValueAnimator.INFINITE
                start()
            }
        }
    }


    override fun initListener() {
        super.initListener()
        binding.topAppBar.btnBack.setOnClickListener { findNavController().popBackStack() }
    }


    override fun initData() {
        super.initData()
        initObservers()
        viewModel.loadFileRecovery()
    }

    private fun initObservers() {
        viewModel.fileRecoveryLiveData.observe(viewLifecycleOwner) { it ->
            when (it.getStatus()) {
                State.DataStatus.LOADING -> {
                    recoveryStatus = State.DataStatus.LOADING
                }

                State.DataStatus.SUCCESS -> {
                    recoveryStatus = State.DataStatus.SUCCESS
                }

                State.DataStatus.ERROR -> {
                    recoveryStatus = State.DataStatus.ERROR
                    doWithContext {
                        it.toast(it.getString(R.string.common_error))
                    }
                }

                else -> {

                }
            }
        }
    }

    override fun baseBackPressed() {
        super.baseBackPressed()
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack()
                }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        forceCancelAnim = true
        if (this::anim.isInitialized) {
            anim.cancel()
        }
    }

    private var isPause = false

}