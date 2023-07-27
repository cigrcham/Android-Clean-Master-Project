package com.phonecleaner.storagecleaner.cache.extension

import com.phonecleaner.storagecleaner.cache.data.model.liveData.MutableStateLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import timber.log.Timber

suspend fun <T> Flow<T>.collectToSateLiveData(liveData: MutableStateLiveData<T>) {
    this.flowOn(Dispatchers.IO)
        .catch {
            Timber.d("thinhvh", "catch: ${Thread.currentThread().name}")
            Timber.d("thinhvh", "catch: ${it.message}")
            liveData.postError(it.message)
        }
        .onStart {
            liveData.postLoading()
        }.collect {
            Timber.d("thinhvh", "postSuccess: ${Thread.currentThread().name}")
            liveData.postSuccess(it)
        }
}