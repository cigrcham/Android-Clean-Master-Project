package com.phonecleaner.storagecleaner.cache.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.databinding.DialogSetupLanguageBinding
import com.phonecleaner.storagecleaner.cache.utils.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SetupLanguageDialog(
    private val lang: String, private val setupLanguage: (String) -> Unit = {}
) : DialogFragment() {
    private lateinit var binding: DialogSetupLanguageBinding
    private var chooseLanguage: String = Constants.ENGLISH

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = DialogSetupLanguageBinding.inflate(layoutInflater, container, false)
        initUi()
        initListener()
        isCancelable = true
        return binding.root
    }

    private fun initUi() {
        when {
            lang == "en" || lang.lowercase() == "english" -> binding.rbEnglish.isChecked = true
            lang == "vi" || lang.lowercase() == "vietnamese" -> binding.rbVietnamese.isChecked =
                true

//            lang == "fr" || lang.lowercase() == "french" -> binding.rbFrench.isChecked = true
//            lang == "de" || lang.lowercase() == "german" -> binding.rbGerman.isChecked = true
//            lang == "in" || lang.lowercase() == "indonesia" -> binding.rbIndonesia.isChecked = true
//            lang == "ru" || lang.lowercase() == "russian" -> binding.rbRussian.isChecked = true
//            lang == "es" || lang.lowercase() == "spanish" -> binding.rbSpanish.isChecked = true
            else -> binding.rbEnglish.isChecked = true
        }
    }

    private fun initListener() {
        binding.rbEnglish.setOnClickListener {
            binding.rbEnglish.isChecked = true
            chooseLanguage = Constants.ENGLISH
        }

        binding.rbVietnamese.setOnClickListener {
            binding.rbVietnamese.isChecked = true
            chooseLanguage = Constants.VIETNAMESE
        }

//        binding.rbFrench.setOnClickListener {
//            binding.rbFrench.isChecked = true
//            chooseLanguage = Constants.FRENCH
//        }
//
//        binding.rbGerman.setOnClickListener {
//            binding.rbGerman.isChecked = true
//            chooseLanguage = Constants.GERMAN
//        }
//
//        binding.rbIndonesia.setOnClickListener {
//            binding.rbIndonesia.isChecked = true
//            chooseLanguage = Constants.INDONESIAN
//        }
//
//        binding.rbRussian.setOnClickListener {
//            binding.rbRussian.isChecked = true
//            chooseLanguage = Constants.RUSSIAN
//        }
//
//        binding.rbSpanish.setOnClickListener {
//            binding.rbSpanish.isChecked = true
//            chooseLanguage = Constants.SPANISH
//            setupLanguage(Constants.SPANISH)
//            this.dismiss()
//        }
        binding.btnOK.setOnClickListener {
            if (lang != chooseLanguage) setupLanguage.invoke(chooseLanguage)
            this.dismiss()
        }
        binding.btnCancel.setOnClickListener {
            this.dismiss()
        }
    }

    override fun getTheme(): Int {
        return R.style.AlertDialog
    }
}