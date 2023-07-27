package com.phonecleaner.storagecleaner.cache.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.phonecleaner.storagecleaner.cache.base.BaseViewModel
import com.phonecleaner.storagecleaner.cache.data.model.entity.Account
import com.phonecleaner.storagecleaner.cache.data.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DataViewModel @Inject constructor(
    private val repository: Repository, @ApplicationContext private val context: Context
) : BaseViewModel() {
    private val myTag: String = this::class.java.simpleName
    val passcode: Flow<String> = repository.getPasscode
    val getAllAccount = MutableLiveData<List<Account>>()
    val accountLiveData = MutableLiveData<Account>()
    fun setLanguage(lang: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setLanguage(lang)
        }
    }

    fun setPasscode(passcode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setPasscode(passcode = passcode)
        }
    }
    fun setDropboxMail(mail: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setDropboxMail(mail)
        }
    }

    fun insertAccount(account: Account) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertAccount(account)
        }
    }

    fun getAllAccounts(type: String) {
        viewModelScope.launch(Dispatchers.IO) {
            getAllAccount.postValue(repository.getAllAccounts(type))
        }
    }

    fun getAccount(email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            accountLiveData.postValue(repository.getAccount(email))
        }
    }

}