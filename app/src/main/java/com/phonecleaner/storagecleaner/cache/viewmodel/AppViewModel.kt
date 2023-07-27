package com.phonecleaner.storagecleaner.cache.viewmodel

import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Environment
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.phonecleaner.storagecleaner.cache.base.BaseViewModel
import com.phonecleaner.storagecleaner.cache.data.model.entity.AppInstalled
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
import com.phonecleaner.storagecleaner.cache.data.model.liveData.MutableStateLiveData
import com.phonecleaner.storagecleaner.cache.data.repository.Repository
import com.phonecleaner.storagecleaner.cache.utils.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject


@HiltViewModel
class AppViewModel @Inject constructor(
    @ApplicationContext private val context: Context, private val repository: Repository
) : BaseViewModel() {

    var listAppLiveData = MutableStateLiveData<ArrayList<AppInstalled>>()
    var listApkLiveData = MutableStateLiveData<ArrayList<FileApp>>()
    val resetLiveData = MutableStateLiveData<Boolean>()
    val listAppBlock = MutableStateLiveData<List<AppInstalled>>()

    fun getAppInstalled(getIconBitmap: Boolean = false) {
        listAppLiveData.postLoading()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val packageManager = context.packageManager
                val list = packageManager.getInstalledApplications(0)
                val appDataList: ArrayList<AppInstalled> = arrayListOf()
                list.forEach { resolveInfo ->
                    if (packageManager.getLaunchIntentForPackage(resolveInfo.packageName) != null) with(
                        resolveInfo
                    ) {
                        val appInfo: ApplicationInfo =
                            packageManager.getApplicationInfo(resolveInfo.packageName, 0)
                        val file = File(appInfo.publicSourceDir)
                        if (resolveInfo.packageName != context.packageName) {
                            val appData = AppInstalled(
                                packageName = resolveInfo.packageName,
                                appName = loadLabel(context.packageManager) as String,
                                modified = file.lastModified(),
                                size = file.length()
                            )
                            if (getIconBitmap) {
                                appData.iconBitmap =
                                    drawableToBitmap(getAppIconDrawable(appData.packageName))
                            }
                            if (!appDataList.contains(appData)) {
                                appDataList.add(appData)
                            }
                        }
                    }
                }
                Log.d("TAG345", "getAppInstalled: ${appDataList.size}")
                Timber.d("list app ${appDataList.size}")
                listAppLiveData.postSuccess(appDataList)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        val width =
            if (!drawable.bounds.isEmpty) drawable.bounds.width() else drawable.intrinsicWidth
        val height =
            if (!drawable.bounds.isEmpty) drawable.bounds.height() else drawable.intrinsicHeight
        val bitmap = Bitmap.createBitmap(
            if (width <= 0) 1 else width, if (height <= 0) 1 else height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun getAppIconDrawable(packageName: String): Drawable {
        return context.packageManager.getApplicationIcon(packageName)
    }

    fun getListAppBlock() {
        viewModelScope.launch {
            repository.getAllAppBlock().catch { exception ->
                emit(listOf())
            }.collect {
                listAppBlock.postSuccess(it)
            }
        }
    }

    fun getApk() {
        listApkLiveData.postLoading()
        viewModelScope.launch(Dispatchers.IO) {
            val root = Environment.getExternalStorageDirectory()
            try {
                listApkLiveData.postSuccess(FileUtils.getApkFile(Environment.getExternalStorageDirectory()))
            } catch (e: Exception) {
                listApkLiveData.postError(e.message)
            }
        }
    }

    fun insertDataBase(item: AppInstalled) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertAppBlock(item)
        }
    }

    fun deleteDataBaseAppBlock(item: AppInstalled) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAppBlock(item)
        }
    }

    fun selectAll(isSelectAll: Boolean, listApp: List<AppInstalled>) {
        viewModelScope.launch(Dispatchers.IO) {
            if (isSelectAll) {
                listApp.forEach {
                    repository.insertAppBlock(it)
                }
            } else {
                repository.clearAllAppSelected()
            }
        }
    }
}