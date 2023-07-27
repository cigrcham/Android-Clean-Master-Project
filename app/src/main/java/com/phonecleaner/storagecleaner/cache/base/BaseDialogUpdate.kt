package com.phonecleaner.storagecleaner.cache.base

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.phonecleaner.storagecleaner.cache.R

open class BaseDialogUpdate : DialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun getTheme() = R.style.RoundedCornersDialog
}