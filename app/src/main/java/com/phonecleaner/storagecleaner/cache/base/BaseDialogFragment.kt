package com.phonecleaner.storagecleaner.cache.base

import com.phonecleaner.storagecleaner.cache.data.model.entity.Folder
import android.accounts.Account
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.phonecleaner.storagecleaner.cache.data.model.entity.AppInstalled
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileDelete
import com.phonecleaner.storagecleaner.cache.data.model.entity.LayoutType
import com.phonecleaner.storagecleaner.cache.ui.activity.MainActivity
import com.phonecleaner.storagecleaner.cache.ui.adapters.FilesAdapter
import com.phonecleaner.storagecleaner.cache.utils.Constants
import com.phonecleaner.storagecleaner.cache.utils.singleton.SingletonMenu
import com.phonecleaner.storagecleaner.cache.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class BaseDialogFragment : DialogFragment() {
    private val myTag: String = this::class.java.simpleName
    val mainViewModel: MainViewModel by activityViewModels()
    private var backPressedTime = 0L
    val onClickEvent: BaseFragment.OnClickEvent = object : BaseFragment.OnClickEvent {
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
    val onClickFolderEvent: BaseFragment.OnClickFolderEvent =
        object : BaseFragment.OnClickFolderEvent {
            override fun onClickItem(
                folder: Folder, position: Int
            ) {
                activity?.let {
                    (activity as MainActivity).navigateToOpenFolderScreen(folder)
                }
            }

            override fun onSelectItem(
                folder: Folder, position: Int
            ) {
                mainViewModel.onSelectFolder(folder)
                SingletonMenu.getInstance().type = Constants.MENU_NORMAL
            }
        }

    val onClickFileDeleteEvent: BaseFragment.OnClickFileDeleteEvent =
        object : BaseFragment.OnClickFileDeleteEvent {
            override fun onClickItem(fileDelete: FileDelete, position: Int) {
                mainViewModel.onSelectFileDelete(fileDelete)
            }
        }

    val onClickAppEvent = object : BaseFragment.OnClickAppEvent {
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
//    val onClickAccountEvent = object : BaseFragment.OnClickAccountEvent {
//        override fun onClickItem(item: Account, position: Int) {
//            activity?.let {
//                (activity as MainActivity).navigateToOpenAccountScreen(item)
//            }
//        }
//
//        override fun onSelectItem(item: Account, position: Int) {
//            mainViewModel.onSelectAccount(item)
//            SingletonMenu.getInstance().type = Constants.MENU_ACCOUNT
//        }
//    }

    open fun initData() {}
    open fun initUi() {}
    open fun initListener() {}
    open fun doWork() {}
    open fun onLayoutChange(type: LayoutType) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
        initUi()
        doWork()
        initListener()
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

    interface OnClickEvent {
        fun onClickItem(file: FileApp, position: Int)
        fun onSelectItem(file: FileApp, position: Int)
    }

    interface OnClickFolderEvent {
        fun onClickItem(folder: Folder, position: Int)
        fun onSelectItem(folder: Folder, position: Int)
    }

    interface OnClickFileDeleteEvent {
        fun onClickItem(fileDelete: FileDelete, position: Int)
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
}