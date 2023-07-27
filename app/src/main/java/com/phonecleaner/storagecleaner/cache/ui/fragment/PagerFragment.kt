package com.phonecleaner.storagecleaner.cache.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.viewpager2.widget.ViewPager2
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.databinding.FragmentPagerBinding
import com.phonecleaner.storagecleaner.cache.base.BaseFragment
import com.phonecleaner.storagecleaner.cache.data.model.entity.LayoutType
import com.phonecleaner.storagecleaner.cache.data.model.entity.PagerType
import com.phonecleaner.storagecleaner.cache.data.model.response.MultiSelect
import com.phonecleaner.storagecleaner.cache.ui.activity.MainActivity
import com.phonecleaner.storagecleaner.cache.ui.adapters.ViewPagerAdapter
import com.phonecleaner.storagecleaner.cache.ui.dialog.SortedDialog
import com.phonecleaner.storagecleaner.cache.utils.Constants
import com.phonecleaner.storagecleaner.cache.utils.gone
import com.phonecleaner.storagecleaner.cache.utils.singleton.SingletonMenu
import com.phonecleaner.storagecleaner.cache.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class PagerFragment : BaseFragment() {
    private val myTag: String = this::class.java.simpleName
    private lateinit var binding: FragmentPagerBinding
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private var screenType = PagerType.IMAGE

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentPagerBinding.inflate(inflater, container, false)
        activity?.let { act ->
            act.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            act.window.statusBarColor = ContextCompat.getColor(act, R.color.grayF6F6F6)
        }
        return binding.root
    }

    override fun initData() {
//        screenType = PagerType.values()[arguments?.getInt(Constants.PAGER_TYPE) ?: 0]
    }

    override fun initUi() {
        setupViewPager()
        binding.toolbar.tvCreateFolder.isGone = true
    }

    override fun initListener() {
        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == binding.rbLeft.id) {
                binding.viewPager.currentItem = 0
            } else {
                binding.viewPager.currentItem = 1
            }
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position == 0) {
                    binding.rbLeft.isChecked = true
                } else {
                    binding.rbRight.isChecked = true
                }
                mainViewModel.multiSelect(MultiSelect.ClearAll)
            }
        })

        binding.toolbar.btnBack.setOnClickListener {
            onBackPressed()
            showInBaseNavigationView()
        }

        binding.toolbar.imgOpenMenu.setOnClickListener {
            binding.toolbar.layoutMenu.isGone = !binding.toolbar.layoutMenu.isGone
        }

        binding.toolbar.tvSortBy.setOnClickListener {
            val sortedDialog =
                SortedDialog(screenType = screenType, sortedByCallback = ::sortedByCallback)
            sortedDialog.isCancelable = false
            sortedDialog.show(parentFragmentManager, Constants.DIALOG_SORT_BY)
        }
        binding.toolbar.ctlView.setOnClickListener {
            binding.toolbar.ctlSwitch.visible()
            binding.toolbar.ctlView.gone()
        }
        binding.toolbar.fmDisplay.setOnClickListener {
            binding.toolbar.ctlSwitch.gone()
            binding.toolbar.ctlView.visible()
        }
        binding.toolbar.tvList.setOnClickListener {
            mainViewModel.changeLayoutLinear()
            binding.toolbar.layoutMenu.gone()
        }
        binding.toolbar.tvGrid.setOnClickListener {
            mainViewModel.changeLayoutGrid()
            binding.toolbar.layoutMenu.gone()
        }
    }

    override fun doWork() {
        mainViewModel.layoutTypeLiveData.observe(this) { type ->
            when (type) {
                LayoutType.LINEAR -> {
                    binding.toolbar.tvList.setBackgroundColor(
                        resources.getColor(
                            R.color.grayF6F6F6, null
                        )
                    )

                    binding.toolbar.tvGrid.setBackgroundColor(
                        resources.getColor(
                            R.color.white, null
                        )
                    )
                }

                LayoutType.GRID -> {
                    binding.toolbar.tvGrid.setBackgroundColor(
                        resources.getColor(
                            R.color.grayF6F6F6, null
                        )
                    )

                    binding.toolbar.tvList.setBackgroundColor(
                        resources.getColor(
                            R.color.white, null
                        )
                    )
                }

                else -> {}
            }
        }
    }

    private fun setupViewPager() {
        viewPagerAdapter = ViewPagerAdapter(childFragmentManager, lifecycle)
        binding.viewPager.adapter = viewPagerAdapter
        binding.viewPager.isUserInputEnabled = true

        when (screenType) {
            PagerType.IMAGE -> {
                viewPagerAdapter.setListFragment(
                    listOf(
                        ListImageFragment(), AlbumImageFragment()
                    )
                )
                binding.toolbar.tvTitle.text = getString(R.string.image)
                binding.rbLeft.text = getString(R.string.images)
                binding.rbRight.text = getString(R.string.album)
            }

            PagerType.MUSIC -> {
                viewPagerAdapter.setListFragment(
                    listOf(
                        AudioFragment(), FolderAudioFragment()
                    )
                )
                binding.toolbar.tvTitle.text = getString(R.string.music)
                binding.rbLeft.text = getString(R.string.song)
                binding.rbRight.text = getString(R.string.folder)
            }

            PagerType.APK -> {
                viewPagerAdapter.setListFragment(
                    listOf(
                        AppFragment(), ApkFragment()
                    )
                )
                binding.toolbar.tvTitle.text = getString(R.string.app)
                binding.rbLeft.text = getString(R.string.apps)
                binding.rbRight.text = getString(R.string.apks)
            }

            PagerType.VIDEO -> {
                viewPagerAdapter.setListFragment(
                    listOf(
                        VideoFragment(), FolderVideoFragment()
                    )
                )
                binding.toolbar.tvTitle.text = getString(R.string.video)
                binding.rbLeft.text = getString(R.string.video)
                binding.rbRight.text = getString(R.string.folder)
            }

            else -> {}
        }
    }

    private fun sortedByCallback(isAgreeSorted: Boolean, condition: String) {
        if (isAgreeSorted) {
            mainViewModel.sortedBy(condition)
            binding.toolbar.layoutMenu.isGone = true
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
        try {
            if (SingletonMenu.getInstance().type > -1) {
                mainViewModel.multiSelect(MultiSelect.ClearAll)
                SingletonMenu.getInstance().type = -1
            } else {
                (activity as MainActivity).let { act ->
                    act.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    act.supportFragmentManager.fragments.forEach { fragment ->
                        when (fragment) {
                            is PagerFragment -> {
                                act.removeFragment(fragment)
                                return@forEach
                            }

                            is DetailsFolderFragment -> {
                                act.removeFragment(fragment)
                                return@forEach
                            }

                            is ZipFragment -> {
                                act.removeFragment(fragment)
                                return@forEach
                            }

                            is DocumentFragment -> {
                                act.removeFragment(fragment)
                                return@forEach
                            }

                            is RecentTabFragment -> {
                                act.removeFragment(fragment)
                                return@forEach
                            }

                            is FavoriteTabFragment -> {
                                act.removeFragment(fragment)
                                return@forEach
                            }

                            else -> {}
                        }
                    }
                    showInBaseNavigationView()
                }
            }
        } catch (e: Exception) {
            Timber.tag(myTag).e("Back pressed failed: ${e.message}")
        }
    }

    companion object {
        fun onSetupView(screenType: PagerType): PagerFragment {
            val dialog = PagerFragment()
            dialog.screenType = screenType
            return dialog
        }
    }
}