package com.phonecleaner.storagecleaner.cache.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

open class BaseViewModel : ViewModel() {
    var isLoading = MutableLiveData<Boolean>()
    var showMessageLiveData = MutableLiveData<String>()

    protected fun showLoading(isShow: Boolean) {
        isLoading.postValue(isShow)
    }

    protected fun showMessage(message: String) {
        showMessageLiveData.postValue(message)
    }
}