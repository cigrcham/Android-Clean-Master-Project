package com.phonecleaner.storagecleaner.cache.data.model.liveData

import androidx.lifecycle.MutableLiveData

/**
 * Created by Thinhvh on 24/08/2022.
 * Phone: 0398477967
 * Email: thinhvh.fpt@gmail.com
 */

class MutableStateLiveData<T> : MutableLiveData<State<T>>() {
    fun postLoading() {
        postValue(State<T>().loading())
    }

    fun postError(errorMsg: String?) {
        postValue(State<T>().error(errorMsg ?: "An error occurred, please try again!"))
    }

    fun postErrorData(errorData: T) {
        postValue(State<T>().error(errorData))
    }

    fun postSuccess(data: T) {
        postValue(State<T>().success(data))
    }
}