package com.phonecleaner.storagecleaner.cache.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.base.BaseFragment
import com.phonecleaner.storagecleaner.cache.data.model.entity.DataSave
import com.phonecleaner.storagecleaner.cache.databinding.FragmentSettingBinding
import com.phonecleaner.storagecleaner.cache.extension.resetActivity
import com.phonecleaner.storagecleaner.cache.ui.dialog.AboutUsDialog
import com.phonecleaner.storagecleaner.cache.ui.dialog.NotifyDialog
import com.phonecleaner.storagecleaner.cache.ui.dialog.SetupLanguageDialog
import com.phonecleaner.storagecleaner.cache.ui.dialog.SetupPasscodeDialog
import com.phonecleaner.storagecleaner.cache.utils.AppActionHelper
import com.phonecleaner.storagecleaner.cache.utils.Constants
import com.phonecleaner.storagecleaner.cache.viewmodel.DataViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingFragment : BaseFragment() {
    private lateinit var binding: FragmentSettingBinding
    private val dataViewModel: DataViewModel by viewModels()
    private val passWord = ""
    private var language = "en"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingBinding.inflate(inflater, container, false)
        binding.containerLanguage.tvLanguage.setText(
            when (DataSave.language) {
//                "fr" -> R.string.french
//                "de" -> R.string.german
//                "in" -> R.string.indonesia
//                "ru" -> R.string.russian
//                "es" -> R.string.spanish
                "vi" -> R.string.vietnamese
                else -> R.string.english
            }
        )
        return binding.root
    }

    override fun initData() {
        super.initData()
        initObserve()
    }

    private fun initObserve() {


    }

    @SuppressLint("SuspiciousIndentation")
    override fun initListener() {
        super.initListener()
        binding.apply {
            containerLanguage.root.setOnClickListener {
                val languageDialog =
                    SetupLanguageDialog(DataSave.language ?: "en", setupLanguage = ::setupLanguage)
                languageDialog.isCancelable = false
                languageDialog.show(childFragmentManager, DIALOG_LANGUAGE)
            }

            containerWidgetPhotos.root.setOnClickListener {

            }
            containerSetPasscode.root.setOnClickListener {
                val passcodeDialog = SetupPasscodeDialog(setupPasscode = ::setupPasscode)
                passcodeDialog.isCancelable = false
                passcodeDialog.show(childFragmentManager, DIALOG_PASSCODE)
            }
            containerResetPasscode.root.setOnClickListener {
                val passcodeDialog =
                    NotifyDialog(
                        getString(R.string.reset_passcode),
                        acceptCallback = ::resetPasscode
                    )
                passcodeDialog.isCancelable = false
                passcodeDialog.show(childFragmentManager, Constants.DIALOG_NOTIFY)
            }
            containerAboutUs.root.setOnClickListener {
            val aboutUsDialog=AboutUsDialog()
                aboutUsDialog.show(childFragmentManager, DIALOG_ABOUTUS)
            }
            containerShareUs.root.setOnClickListener {
                context?.let { AppActionHelper.share(it) }
            }
            containerHelpUs.root.setOnClickListener {
                context?.let { AppActionHelper.openMail(it) }
            }
        }
    }

    private fun setupLanguage(lang: String) {
        if (lang.isNotEmpty()) {
            DataSave.language = lang
            activity?.resetActivity()
        }
    }

    private fun setupPasscode(passcode: String) {
        if (passcode.isNotEmpty()) {
            dataViewModel.setPasscode(passcode)
        }
    }
    private fun resetPasscode(isReset: Boolean) {
        if (isReset) {
            dataViewModel.setPasscode("")
        }
    }

    companion object {
        private const val DIALOG_PASSCODE = "DIALOG_PASSCODE"
        private const val DIALOG_LANGUAGE = "DIALOG_LANGUAGE"
        private const val DIALOG_ABOUTUS = "DIALOG_LANGUAGE"
    }
}