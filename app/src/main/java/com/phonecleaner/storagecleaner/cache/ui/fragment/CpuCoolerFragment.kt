package com.phonecleaner.storagecleaner.cache.ui.fragment

import android.animation.Animator
import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.databinding.FragmentCpuCoolerBinding
import com.phonecleaner.storagecleaner.cache.base.BaseFragment
import com.phonecleaner.storagecleaner.cache.data.model.entity.AppInstalled
import com.phonecleaner.storagecleaner.cache.data.model.liveData.State
import com.phonecleaner.storagecleaner.cache.extension.toast
import com.phonecleaner.storagecleaner.cache.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.random.Random

@AndroidEntryPoint
class CpuCoolerFragment : BaseFragment() {
    private lateinit var binding: FragmentCpuCoolerBinding
    private val viewModel: AppViewModel by viewModels()
    private val listAppInstalled: ArrayList<AppInstalled> = arrayListOf()
    override fun initData() {
        super.initData()
        viewModel.getAppInstalled(true)
        viewModel.listAppLiveData.observe(this@CpuCoolerFragment) { it ->
            if (it.getStatus() == State.DataStatus.SUCCESS) {
                val data = it.getData()
                data?.let {
                    val listApp = getLimitListApp(it)
                    listAppInstalled.clear()
                    listAppInstalled.addAll(listApp)
                    binding.cpuCooler.showListApp(listAppInstalled)
                    showProgress(listApp.size)
                }
            } else if (it.getStatus() == State.DataStatus.ERROR) {
                findNavController().popBackStack()
            }
        }
    }

    private fun getLimitListApp(listApp: ArrayList<AppInstalled>): ArrayList<AppInstalled> {
        val appCount = (MIN_APP_COUNT..MAX_APP_COUNT).random()
        val newList = ArrayList<AppInstalled>()
        if (listApp.size > appCount) {
            newList.addAll(listApp.subList(0, appCount))
        } else {
            newList.addAll(listApp)
        }
        return newList
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCpuCoolerBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun initListener() {
        super.initListener()
        binding.topAppBar.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun showProgress(total: Int) {
        val animatorText: ValueAnimator = ValueAnimator.ofInt(0, 99).apply {
            duration = 6000
            repeatCount = 0
            addUpdateListener { animation ->
                val isDelay: Boolean = Random.nextInt(12) > 10
                if (!isDelay) Handler(Looper.getMainLooper()).postDelayed(
                    {}, Random.nextInt(1000, 2000).toLong()
                )
                val animatedValue = animation.animatedValue as Int
                binding.textProgress.text = getTextProgress(animatedValue)
            }
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                }

                override fun onAnimationEnd(animation: Animator) {
                    binding.textProgress.text = getTextProgress(100)
                    binding.cpuCooler.stopAnim()
                    doWithContext { context ->
                        context.toast(getString(R.string.info_clear_total, total))
                    }
                }

                override fun onAnimationCancel(animation: Animator) {

                }

                override fun onAnimationRepeat(animation: Animator) {
                }
            })
        }
        animatorText.start()
    }

    override fun baseBackPressed() {
        super.baseBackPressed()
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack()
                }
            })
    }

    private fun getTextProgress(progress: Int): String = "$progress %"

    companion object {
        const val MIN_APP_COUNT = 32
        const val MAX_APP_COUNT = 54
    }
}