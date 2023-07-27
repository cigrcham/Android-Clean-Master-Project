package com.phonecleaner.storagecleaner.cache.base

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.data.model.entity.Account
import com.phonecleaner.storagecleaner.cache.data.model.entity.AppInstalled
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileDelete
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileHide
import com.phonecleaner.storagecleaner.cache.data.model.entity.Folder
import com.phonecleaner.storagecleaner.cache.data.model.entity.LayoutType
import com.phonecleaner.storagecleaner.cache.data.model.entity.MessageEvent
import com.phonecleaner.storagecleaner.cache.extension.toast
import com.phonecleaner.storagecleaner.cache.ui.activity.MainActivity
import com.phonecleaner.storagecleaner.cache.ui.adapters.FilesAdapter
import com.phonecleaner.storagecleaner.cache.ui.fragment.DetailsFolderFragment
import com.phonecleaner.storagecleaner.cache.ui.fragment.DocumentFragment
import com.phonecleaner.storagecleaner.cache.ui.fragment.FavoriteTabFragment
import com.phonecleaner.storagecleaner.cache.ui.fragment.PagerFragment
import com.phonecleaner.storagecleaner.cache.ui.fragment.RecentTabFragment
import com.phonecleaner.storagecleaner.cache.ui.fragment.ZipFragment
import com.phonecleaner.storagecleaner.cache.utils.Constants
import com.phonecleaner.storagecleaner.cache.utils.singleton.SingletonMenu
import com.phonecleaner.storagecleaner.cache.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
open class BaseFragment : Fragment() {
    private val myTag: String = this::class.java.simpleName
    val mainViewModel: MainViewModel by activityViewModels()
    private var backPressedTime = 0L
    open val onClickEvent: OnClickEvent = object : OnClickEvent {
        override fun onClickItem(file: FileApp, position: Int) {
            activity?.let {
                (activity as MainActivity).navigateToOpenFileScreen(file)
            }
        }

        override fun onSelectItem(file: FileApp, position: Int) {
            mainViewModel.onSelectFile(file, position)
            SingletonMenu.getInstance().type = Constants.MENU_NORMAL
        }

        override fun onItemTypeAdapter(adapter: FilesAdapter?) {
        }
    }

    val onClickFolderEvent: OnClickFolderEvent = object : OnClickFolderEvent {
        override fun onClickItem(folder: Folder, position: Int) {
            activity?.let {
                (activity as MainActivity).navigateToOpenFolderScreen(folder)
            }
        }

        override fun onSelectItem(folder: Folder, position: Int) {
            mainViewModel.onSelectFolder(folder)
            SingletonMenu.getInstance().type = Constants.MENU_NORMAL
        }
    }

    val onClickFileDeleteEvent: OnClickFileDeleteEvent = object : OnClickFileDeleteEvent {
        override fun onClickItem(fileDelete: FileDelete, position: Int) {
            mainViewModel.onSelectFileDelete(fileDelete)
        }
    }

    val onClickFileHideEvent: OnClickFileHideEvent = object : OnClickFileHideEvent {
        override fun onClickItem(fileDelete: FileHide, position: Int) {
            mainViewModel.onSelectFileHide(fileDelete)
        }
    }

    val onClickAppEvent: OnClickAppEvent = object : OnClickAppEvent {
        override fun onClickItem(item: AppInstalled, position: Int) {
            activity?.let {
                (activity as MainActivity).navigateToOpenAppScreen(item)
            }
        }

        override fun onSelectItem(item: AppInstalled, position: Int) {
            mainViewModel.onSelectApp(item)
            SingletonMenu.getInstance().type = Constants.MENU_APP
        }
    }

    val onClickAccountEvent = object : OnClickAccountEvent {
        override fun onClickItem(item: Account, position: Int) {
            activity?.let {
                (activity as MainActivity).navigateToOpenAccountScreen(item)
            }
        }

        override fun onSelectItem(item: Account, position: Int) {
            mainViewModel.onSelectAccount(item)
            SingletonMenu.getInstance().type = Constants.MENU_ACCOUNT
        }
    }

    open fun initData() {}
    open fun initUi() {}
    open fun initListener() {}
    open fun doWork() {}
    open fun onLayoutChange(type: LayoutType) {}
    open fun baseBackPressed() {}

    override fun onStart() {
        super.onStart()
//        if (!EventBus.getDefault().isRegistered(this)) {
//            EventBus.getDefault().register(this)
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
//        if (EventBus.getDefault().isRegistered(this)) {
//            EventBus.getDefault().unregister(this)
//        }
    }

    //    @Subscribe(threadMode = ThreadMode.MAIN)
    open fun onMessageEvent(event: MessageEvent?) {
        // Do something
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
        initUi()
        initListener()
        doWork()
        registerOnLayoutTypeChange()
        baseBackPressed()
    }

    private fun registerOnLayoutTypeChange() {
        mainViewModel.layoutTypeLiveData.observe(viewLifecycleOwner) {
            onLayoutChange(it)
        }
    }

    fun doWithContext(callback: (Context) -> Unit) {
        context?.let {
            callback.invoke(it)
        }
    }

    open fun hideKeyboard(view: View) {
        val imm = context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun customPopBackStack(act: MainActivity) {
        try {
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
                    else -> {
                        if (backPressedTime + 2000L > System.currentTimeMillis()) {
                            act.finish()
                        } else {
                            act.toast(getString(R.string.notify_exit))
                        }
                        backPressedTime = System.currentTimeMillis()
                    }
                }
            }
        } catch (e: Exception) {
            Timber.tag(myTag).e("back pressed failed: ${e.message}")
        }
    }

    interface OnClickEvent {
        fun onClickItem(file: FileApp, position: Int)
        fun onSelectItem(file: FileApp, position: Int)
        fun onItemTypeAdapter(adapter: FilesAdapter?)
    }

    interface OnClickFolderEvent {
        fun onClickItem(folder: Folder, position: Int)
        fun onSelectItem(folder: Folder, position: Int)
    }

    interface OnClickFileDeleteEvent {
        fun onClickItem(fileDelete: FileDelete, position: Int)
    }

    interface OnClickFileHideEvent {
        fun onClickItem(fileHide: FileHide, position: Int)
    }

    interface OnClickAppEvent {
        fun onClickItem(item: AppInstalled, position: Int)
        fun onSelectItem(item: AppInstalled, position: Int)
    }

    interface OnClickAccountEvent {
        fun onClickItem(item: Account, position: Int)
        fun onSelectItem(item: Account, position: Int)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mainViewModel.clearData()
    }

    open fun hideInBaseNavigationView() {
        (activity as? MainActivity?)?.let {
            it.hideNavigationView()
        }
    }

    fun showInBaseNavigationView() {
        (activity as? MainActivity?)?.let {
            it.showNavigationView()
        }
    }
}