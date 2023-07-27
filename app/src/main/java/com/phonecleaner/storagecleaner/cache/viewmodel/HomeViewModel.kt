package com.phonecleaner.storagecleaner.cache.viewmodel

import android.content.Context
import com.phonecleaner.storagecleaner.cache.base.BaseViewModel
import com.phonecleaner.storagecleaner.cache.data.model.entity.Analytics
import com.phonecleaner.storagecleaner.cache.data.model.liveData.MutableStateLiveData
import com.phonecleaner.storagecleaner.cache.data.repository.Repository
import com.phonecleaner.storagecleaner.cache.utils.FileHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context, private val mainRepository: Repository
) : BaseViewModel() {
    private val myTag: String = this::class.java.simpleName
    var analyticsLiveData = MutableStateLiveData<Analytics>()

    fun getAnalytics() {
        try {
            analyticsLiveData.postLoading()
            val image = FileHelper.calculateImage(context)
            val video = FileHelper.calculateVideo(context)
            val used = FileHelper.getUsedStorage()
            val total = FileHelper.getTotalStorage()
            val document = FileHelper.calculateDocument()
            analyticsLiveData.postSuccess(
                Analytics(
                    image = image, video = video, document = document, used = used, total = total
                )
            )
        } catch (ex: Exception) {
            analyticsLiveData.postError(ex.message)
        }
    }
}