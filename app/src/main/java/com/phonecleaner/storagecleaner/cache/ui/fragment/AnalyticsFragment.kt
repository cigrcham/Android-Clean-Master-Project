package com.phonecleaner.storagecleaner.cache.ui.fragment

import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.base.BaseFragment
import com.phonecleaner.storagecleaner.cache.data.model.entity.AnalyticModel
import com.phonecleaner.storagecleaner.cache.data.model.entity.AppInstalled
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
import com.phonecleaner.storagecleaner.cache.data.model.liveData.State
import com.phonecleaner.storagecleaner.cache.databinding.FragmentAnalyzingBinding
import com.phonecleaner.storagecleaner.cache.extension.managerStoragePermissionGranted
import com.phonecleaner.storagecleaner.cache.extension.observe
import com.phonecleaner.storagecleaner.cache.ui.activity.MainActivity
import com.phonecleaner.storagecleaner.cache.ui.adapters.AnalyticsAdapter
import com.phonecleaner.storagecleaner.cache.viewmodel.AnalyticsViewModel
import com.phonecleaner.storagecleaner.cache.viewmodel.AppViewModel
import com.phonecleaner.storagecleaner.cache.viewmodel.AudioViewModel
import com.phonecleaner.storagecleaner.cache.viewmodel.ImageViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AnalyticsFragment : BaseFragment(), View.OnClickListener {
    private val myTag: String = this::class.java.simpleName
    private lateinit var binding: FragmentAnalyzingBinding
    private val viewModel: AnalyticsViewModel by viewModels()
    private val imageViewModel: ImageViewModel by viewModels()
    private val audioViewModel: AudioViewModel by viewModels()
    private val appViewModel: AppViewModel by viewModels()
    private val adapter: AnalyticsAdapter = AnalyticsAdapter()
    private var numProgress: Int = 0
    private lateinit var animator: Animator

    interface OnClickAnalytic {
        fun onClick(item: AnalyticModel)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAnalyzingBinding.inflate(inflater, container, false)
        activity?.let { act ->
            act.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            act.window.statusBarColor = ContextCompat.getColor(act, R.color.analytic_color)
        }
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        doWithContext { context ->
            animator = AnimatorInflater.loadAnimator(
                context, R.animator.animator_rotation
            ) as Animator
        }
        animator.setTarget(binding.progressBar)
        animator.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.let { act ->
            act.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            act.window.statusBarColor = ContextCompat.getColor(act, R.color.grayF6F6F6)
        }
    }

    override fun initUi() {
        binding.topAppBar.tvTitle.text = getString(R.string.analytics_storage)
        initRecycleView()
    }

    override fun initListener() {
        binding.topAppBar.btnBack.setOnClickListener(this)
        binding.topAppBar.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    override fun initData() {
        activity?.let {
            if (it.managerStoragePermissionGranted()) {
                viewModel.getScreenShotImages()
                viewModel.getDuplicateFile()
                viewModel.getLargerFile()
//                viewModel.getMiscFiles()
                imageViewModel.getFileImage()
                audioViewModel.getFileAudio()
                appViewModel.getAppInstalled(getIconBitmap = true)
            }
        }
    }

    private val onClickItem: OnClickAnalytic = object : OnClickAnalytic {
        override fun onClick(item: AnalyticModel) {
            activity?.let { act ->
                val viewPager: AnalyticDetailFragment =
                    AnalyticDetailFragment.onSetupView(analytic = item)
                (act as MainActivity).addFragment(viewPager)
            }
        }
    }

    override fun doWork() {
        initObserve()
    }

    private fun initObserve() {
        with(viewModel) {
            observe(listFileLiveData, ::handleGetDuplicateFile)
            observe(largerFileLiveData, ::handleGetLargeFile)
            observe(screenShotLiveData, ::handleGetScreenShotImages)
//            observe(miscFilesLiveData, ::handleGetMisc)
        }
        with(imageViewModel) {
            observe(listImageLiveData, ::handleGetImages)
        }
        with(audioViewModel) {
            observe(listFileAudio, ::handleGetAudio)
        }
        with(appViewModel) {
            observe(listAppLiveData, ::handleGetApps)
        }
    }

//    private fun handleGetMisc(state: State<List<FileApp>>?) {
//        when (state?.getStatus()) {
//            State.DataStatus.LOADING -> {
//                Toast.makeText(requireContext(), "Start", Toast.LENGTH_SHORT).show()
//
//            }
//
//            State.DataStatus.SUCCESS -> {
//                Toast.makeText(requireContext(), "Have Data", Toast.LENGTH_SHORT).show()
//                state.getData()?.let {
//                    val analyticModel = AnalyticModel(
//                        title = R.string.miscellaneous_files, listFileApps = it, size = it.size
//                    )
//                    adapter.addDataList(analyticModel)
//                }
//            }
//
//            State.DataStatus.ERROR -> {
//
//            }
//
//            else -> {
//
//            }
//
//        }
//    }

    private fun handleGetScreenShotImages(state: State<List<FileApp>>?) {
        when (state?.getStatus()) {
            State.DataStatus.LOADING -> {

            }

            State.DataStatus.SUCCESS -> {
                state.getData()?.let {
                    val analyticModel = AnalyticModel(
                        title = R.string.screen_shot, listFileApps = it, size = it.size
                    )
                    adapter.addDataList(analyticModel)
                    updateNumProgress(Analytics.ScreenShot.progress)
                }
            }

            State.DataStatus.ERROR -> {

            }

            else -> {

            }
        }
    }

    private fun handleGetImages(state: State<ArrayList<FileApp>>?) {
        when (state?.getStatus()) {
            State.DataStatus.LOADING -> {

            }

            State.DataStatus.SUCCESS -> {
                state.getData()?.let {
                    val analyticModel = AnalyticModel(
                        title = R.string.image, listFileApps = it, size = it.size
                    )
                    adapter.addDataList(analyticModel)
                    updateNumProgress(Analytics.Images.progress)
                }
            }

            State.DataStatus.ERROR -> {

            }

            else -> {

            }
        }
    }

    private fun handleGetAudio(state: State<ArrayList<FileApp>>?) {
        when (state?.getStatus()) {
            State.DataStatus.LOADING -> {

            }

            State.DataStatus.SUCCESS -> {
                state.getData()?.let {
                    val analyticModel = AnalyticModel(
                        title = R.string.music, listFileApps = it, size = it.size
                    )
                    adapter.addDataList(analyticModel)
                    updateNumProgress(Analytics.Music.progress)
                }
            }

            State.DataStatus.ERROR -> {

            }

            else -> {

            }
        }
    }

    private fun handleGetApps(state: State<ArrayList<AppInstalled>>?) {
        when (state?.getStatus()) {
            State.DataStatus.LOADING -> {

            }

            State.DataStatus.SUCCESS -> {
                state.getData()?.let { list ->
                    val analyticModel = AnalyticModel(
                        title = R.string.app, listFileApps = list.map {
                            FileApp(
                                name = it.appName,
                                size = it.size,
                                dateModified = it.modified,
                                path = it.packageName
                            ).apply {
                                iconBitmap = it.iconBitmap
                            }
                        }, size = list.size
                    )
                    adapter.addDataList(analyticModel)
                    updateNumProgress(Analytics.Apps.progress)
                }
            }

            State.DataStatus.ERROR -> {

            }

            else -> {

            }
        }
    }

    private fun handleGetLargeFile(state: State<List<FileApp>>?) {
        when (state?.getStatus()) {
            State.DataStatus.LOADING -> {

            }

            State.DataStatus.SUCCESS -> {
                state.getData()?.let { data ->
                    val list = (if (data.size > FILE_COUNT) data.subList(0, FILE_COUNT - 1)
                    else data).toMutableList()
                    val analyticModel = AnalyticModel(
                        title = R.string.larger_apps, listFileApps = list, size = list.size
                    )
                    adapter.addDataList(analyticModel)
                    updateNumProgress(Analytics.LargerApps.progress)
                }
            }

            State.DataStatus.ERROR -> {

            }

            else -> {

            }
        }
    }

    private fun handleGetDuplicateFile(state: State<List<FileApp>>?) {
        when (state?.getStatus()) {
            State.DataStatus.LOADING -> {

            }

            State.DataStatus.SUCCESS -> {
                state.getData()?.let {
                    val analyticModel = AnalyticModel(
                        title = R.string.duplicate_file, listFileApps = it, size = it.size
                    )
                    adapter.addDataList(analyticModel)
                    updateNumProgress(Analytics.DuplicateFiles.progress)
                }
            }

            State.DataStatus.ERROR -> {

            }

            else -> {

            }
        }
    }

    private fun initRecycleView() {
        binding.coolerRecycleView.adapter = adapter
        binding.coolerRecycleView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        adapter.setupListener(onClickItem)
    }

    override fun onClick(v: View?) {
        when (view) {
            binding.topAppBar.btnBack -> {
                Toast.makeText(requireContext(), "Back Press", Toast.LENGTH_SHORT).show()
                onBackPressed()
                showInBaseNavigationView()
            }
        }
    }

    override fun baseBackPressed() {
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onBackPressed()
                }
            })
    }

    private fun onBackPressed() {
        activity?.let { act ->
            act.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            act.window.statusBarColor = ContextCompat.getColor(act, R.color.grayF6F6F6)
            (act as MainActivity).removeFragment(this)
        }
        showInBaseNavigationView()
    }

    @SuppressLint("SetTextI18n")
    private fun updateNumProgress(count: Int) {
        val animatorText = ValueAnimator.ofInt(numProgress, (numProgress + count)).apply {
            duration = 1000
            addUpdateListener { animation ->
                val animatedValue = animation.animatedValue as Int
                binding.numProgress.text = "$animatedValue %"
            }
        }
        animatorText.start()
        numProgress += count
        animatorText.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
            }

            override fun onAnimationEnd(animation: Animator) {
                if (adapter.itemCount == ItemNum) {
                    animator.cancel()
                    binding.btnDone.visibility = View.VISIBLE
                    binding.textAnalyzing.visibility = View.INVISIBLE
                }
            }

            override fun onAnimationCancel(animation: Animator) {
            }

            override fun onAnimationRepeat(animation: Animator) {
            }
        })
    }

    companion object {
        private const val AMOUNT_LARGE_FILES_SHOW = 1
        private const val FILE_COUNT = 4
        private const val ItemNum: Int = 6
        fun onSetupView(): AnalyticsFragment {
            return AnalyticsFragment()
        }
    }
}

enum class Analytics(val stringResId: Int, val progress: Int) {
    ScreenShot(R.string.screen_shot, 10), LargerApps(
        R.string.larger_apps, 30
    ),
    Images(R.string.image, 10), Apps(R.string.app, 10), Music(
        R.string.music, 10
    ),
    DuplicateFiles(R.string.duplicate_file, 30)
}