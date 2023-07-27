package com.phonecleaner.storagecleaner.cache.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.DialogFragment
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.data.model.entity.PagerType
import com.phonecleaner.storagecleaner.cache.databinding.DialogSortedBinding
import com.phonecleaner.storagecleaner.cache.utils.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SortedDialog(
    private val screenType: PagerType = PagerType.IMAGE,
    private var sortedByCallback: ((Boolean, String) -> Unit)? = null
) : DialogFragment() {
    private lateinit var binding: DialogSortedBinding
    private var optionSort: String = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = DialogSortedBinding.inflate(layoutInflater, container, false)
        initUi()
        initListener()
        isCancelable = true
        return binding.root
    }

    private fun initUi() {
        when (screenType) {
            PagerType.IMAGE -> {}
            PagerType.VIDEO -> {}
            PagerType.MUSIC -> {}
            PagerType.APK -> {
                binding.rbType.isGone = true
            }

            PagerType.ZIP -> {}
            else -> {}
        }
    }

    private fun initListener() {
        binding.apply {
            rbName.setOnClickListener {
                binding.rbName.isChecked = true
                binding.rgName.visibility = View.VISIBLE
                binding.rgType.isGone = true
            }
            rbNameFromAToZ.setOnClickListener {
                optionSort = Constants.SORTED_BY_NAME_FROM_A_TO_Z
            }
            binding.rbNameFromZToA.setOnClickListener {
                optionSort = Constants.SORTED_BY_NAME_FROM_Z_TO_A
            }

            binding.rbDate.setOnClickListener {
                optionSort = Constants.SORTED_BY_DATE
                binding.rgName.isGone = true
                binding.rgType.isGone = true
            }

            binding.rbSize.setOnClickListener {
                optionSort = Constants.SORTED_BY_SIZE
                binding.rgName.isGone = true
                binding.rgType.isGone = true
            }

            binding.rbType.setOnClickListener {
                binding.rbType.isChecked = true
                binding.rgType.visibility = View.VISIBLE
                binding.rgName.isGone = true
            }

            binding.rbTypeFromAToZ.setOnClickListener {
                optionSort = Constants.SORTED_BY_TYPE_FROM_A_TO_Z
            }

            binding.rbTypeFromZToA.setOnClickListener {
                optionSort = Constants.SORTED_BY_TYPE_FROM_Z_TO_A
            }

            binding.btnOk.setOnClickListener {
                sortedByCallback?.invoke(true, optionSort)
                dismiss()
            }

            btnCancel.setOnClickListener {
                sortedByCallback?.invoke(false, optionSort)
                this@SortedDialog.dismiss()
            }
        }
    }

    override fun getTheme(): Int {
        return R.style.AlertDialog
    }
}
