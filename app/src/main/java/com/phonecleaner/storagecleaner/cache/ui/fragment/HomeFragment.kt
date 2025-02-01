package com.phonecleaner.storagecleaner.cache.ui.fragment

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import app.futured.donut.DonutSection
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.base.BaseFragment
import com.phonecleaner.storagecleaner.cache.data.model.entity.Analytics
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
import com.phonecleaner.storagecleaner.cache.data.model.entity.PagerType
import com.phonecleaner.storagecleaner.cache.data.model.liveData.State
import com.phonecleaner.storagecleaner.cache.data.model.response.FileState
import com.phonecleaner.storagecleaner.cache.data.model.response.MediaStoreState
import com.phonecleaner.storagecleaner.cache.data.model.response.MultiSelect
import com.phonecleaner.storagecleaner.cache.data.model.response.RecentlyScreen
import com.phonecleaner.storagecleaner.cache.data.model.response.Screen
import com.phonecleaner.storagecleaner.cache.databinding.FragmentHomeBinding
import com.phonecleaner.storagecleaner.cache.extension.convertToSize
import com.phonecleaner.storagecleaner.cache.extension.observe
import com.phonecleaner.storagecleaner.cache.ui.activity.MainActivity
import com.phonecleaner.storagecleaner.cache.ui.adapters.FilesAdapter
import com.phonecleaner.storagecleaner.cache.ui.adapters.RecentUploadRecycleView
import com.phonecleaner.storagecleaner.cache.ui.layoutmanager.WrapContentLinearLayoutManager
import com.phonecleaner.storagecleaner.cache.utils.singleton.SingletonMenu
import com.phonecleaner.storagecleaner.cache.utils.singleton.SingletonScreen
import com.phonecleaner.storagecleaner.cache.viewmodel.HomeViewModel
import com.facebook.shimmer.Shimmer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

@AndroidEntryPoint
class HomeFragment : BaseFragment(), View.OnClickListener {
    private val myTag: String = this::class.java.simpleName
    private lateinit var binding: FragmentHomeBinding
    private val homeViewModel: HomeViewModel by viewModels()
    private val recentAdapter = RecentUploadRecycleView()
    private lateinit var shimmerBuilder: Shimmer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Create Shimmer Builder
        shimmerBuilder =
            Shimmer.AlphaHighlightBuilder().setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
                .setBaseAlpha(0f).setClipToChildren(true).setDropoff(0.6f).setTilt(36f)
                .setShape(Shimmer.Shape.LINEAR).setDuration(2000L).setFixedHeight(100)
                .setRepeatMode(ValueAnimator.RESTART).build()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        activity?.let { act ->
            act.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            @Suppress("DEPRECATION")
            act.window.statusBarColor = ContextCompat.getColor(act, R.color.grayF6F6F6)
        }
        binding.shimmerLayout.apply {
            setShimmer(shimmerBuilder)
            startShimmer()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecentAdapter()
    }

    private fun setUpProgressBar(analytic: Analytics) {
        // Set up progress Circle
        val pictureSize = analytic.image.split("-").first().toLong()
        val videoSize = analytic.video.split("-").first().toLong()
        val documentSize = analytic.document
        val videoSection = DonutSection(
            name = "video",
            color = Color.parseColor("#9C14F0"),
            amount = videoSize * 100f / analytic.used
        )

        val pictureSection = DonutSection(
            name = "picture",
            color = Color.parseColor("#F143F4"),
            amount = pictureSize * 100f / analytic.used
        )
        val documentSection = DonutSection(
            name = "document",
            color = Color.parseColor("#4375F4"),
            amount = documentSize * 100f / analytic.used
        )
        binding.containerDetailStorage.donutView.cap = 100f
        binding.containerDetailStorage.donutView.submitData(
            listOf(
                videoSection, pictureSection, documentSection
            )
        )
        // Set up progress Horizontal
        binding.containerDetailStorage.containerPicture.progress.progress =
            videoSection.amount.toInt()
        binding.containerDetailStorage.containerVideo.progress.progress =
            pictureSection.amount.toInt()
        binding.containerDetailStorage.containerDocument.progress.progress =
            documentSection.amount.toInt()
        binding.containerDetailStorage.tvUsed.text = analytic.used.convertToSize()
        binding.containerStorage.useStorage.text = analytic.used.convertToSize()
        binding.containerStorage.totalStorage.text = analytic.total.convertToSize()

        binding.containerCloud.itemCloudDriver.donutDrive.cap = 100f;
        binding.containerCloud.itemCloudDriver.donutDrive.submitData(listOf(videoSection))
        binding.containerCloud.itemDropBox.donutDropbox.cap = 100f;
        binding.containerCloud.itemDropBox.donutDropbox.submitData(listOf(pictureSection))
        binding.containerCloud.itemWifiTransfer.donutWifi.cap = 100f;
        binding.containerCloud.itemWifiTransfer.donutWifi.submitData(listOf(documentSection))
        // Set progress total
        ValueAnimator.ofInt(0, (analytic.used * 100 / analytic.total).toInt()).apply {
            duration = 1000
            addUpdateListener { animator ->
                val animatorValue = animator.animatedValue as Int
                binding.containerStorage.barStorage.progress = animatorValue
            }
            start()
        }
        binding.containerStorage.useStorage.text = analytic.used.convertToSize()
        binding.containerStorage.totalStorage.text = analytic.total.convertToSize()
    }

    private val onItemClickRecent: OnClickEvent = object : OnClickEvent {
        override fun onClickItem(file: FileApp, position: Int) {
            if (!File(file.path).isDirectory) {
                (activity as MainActivity).navigateToOpenFileScreen(file = file)
            } else {
                activity?.let { act ->
                    hideInBaseNavigationView()
                    val pagerView = DetailsFolderFragment.onSetupView(file.path)
                    (act as MainActivity).addFragment(pagerView)
                }
            }
        }

        override fun onSelectItem(file: FileApp, position: Int) {
        }

        override fun onItemTypeAdapter(adapter: FilesAdapter?) {
        }
    }

    private fun setUpRecentAdapter() {
        doWithContext { context ->
            binding.recycleRecent.adapter = recentAdapter
            recentAdapter.setOnClickEvent(onItemClickRecent)
            binding.recycleRecent.layoutManager = WrapContentLinearLayoutManager(context)
        }
    }


    override fun initListener() {
        super.initListener()
        binding.apply {
            containerFileManager.containerInternal.setOnClickListener(this@HomeFragment)
            containerFileManager.containerImage.setOnClickListener(this@HomeFragment)
            containerFileManager.containerAudio.setOnClickListener(this@HomeFragment)
            containerFileManager.containerVideo.setOnClickListener(this@HomeFragment)
            containerFileManager.containerApps.setOnClickListener(this@HomeFragment)
            containerFileManager.containerFolder.setOnClickListener(this@HomeFragment)
            containerFileManager.containerZipFiles.setOnClickListener(this@HomeFragment)
            containerFileManager.containerDownload.setOnClickListener(this@HomeFragment)
            containerCloud.itemDropBox.dropbox.setOnClickListener(this@HomeFragment)
            containerCloud.itemCloudDriver.itemDrive.setOnClickListener(this@HomeFragment)
            containerCloud.itemWifiTransfer.itemWifi.setOnClickListener(this@HomeFragment)
            btnAnalyze.setOnClickListener(this@HomeFragment)
        }
    }

    override fun initData() {
        homeViewModel.getAnalytics()
        mainViewModel.getRecentFile(limit = 10)
    }

    override fun doWork() {
        initObserve()
        initFileState()
        initMediaStoreState()
    }

    private fun initFileState() {
        (activity as MainActivity).let { ct ->
            lifecycleScope.launch {
                lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    mainViewModel.fileStateFlow.collect { fileState ->
                        when (fileState) {
                            is FileState.START -> {
                                Timber.tag(myTag).d("FileState.START")
                                if (ct.dialog?.isShowing == true) {
                                    ct.dialog?.dismiss()
                                }
                            }

                            is FileState.PREPARE -> {
                                Timber.tag(myTag).d("FileState.PREPARE")
                            }

                            is FileState.LOADING -> {
                            }

                            is FileState.SUCCESS -> {
                                Timber.tag(myTag).d("FileState.SUCCESS")
                                mainViewModel.clearFileState()
                                mainViewModel.clearData()
                                mainViewModel.getFavoriteFile(limit = true)
                                ct.dialog?.dismiss()
                            }

                            is FileState.ERROR -> {
                                Timber.tag(myTag).d("FileState.ERROR")
                                mainViewModel.clearFileState()
                                mainViewModel.clearData()
                                ct.dialog?.dismiss()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun initMediaStoreState() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.mediaStoreStateFlow.collect { mediaStoreState ->
                    when (mediaStoreState) {
                        is MediaStoreState.NOTHING -> {}
                        is MediaStoreState.INSERT -> {
                            mainViewModel.insertMediaStore(RecentlyScreen.Main)
                            mainViewModel.clearMediaStoreState()
                        }

                        is MediaStoreState.UPDATE -> {
                            mainViewModel.updateMediaStore(RecentlyScreen.Main)
                            mainViewModel.clearMediaStoreState()
                        }

                        is MediaStoreState.DELETE -> {
                            mainViewModel.removeMediaStore(RecentlyScreen.Main)
                            mainViewModel.clearMediaStoreState()
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    private fun handleEventBackPressed(multiSelect: MultiSelect?) {
        multiSelect?.let { select ->
            when (select) {
                is MultiSelect.SelectAll -> {
                    Timber.tag(myTag).d("MultiSelect.SelectAll")
                }

                is MultiSelect.ClearAll -> {
//                    recentAdapter.multiSelect(false)
                    mainViewModel.clearData()
                }

                is MultiSelect.Nothing -> {
                    Timber.tag(myTag).d("MultiSelect.Nothing")
                }

                else -> {}
            }
        }
    }

    private fun initObserve() {
        with(mainViewModel) {
            observe(multiLiveData, ::handleEventBackPressed)
            observe(recentFileLiveData) {
                when (it?.getStatus()) {
                    State.DataStatus.LOADING -> {
                    }

                    State.DataStatus.SUCCESS -> {
                        it.getData()?.let { listRecent ->
                            recentAdapter.setListData(listRecent)
                        }
                    }

                    State.DataStatus.ERROR -> {
                    }

                    else -> {}
                }
            }
        }
        with(homeViewModel) {
            observe(this.analyticsLiveData) {
                when (it?.getStatus()) {
                    State.DataStatus.LOADING -> {
                    }

                    State.DataStatus.SUCCESS -> {
                        it.getData()?.let { data ->
                            setUpProgressBar(analytic = data)
                        }
                    }

                    State.DataStatus.ERROR -> {
                    }

                    else -> {}
                }
            }
        }
    }

    override fun onClick(view: View?) {
        when (view) {
            binding.containerFileManager.containerInternal -> {
                activity?.let { act ->
                    hideInBaseNavigationView()
                    val pagerView =
                        DetailsFolderFragment.onSetupView(Environment.getExternalStorageDirectory().path)
                    (act as MainActivity).addFragment(pagerView)
                }
                SingletonScreen.getInstance().type = Screen.Internal
            }


            binding.containerFileManager.containerImage -> {
                activity?.let { act ->
                    hideInBaseNavigationView()
                    val pagerView = PagerFragment.onSetupView(PagerType.IMAGE)
                    (act as MainActivity).addFragment(pagerView)
                }
                SingletonScreen.getInstance().type = Screen.Image
            }

            binding.containerFileManager.containerAudio -> {
                activity?.let { act ->
                    hideInBaseNavigationView()
                    val pageView: PagerFragment = PagerFragment.onSetupView(PagerType.MUSIC)
                    (act as MainActivity).addFragment(pageView)
                }
                SingletonScreen.getInstance().type = Screen.Audio
            }

            binding.containerFileManager.containerVideo -> {
                activity?.let { act ->
                    hideInBaseNavigationView()
                    val pagerView: PagerFragment = PagerFragment.onSetupView(PagerType.VIDEO)
                    (act as MainActivity).addFragment(pagerView)
                }
            }

            binding.containerFileManager.containerApps -> {
                activity.let { act ->
                    hideInBaseNavigationView()
                    val pageView: PagerFragment = PagerFragment.onSetupView(PagerType.APK)
                    (act as MainActivity).addFragment(pageView)
                }
            }

            binding.containerFileManager.containerFolder -> {
                activity?.let { act ->
                    hideInBaseNavigationView()
                    val documentView: DocumentFragment = DocumentFragment.onSetupView()
                    (act as MainActivity).addFragment(documentView)
                }
                SingletonScreen.getInstance().type = Screen.Document
            }

            binding.containerFileManager.containerDownload -> {
                activity?.let { act ->
                    hideInBaseNavigationView()
                    val detailView: DetailsFolderFragment = DetailsFolderFragment.onSetupView(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path

                    )
                    (act as MainActivity).addFragment(detailView)
                }
                SingletonScreen.getInstance().type = Screen.Download
            }

            binding.containerFileManager.containerZipFiles -> {
                activity?.let { act ->
                    hideInBaseNavigationView()
                    val zipView: ZipFragment = ZipFragment.onSetupView()
                    (act as MainActivity).addFragment(zipView)
                }
                SingletonScreen.getInstance().type = Screen.Zip
            }

            binding.btnAnalyze -> {
                activity?.let { act ->
                    hideInBaseNavigationView()
                    val analyze: AnalyticsFragment = AnalyticsFragment.onSetupView()
                    (act as MainActivity).addFragment(analyze)
                }
                SingletonScreen.getInstance().type = Screen.Analytics
            }
            //Cloud
            binding.containerCloud.itemDropBox.dropbox -> {
                activity?.let { act ->
                    hideInBaseNavigationView()
                    val dropBox: DropBoxLoginFragment = DropBoxLoginFragment.onSetupView()
                    (act as MainActivity).addFragment(dropBox)
                }
            }
        }
    }

    override fun baseBackPressed() {
        if (SingletonMenu.getInstance().type > -1) {
            mainViewModel.multiSelect(MultiSelect.ClearAll)
            SingletonMenu.getInstance().type = -1
        }
    }
}