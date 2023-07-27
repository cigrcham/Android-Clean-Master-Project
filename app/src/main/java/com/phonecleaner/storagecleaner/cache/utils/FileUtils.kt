package com.phonecleaner.storagecleaner.cache.utils

import android.os.Environment
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
import com.phonecleaner.storagecleaner.cache.extension.convertToFile
import com.phonecleaner.storagecleaner.cache.extension.convertToFileApp
import com.phonecleaner.storagecleaner.cache.extension.isApk
import com.phonecleaner.storagecleaner.cache.extension.isAudio
import com.phonecleaner.storagecleaner.cache.extension.isCacheFolder
import com.phonecleaner.storagecleaner.cache.extension.isImage
import com.phonecleaner.storagecleaner.cache.extension.isVideo
import com.phonecleaner.storagecleaner.cache.extension.isZip
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.channels.FileChannel

object FileUtils {

    private val myTag = "FileUtils"

    fun getApkFile(folder: File): ArrayList<FileApp> {
        val listApk = ArrayList<FileApp>()
        val listFile = folder.listFiles()
        if (listFile != null) {
            for (i in listFile.indices) {
                if (listFile[i].isDirectory) {
                    listApk.addAll(getApkFile(listFile[i]))
                } else {
                    listFile[i].convertToFileApp()?.let {
                        if (it.isApk()) {
                            listApk.add(it)
                        }
                    }
                }
            }
        }
        return listApk
    }

    fun getFileZip(folder: File): ArrayList<FileApp> {
        val listZip = ArrayList<FileApp>()
        val listFile = folder.listFiles()
        if (listFile != null) {
            for (i in listFile.indices) {
                if (listFile[i].isDirectory) {
                    listZip.addAll(getFileZip(listFile[i]))
                } else {
                    listFile[i].convertToFileApp()?.let {
                        if (it.isZip()) {
                            listZip.add(it)
                        }
                    }
                }
            }
        }
        return listZip
    }

    fun getFileVideo(folder: File): ArrayList<FileApp> {
        val listVideo = ArrayList<FileApp>()
        val listFile = folder.listFiles()
        if (listFile != null) {
            for (i in listFile.indices) {
                if (listFile[i].isDirectory) {
                    listVideo.addAll(getFileVideo(listFile[i]))
                } else {
                    listFile[i].convertToFileApp()?.let {
                        if (it.isVideo()) {
                            listVideo.add(it)
                        }
                    }
                }
            }
        }
        return listVideo
    }

    fun getFileAudio(folder: File): ArrayList<FileApp> {
        val listAudio = ArrayList<FileApp>()
        val listFile = folder.listFiles()
        if (listFile != null) {
            for (i in listFile.indices) {
                if (listFile[i].isDirectory) {
                    listAudio.addAll(getFileAudio(listFile[i]))
                } else {
//                    if (!listFile[i].isHidden) {
                    listFile[i].convertToFileApp()?.let {
                        if (it.isAudio()) {
                            listAudio.add(it)
                        }
                    }
//                    }
                }
            }
        }
        return listAudio
    }

    fun getFileImage(folder: File): ArrayList<FileApp> {
        val listImage = ArrayList<FileApp>()
        val listFile = folder.listFiles()
        if (listFile != null) {
            for (i in listFile.indices) {
                if (listFile[i].isDirectory) {
                    listImage.addAll(getFileImage(listFile[i]))
                } else {
                    listFile[i].convertToFileApp()?.let {
                        if (it.isImage() && it.convertToFile().extension.lowercase() in listOf(
                                "jpg", "png", "jpeg"
                            )
                        ) {
                            listImage.add(it)
                        }
                    }
                }
            }
        }
        return listImage
    }

    fun getFileDocument(folder: File): ArrayList<FileApp> {
        val listDocument = ArrayList<FileApp>()
        val listFile = folder.listFiles()
        if (listFile != null) {
            for (i in listFile.indices) {
                listFile.getOrNull(i)?.let { file ->
                    if (file.isDirectory) {
                        listDocument.addAll(getFileDocument(file))
                    } else {
                        if (file.extension in listOf("xlsx", "docx", "pptx", "pdf", "txt")) {
                            file.convertToFileApp()?.let { listDocument.add(it) }
                        } else {
                            Timber.tag(myTag).d("file is not document")
                        }
                    }
                }
            }
        }
        return listDocument
    }

    fun getTotalFileCount(folder: File): Int {
        var numberOfFiles = 0
        if (folder.exists()) {
            val files: Array<File> = folder.listFiles() ?: return numberOfFiles
            for (file in files) {
                if (file.isDirectory) {
                    numberOfFiles += getTotalFileCount(file)
                } else {
                    numberOfFiles++
                }
            }
        }
        return numberOfFiles
    }

    fun FileUtils.getInternalStoragePath() =
        if (File("/storage/emulated/0").exists()) "/storage/emulated/0" else Environment.getExternalStorageDirectory().absolutePath.trimEnd(
            '/'
        )

    fun getAllFilesInFolder(folder: File): ArrayList<FileApp> {
        val listFiles = ArrayList<FileApp>()
        val files = folder.listFiles()
        if (files != null) {
            for (file in files) {
                if (file.isDirectory) {
                    listFiles.addAll(getAllFilesInFolder(file))
                } else {
                    file.convertToFileApp()?.let {
                        it.isSelected = true
                        listFiles.add(it)
                    }
                }
            }
        }
        return listFiles
    }

    fun deleteCacheInFolder(fileApp: FileApp) {
        val dir = Constants.INTERNAL_STORAGE_PATH.plus("/").plus(fileApp.path)
        val file = File(dir)
        if (file.exists()) {
            val listCacheFolder = file.listFiles()?.filter { it.isCacheFolder() }
            listCacheFolder?.let {
                for (cacheFolder in listCacheFolder) {
                    if (cacheFolder.exists()) {
                        deleteRecursive(cacheFolder)
                    }
                }
            }
        }
    }

    fun deleteRecursive(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory) {
            fileOrDirectory.listFiles()?.let {
                for (fileChild in it) {
                    deleteRecursive(fileChild)
                }
            }
        }
        val delete = fileOrDirectory.delete()
    }

    fun getFileList(str: String): Array<File>? {
        val file = File(str)
        if (!file.isDirectory) {
            return null
        }
        return if (file.listFiles() != null) {
            file.listFiles()
        } else null
    }

    fun copy(oldFile: File, newFile: File) {
        var source: FileChannel? = null
        var destination: FileChannel? = null
        source = FileInputStream(oldFile).channel
        destination = FileOutputStream(newFile).channel
        source.transferTo(0, source.size(), destination)
        source?.close()
        destination?.close()
    }
}