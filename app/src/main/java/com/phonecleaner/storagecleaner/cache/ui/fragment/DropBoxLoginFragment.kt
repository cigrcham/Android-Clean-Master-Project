package com.phonecleaner.storagecleaner.cache.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.databinding.FragmentDropBoxLoginBinding
import com.phonecleaner.storagecleaner.cache.base.BaseFragment
import com.phonecleaner.storagecleaner.cache.data.model.entity.Account
import com.phonecleaner.storagecleaner.cache.data.model.response.AccountState
import com.phonecleaner.storagecleaner.cache.data.model.response.MultiSelect
import com.phonecleaner.storagecleaner.cache.extension.observe
import com.phonecleaner.storagecleaner.cache.ui.activity.DropBoxActivity
import com.phonecleaner.storagecleaner.cache.ui.activity.MainActivity
import com.phonecleaner.storagecleaner.cache.ui.adapters.AccountsAdapter
import com.phonecleaner.storagecleaner.cache.utils.Constants
import com.phonecleaner.storagecleaner.cache.utils.singleton.SingletonDropboxMail
import com.phonecleaner.storagecleaner.cache.utils.singleton.SingletonMenu
import com.phonecleaner.storagecleaner.cache.viewmodel.DataViewModel
import kotlinx.coroutines.launch
import timber.log.Timber


class DropBoxLoginFragment : BaseFragment(), View.OnClickListener {

    private val myTag: String = this::class.java.simpleName

    private lateinit var binding: FragmentDropBoxLoginBinding
    private val dataViewModel by viewModels<DataViewModel>()
    private val accountList = arrayListOf<Account>()
    private val accountsAdapter = AccountsAdapter(onItemSelectListener = ::onItemSelectListener)
    private var positionSelect = -1

    private fun onItemSelectListener(accountAmount: Int) {
        binding.btnAddAccount.isGone = accountAmount > 0
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentDropBoxLoginBinding.inflate(inflater, container, false)
        activity?.let { act ->
            act.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            act.window.statusBarColor = ContextCompat.getColor(act, R.color.grayF6F6F6)
        }
        return binding.root
    }

    override fun initData() {
        Timber.tag(myTag).d("initData")
        binding.toolbar.tvTitle.text = Constants.DROP_BOX
        accountsAdapter.setOnClickEvent(onClickAccountEvent)
        dataViewModel.getAllAccounts(Constants.DROPBOX_EMAIL)
        observeAccountState()
        observeInsertAccount()
    }

    private fun observeAccountState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.accountStateFlow.collect { accountState ->
                    when (accountState) {
                        is AccountState.LOADING -> {
                            Timber.tag(myTag).d("AccountState.LOADING")
                        }

                        is AccountState.SUCCESS -> {
                            Timber.tag(myTag).d("AccountState.SUCCESS")
                            accountsAdapter.deleteItem(index = positionSelect)
                            accountList.removeAt(positionSelect)
                            Log.d("TAG", "observeAccountState: " + "SUCCes")
                            binding.btnAddAccount.isVisible = true
                        }

                        is AccountState.ERROR -> {
                            Timber.tag(myTag).d("AccountState.ERROR: ${accountState.message}")
                        }
                    }
                }
            }
        }
    }

    private fun observeInsertAccount() {
        viewLifecycleOwner.lifecycleScope.launch {
            dataViewModel.accountLiveData.observe(viewLifecycleOwner) {
                it?.let { account ->
                    if (account.email.isNotEmpty()) {
                        var isInsert = true
                        Timber.tag(myTag).d("observeInsertAccount: $accountList")
                        accountList.forEach { item ->
                            if (account.email == item.email) {
                                isInsert = false
                                return@forEach
                            }
                        }
                        Timber.tag(myTag).d("observeInsertAccount: $isInsert")
                        if (isInsert) {
                            accountList.add(account)
                            accountsAdapter.addItem(item = account)
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        dataViewModel.getAccount(SingletonDropboxMail.getInstance().mail)
    }

    override fun onPause() {
        super.onPause()
        Timber.tag(myTag).d("onPause")
        mainViewModel.resetAccountState()
    }

    override fun initUi() {
        binding.toolbar.imgOpenMenu.visibility = View.GONE
    }

    override fun initListener() {
        binding.toolbar.btnBack.setOnClickListener(this)
        binding.btnAddAccount.setOnClickListener(this)
    }

    override fun doWork() {
        initObserve()
        accountsAdapter.onItemSelected = { positionSelect ->
            this.positionSelect = positionSelect
        }
    }

    private fun initObserve() {
        with(dataViewModel) {
            observe(getAllAccount) {
                it?.let { list ->
                    binding.progressBar.visibility = View.GONE
                    accountList.clear()
                    accountList.addAll(list)
                    accountsAdapter.setData(accountList)
                    binding.rcvDropbox.adapter = accountsAdapter
                }
            }
        }
        with(mainViewModel) {
            observe(multiLiveData) { select ->
                when (select) {
                    is MultiSelect.SelectAll -> {
                        Timber.tag(myTag).d(("MultiSelect.SelectAll"))
                    }

                    is MultiSelect.ClearAll -> {
                        accountsAdapter.multiSelect(false)
                        mainViewModel.clearData()
                    }

                    is MultiSelect.Nothing -> {
                        Timber.tag(myTag).d(("MultiSelect.Nothing"))
                    }

                    else -> {}
                }
            }
        }

    }

    override fun onClick(view: View?) {
        when (view) {
            binding.toolbar.btnBack -> {
                onBackPressed()
            }

            binding.btnAddAccount -> {
                activity?.let { act ->
                    val intent = Intent(act, DropBoxActivity::class.java)
                    act.startActivity(intent)
                }
            }
        }
    }

    override fun baseBackPressed() {
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner,
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
                    act.window.statusBarColor =
                        ContextCompat.getColor(act, R.color.color_start_gradient)
                    act.removeFragment(this)
                }
            }
            showInBaseNavigationView()
            activity?.let { act ->
                act.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                act.window.statusBarColor = ContextCompat.getColor(act, R.color.grayF6F6F6)
            }
        } catch (e: Exception) {
            Timber.tag(myTag).e("back pressed failed: ${e.message}")
        }
    }

    companion object {
        fun onSetupView(): DropBoxLoginFragment {
            val dialog = DropBoxLoginFragment()
            return dialog
        }
    }
}
