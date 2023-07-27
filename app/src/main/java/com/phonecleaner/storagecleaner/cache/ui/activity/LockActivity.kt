package com.phonecleaner.storagecleaner.cache.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.base.BaseActivity
import com.phonecleaner.storagecleaner.cache.databinding.ActivityLockBinding
import com.phonecleaner.storagecleaner.cache.extension.toast
import com.phonecleaner.storagecleaner.cache.utils.Constants
import com.phonecleaner.storagecleaner.cache.viewmodel.DataViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LockActivity : BaseActivity() {
    private val passcodeList: MutableList<AppCompatImageView> = mutableListOf()
    private lateinit var binding: ActivityLockBinding
    private val viewModel: DataViewModel by viewModels()
    private var passcode: String? = null
    private var passcodeUser = ""
    private var deep_link = ""
    private var code: String = ""
    private var setupPasscode: (String) -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLockBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this@LockActivity, R.color.grayF6F6F6)
        passcodeList.clear()
        passcodeList.addAll(
            listOf(
                binding.inputPasscode.item1,
                binding.inputPasscode.item2,
                binding.inputPasscode.item3,
                binding.inputPasscode.item4,
                binding.inputPasscode.item5,
                binding.inputPasscode.item6,
            )
        )
        if (intent != null) {
            code = intent.getStringExtra(Constants.CODE).toString()
            deep_link = intent.getStringExtra(Constants.ACTION_TYPE).toString()
        }
        showPasscode()
        initListener()

    }

    private fun initListener() {
        binding.keyboard.lockNumber0.setOnClickListener {
            passcodeUser += "0"
            showPasscode()
            checkPasscode()
        }
        binding.keyboard.lockNumber1.setOnClickListener {
            passcodeUser += "1"
            showPasscode()
            checkPasscode()
        }
        binding.keyboard.lockNumber2.setOnClickListener {
            passcodeUser += "2"
            showPasscode()
            checkPasscode()
        }
        binding.keyboard.lockNumber3.setOnClickListener {
            passcodeUser += "3"
            showPasscode()
            checkPasscode()
        }
        binding.keyboard.lockNumber4.setOnClickListener {
            passcodeUser += "4"
            showPasscode()
            checkPasscode()
        }
        binding.keyboard.lockNumber5.setOnClickListener {
            passcodeUser += "5"
            showPasscode()
            checkPasscode()
        }
        binding.keyboard.lockNumber6.setOnClickListener {
            passcodeUser += "6"
            showPasscode()
            checkPasscode()
        }
        binding.keyboard.lockNumber7.setOnClickListener {
            passcodeUser += "7"
            showPasscode()
            checkPasscode()
        }
        binding.keyboard.lockNumber8.setOnClickListener {
            passcodeUser += "8"
            showPasscode()
            checkPasscode()
        }
        binding.keyboard.lockNumber9.setOnClickListener {
            passcodeUser += "9"
            showPasscode()
            checkPasscode()
        }
        binding.keyboard.iconDelete.setOnClickListener {
            if (passcodeUser.isNotEmpty() && passcodeUser.isNotBlank()) {
                passcodeUser = passcodeUser.substring(0, passcodeUser.lastIndex)
                showPasscode()
            }
        }
        binding.btnBack.setOnClickListener {
            this.finish()
        }
    }


    private fun hideFragment(hide: Boolean = true) {
        if (hide) binding.keyboard.root.visibility = View.INVISIBLE
        else binding.keyboard.root.visibility = View.VISIBLE
    }

    private fun checkPasscode() {
        if (passcodeUser.length == 6) {
            if (passcodeUser == code) {
                intent = Intent(this, MainActivity::class.java)
                intent.putExtra(Constants.ACTION_TYPE, deep_link)
                startActivity(intent)
                finish()
            } else {
                this.toast(getString(R.string.notify_passcode))
                passcodeUser = ""
                showPasscode()
            }
        }
    }

    private fun showPasscode() {
        for (i in passcodeList.indices) {
            if (i + 1 <= passcodeUser.length) {
                passcodeList[i].setImageResource(R.drawable.bg_view_gradient_oval)
            } else {
                passcodeList[i].setImageResource(R.drawable.bg_view_white_oval)
            }
        }
    }
}