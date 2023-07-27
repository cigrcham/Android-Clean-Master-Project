package com.phonecleaner.storagecleaner.cache.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import com.phonecleaner.storagecleaner.cache.utils.FileUtils.getFileDocument
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.roundToInt

object FileHelper {
    fun calculateMemory(): String {
        val internalStatFs = StatFs(Environment.getRootDirectory().absolutePath)
        val externalStatFs = StatFs(Environment.getExternalStorageDirectory().absolutePath)
        val internalTotal = internalStatFs.blockCountLong * internalStatFs.blockSizeLong
        val internalFree = internalStatFs.availableBlocksLong * internalStatFs.blockSizeLong
        val externalTotal = externalStatFs.blockCountLong * externalStatFs.blockSizeLong
        val externalFree = externalStatFs.availableBlocksLong * externalStatFs.blockSizeLong
        val total = internalTotal + externalTotal
        val free = internalFree + externalFree
        val used = total - free
        val fileList = mutableListOf<File>()
        getFileList(Environment.getRootDirectory(), fileList)
        getFileList(Environment.getExternalStorageDirectory(), fileList)
        return "$total-$used-${fileList.size}"
    }

    fun calculateImage(context: Context): String {
        val imageProjection = arrayOf(MediaStore.Images.Media.DATA)

        val cursor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageProjection, null, null
            )
        } else {
            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                imageProjection,
                null,
                null,
                null,
            )
        }
        var size = 0L
        var count = 0
        cursor.use {
            val data = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            while (cursor?.moveToNext() == true) {
                val file = data?.let { it1 -> cursor.getString(it1) }?.let { it2 -> File(it2) }
                file?.length()?.let {
                    size += it
                }
                count++
            }
        }
        return "$size-$count"
    }

    fun calculateVideo(context: Context): String {
        val videoProjection = arrayOf(MediaStore.Video.Media.DATA)

        val cursor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoProjection, null, null
            )
        } else {
            context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                videoProjection,
                null,
                null,
                null,
            )
        }
        var size = 0L
        var count = 0
        cursor.use {
            val data = cursor?.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            while (cursor?.moveToNext() == true) {
                val file = data?.let { it1 -> cursor.getString(it1) }?.let { it2 -> File(it2) }
                file?.length()?.let {
                    size += it
                }
                count++
            }
        }
        return "$size-$count"
    }

    fun calculateMusic(context: Context): String {
        val videoProjection = arrayOf(MediaStore.Audio.Media.DATA)

        val cursor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, videoProjection, null, null
            )
        } else {
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                videoProjection,
                null,
                null,
                null,

                )
        }
        var size = 0L
        var count = 0
        cursor.use {
            val data = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            while (cursor?.moveToNext() == true) {
                val file = data?.let { it1 -> cursor.getString(it1) }?.let { it2 -> File(it2) }
                file?.length()?.let {
                    size += it
                }
                count++
            }
        }
        return "$size-$count"
    }

    fun calculateDocument(): Long {
        var totalDocument = 0L
        getFileDocument(Environment.getExternalStorageDirectory()).forEach {
            totalDocument += it.size
        }
        return totalDocument
    }

    fun calculateApp(context: Context): String {
        var size = 0L
        var count = 0
        val applicationInfoList: List<ApplicationInfo> =
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                context.packageManager.getInstalledApplications(
//                    PackageManager.ApplicationInfoFlags.of(
//                        0
//                    )
//                )
//            } else {
            context.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
//            }
        context.packageManager
        for (packageInfo in applicationInfoList) {
            if (packageInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
                size += File(packageInfo.publicSourceDir).length()
                count++
            }
        }
        return "$size-$count"
    }

    fun calculateOthers(context: Context): String {
        val used = calculateMemory().split("-")[1].toLong()
        val totalFiles = calculateMemory().split("-")[2].toLong()
        val imageSize = calculateImage(context).split("-")[0].toLong()
        val imageFiles = calculateImage(context).split("-")[1].toLong()
        val videoSize = calculateVideo(context).split("-")[0].toLong()
        val videoFiles = calculateVideo(context).split("-")[1].toLong()
        val musicSize = calculateMusic(context).split("-")[0].toLong()
        val musicFiles = calculateMusic(context).split("-")[1].toLong()
        val appSize = calculateApp(context).split("-")[0].toLong()
        val appFiles = calculateApp(context).split("-")[1].toLong()
        return "${used - imageSize - videoSize - musicSize - appSize}-${totalFiles - imageFiles - videoFiles - musicFiles - appFiles}"
    }

    fun getFileList(files: File, list: MutableList<File>) {
        if (files.isDirectory) {
            files.listFiles()?.let {
                for (file in it) {
                    if (file.isDirectory) {
                        getFileList(file.absoluteFile, list)
                    } else {
                        list.add(file)
                    }
                }
            }
        } else {
            list.add(files)
        }
    }

    fun sizeFormat(size: Long): String {
        var result = size.toDouble() / 1024
        if (result < 1024) return "${result.roundToInt()} KB"
        result /= 1024
        if (result < 1024) return String.format("%.2f MB", result)
        result /= 1024
        return String.format("%.2f GB", result)
    }

    fun dateFormat(milliseconds: Long): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val date: LocalDateTime =
                LocalDateTime.ofInstant(Instant.ofEpochMilli(milliseconds), ZoneId.systemDefault())
            date.format(DateTimeFormatter.ofPattern(DATE_FORMAT))
        } else {
            SimpleDateFormat(
                DATE_FORMAT, Locale.getDefault()
            ).format(milliseconds)
        }
    }

    fun getUsedStorage(): Long {
        val internalStatFs = StatFs(Environment.getRootDirectory().absolutePath)
        val externalStatFs = StatFs(Environment.getExternalStorageDirectory().absolutePath)
        val internalTotal = internalStatFs.blockCountLong * internalStatFs.blockSizeLong
        val internalFree = internalStatFs.availableBlocksLong * internalStatFs.blockSizeLong
        val externalTotal = externalStatFs.blockCountLong * externalStatFs.blockSizeLong
        val externalFree = externalStatFs.availableBlocksLong * externalStatFs.blockSizeLong
        val total = internalTotal + externalTotal
        val free = internalFree + externalFree
        return total - free
    }

    fun getTotalStorage(): Long {
        val internalStatFs = StatFs(Environment.getRootDirectory().absolutePath)
        val externalStatFs = StatFs(Environment.getExternalStorageDirectory().absolutePath)
        val internalTotal = internalStatFs.blockCountLong * internalStatFs.blockSizeLong
        val internalFree = internalStatFs.availableBlocksLong * internalStatFs.blockSizeLong
        val externalTotal = externalStatFs.blockCountLong * externalStatFs.blockSizeLong
        val externalFree = externalStatFs.availableBlocksLong * externalStatFs.blockSizeLong
        return internalTotal + externalTotal
    }

    private const val DATE_FORMAT = "dd/MM/yyyy"

    fun convertImageToPdf(pathImage: String?, onSuccess: () -> Unit, onFail: () -> Unit) {
        if (!pathImage.isNullOrEmpty()) {
            try {
                // Load JPG file into bitmap
                val bitmap: Bitmap = BitmapFactory.decodeFile(pathImage)

                // Create a PdfDocument with a page of the same size as the image
                val document = PdfDocument()
                val pageInfo: PdfDocument.PageInfo =
                    PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
                val page: PdfDocument.Page = document.startPage(pageInfo)

                // Draw the bitmap onto the page
                val canvas: Canvas = page.canvas
                canvas.drawBitmap(bitmap, 0f, 0f, null)
                document.finishPage(page)

                // Write the PDF file to a file
                val directoryPath: String = Environment.getExternalStorageDirectory().toString()
                document.writeTo(FileOutputStream("$directoryPath/${System.currentTimeMillis()}.pdf"))
                document.close()
                onSuccess.invoke()
            } catch (ex: Exception) {
                onFail.invoke()
            }
        } else {
            onFail.invoke()
        }
    }

//    fun convertMediaFile(
//        pathInput: String?,
//        pathOutPut: String,
//        onSuccess: () -> Unit,
//        onFail: () -> Unit
//    ) {
//        val command = arrayOf("-i", pathInput, pathOutPut)
//        val executionId = FFmpeg.executeAsync(
//            command
//        ) { _, returnCode ->
//            when (returnCode) {
//                RETURN_CODE_SUCCESS -> {
//                    Log.d("TAG999", "success: ")
//                    onSuccess.invoke()
//                }
//                RETURN_CODE_CANCEL -> {
//                    Log.d("TAG999", "cancel: ")
//                    onFail.invoke()
//                }
//                else -> {
//                    onFail.invoke()
//                    Log.d("TAG999", "fail: ${returnCode}")
//                }
//            }
//        }
//    }

    fun convertDocToPDF(pathDoc: String?) {
//        try {
//            val document = com.aspose.words.Document(pathDoc)
//            val directoryPath: String = Environment.getExternalStorageDirectory().toString()
//            val filePDF = "$directoryPath/examConvert.pdf"
//            document.save(filePDF)
//        } catch (e: java.lang.Exception) {
//            e.printStackTrace()
//        }
    }

    fun convertPPTXtoPDF(path: String) {
        try {
            val directoryPath: String = Environment.getExternalStorageDirectory().toString()
            val filePDF = File("$directoryPath/examEmTuanAnh.pdf")
            val inputStream = FileInputStream(path)
            val outPutStream = FileOutputStream(filePDF)

        } catch (ex: Exception) {

        }
    }
}