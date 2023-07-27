package com.phonecleaner.storagecleaner.cache.extension

import android.webkit.MimeTypeMap
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileDelete
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileHide
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileType
import com.phonecleaner.storagecleaner.cache.data.model.entity.Folder
import com.phonecleaner.storagecleaner.cache.utils.Constants
import com.phonecleaner.storagecleaner.cache.utils.FileUtils
import com.phonecleaner.storagecleaner.cache.utils.FileUtils.getInternalStoragePath
import com.phonecleaner.storagecleaner.cache.utils.singleton.SingletonListFileApp
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

private const val myTag = "FileExt"

fun File.getMimeType(fallback: String = ""): String {
    return MimeTypeMap.getFileExtensionFromUrl(toString())
        ?.run { MimeTypeMap.getSingleton().getMimeTypeFromExtension(lowercase()) } ?: this.extension
}

fun File.getFileCount(): Int {
    return listFiles()?.filter {

        !it.name.startsWith('.')
    }?.size ?: 0
}

fun FileApp.isAudio(): Boolean {
    return getMediaType(this.type) == FileType.AUDIO || this.convertToFile().extension.lowercase() in listOf(
        "3gp",
        "m4a",
        "amr",
        "flac",
        "mid",
        "xmf",
        "mxmf",
        "rtttl",
        "rtx",
        "ota",
        "imy",
        "mp3",
        "mkv",
        "ogg",
        "wav"
    )
}

fun FileApp.isVideo(): Boolean {
    return getMediaType(this.type) == FileType.VIDEO || this.convertToFile().extension.lowercase() in listOf(
        "3gp", "mp4", "mkv", "webm"
    )
}

fun FileApp.isApk(): Boolean {
    return this.path.lowercase().endsWith(Constants.APK_SUFFIX.lowercase())
}

fun FileApp.isTxt(): Boolean {
    return this.path.lowercase().endsWith(Constants.TXT_SUFFIX.lowercase())
}

fun FileApp.isPptx(): Boolean {
    return this.path.lowercase().endsWith(Constants.PPTX_SUFFIX.lowercase())
}

fun FileApp.isXlsx(): Boolean {
    return this.path.lowercase().endsWith(Constants.XLSX_SUFFIX.lowercase())
}

fun FileApp.isPdf(): Boolean {
    return this.path.lowercase().endsWith(Constants.PDF_SUFFIX.lowercase())
}

fun FileApp.isZip(): Boolean {
    return getMediaType(this.type) == FileType.ZIP || this.convertToFile().extension.lowercase() == "zip"
}

fun FileApp.isImage(): Boolean {
    return getMediaType(this.type) == FileType.IMAGE || (File(this.path)).extension == "png" || (File(
        this.path
    )).extension == "jpg"
}

fun FileApp.isDocx(): Boolean {
    return (File(this.path)).extension == "docx"
}

fun FileApp.isDirectory(): Boolean {
    return File(this.path).isDirectory
}

fun FileDelete.isDirectory(): Boolean {
    return File(this.currentPath).isDirectory
}

fun File.isDirectory(): Boolean {
    return isDirectory
}

fun File.isCacheFolder(): Boolean {
    return name.lowercase().contains(Constants.CACHE_FOLDER_NAME.lowercase()) && isDirectory
}

fun FileApp.isPackageFolder(): Boolean {
    return type.lowercase() == Constants.PACKAGE_TYPE.lowercase()
}

fun FileDelete.isAudio(): Boolean {
    return getMediaType(this.type) == FileType.AUDIO || File(this.originPath).extension.lowercase() in listOf(
        "3gp",
        "mp4",
        "m4a",
        "amr",
        "flac",
        "mid",
        "xmf",
        "mxmf",
        "rtttl",
        "rtx",
        "ota",
        "imy",
        "mp3",
        "mkv",
        "ogg",
        "wav"
    )
}

fun FileDelete.isVideo(): Boolean {
    return this.getMediaType(this.type) == FileType.VIDEO || File(this.originPath).extension.lowercase() in listOf(
        "3gp", "mp4", "mkv", "webm"
    )
}

fun FileDelete.isApk(): Boolean {
    return this.originPath.lowercase().endsWith(Constants.APK_SUFFIX.lowercase())
}

fun FileDelete.isTxt(): Boolean {
    return this.originPath.lowercase().endsWith(Constants.TXT_SUFFIX.lowercase())
}

fun FileDelete.isPptx(): Boolean {
    return this.originPath.lowercase().endsWith(Constants.PPTX_SUFFIX.lowercase())
}

fun FileDelete.isXlsx(): Boolean {
    return this.originPath.lowercase().endsWith(Constants.XLSX_SUFFIX.lowercase())
}

fun FileDelete.isPdf(): Boolean {
    return this.originPath.lowercase().endsWith(Constants.PDF_SUFFIX.lowercase())
}

fun FileDelete.isZip(): Boolean {
    return getMediaType(this.type) == FileType.ZIP
}

fun FileDelete.isImage(): Boolean {
    return getMediaType(this.type) == FileType.IMAGE
}


fun FileHide.isDirectory(): Boolean {
    return File(this.currentPath).isDirectory
}

fun FileHide.isAudio(): Boolean {
    return getMediaType(this.type) == FileType.AUDIO || File(this.originPath).extension.lowercase() in listOf(
        "3gp",
        "mp4",
        "m4a",
        "amr",
        "flac",
        "mid",
        "xmf",
        "mxmf",
        "rtttl",
        "rtx",
        "ota",
        "imy",
        "mp3",
        "mkv",
        "ogg",
        "wav"
    )
}

fun FileHide.isVideo(): Boolean {
    return getMediaType(this.type) == FileType.VIDEO || File(this.originPath).extension.lowercase() in listOf(
        "3gp", "mp4", "mkv", "webm"
    )
}

fun FileHide.isApk(): Boolean {
    return this.originPath.lowercase().endsWith(Constants.APK_SUFFIX.lowercase())
}

fun FileHide.isTxt(): Boolean {
    return this.originPath.lowercase().endsWith(Constants.TXT_SUFFIX.lowercase())
}

fun FileHide.isPptx(): Boolean {
    return this.originPath.lowercase().endsWith(Constants.PPTX_SUFFIX.lowercase())
}

fun FileHide.isXlsx(): Boolean {
    return this.originPath.lowercase().endsWith(Constants.XLSX_SUFFIX.lowercase())
}

fun FileHide.isPdf(): Boolean {
    return this.originPath.lowercase().endsWith(Constants.PDF_SUFFIX.lowercase())
}

fun FileHide.isZip(): Boolean {
    return getMediaType(this.type) == FileType.ZIP
}

fun FileHide.isImage(): Boolean {
    return getMediaType(this.type) == FileType.IMAGE
}

fun File.convertToFileApp(): FileApp? {
    if (this.exists()) {
        val name =
            if (this.absolutePath == FileUtils.getInternalStoragePath()) Constants.HOME else this.name
        return if (!name.startsWith(".")) {
            val path = this.path
            val size = this.length()
            val dateModified = this.lastModified()
            val mimeType = this.getMimeType()
            FileApp(
                name = name, path = path, size = size, type = mimeType, dateModified = dateModified
            )
        } else null
    }
    return null
}

fun FileApp.convertToFile(): File {
    return File(this.path)
}

fun FileHide.convertToFile(): File {
    return File(this.currentPath)
}

fun Folder.convertToFile(): File {
    return File(this.path)
}

fun FileApp.convertToFileDelete(destinationPath: String): FileDelete? {
    return try {
        FileDelete(name = this.name,
            originPath = this.path,
            currentPath = destinationPath,
            size = File(destinationPath).walkTopDown().filter { it.isFile }.map { it.length() }
                .sum(),
            type = this.type,
            dateModified = this.dateModified)
    } catch (ex: Exception) {
        Timber.tag(myTag).e("convert file delete failed: ${ex.message}")
        null
    }
}


fun FileApp.convertToFileHide(destinationPath: String): FileHide? {
    return try {
        FileHide(name = this.name,
            originPath = this.path,
            currentPath = destinationPath,
            size = File(destinationPath).walkTopDown().filter { it.isFile }.map { it.length() }
                .sum(),
            type = this.type,
            dateModified = this.dateModified)
    } catch (ex: Exception) {
        Timber.tag(myTag).e("convert file delete failed: ${ex.message}")
        null
    }
}

fun File.convertToFolder(): Folder? {
    return try {
        val list = arrayListOf<FileApp>()
        this.listFiles()?.forEach { file ->
            file.convertToFileApp()?.let { list.add(it) }
        }
        Folder(
            name = this.name, path = this.absolutePath, coverPath = this.path, listFile = list
        )
    } catch (ex: Exception) {
        Timber.tag(myTag).e("convert file delete failed: ${ex.message}")
        null
    }
}

fun FileApp.getMediaType(mimeType: String): FileType {
    if (mimeType.startsWith("image")) return FileType.IMAGE
    if (mimeType.startsWith("video")) return FileType.VIDEO
    if (mimeType.startsWith("audio")) return FileType.AUDIO
    if (mimeType.startsWith("application/zip")) return FileType.ZIP
    return FileType.UNKNOWN
}

fun FileDelete.getMediaType(mimeType: String): FileType {
    if (mimeType.startsWith("image")) return FileType.IMAGE
    if (mimeType.startsWith("video")) return FileType.VIDEO
    if (mimeType.startsWith("audio")) return FileType.AUDIO
    if (mimeType.startsWith("application/zip")) return FileType.ZIP
    return FileType.UNKNOWN
}

fun FileHide.getMediaType(mimeType: String): FileType {
    if (mimeType.startsWith("image")) return FileType.IMAGE
    if (mimeType.startsWith("video")) return FileType.VIDEO
    if (mimeType.startsWith("audio")) return FileType.AUDIO
    if (mimeType.startsWith("application/zip")) return FileType.ZIP
    return FileType.UNKNOWN
}

fun File.copyFile(destinationPath: String, extension: String) {
    var path = "$destinationPath.$extension"
    var count = 1
    while (!File(path).createNewFile()) {
        path = "${destinationPath}($count).${extension}"
        count++
    }
    FileInputStream(this).use { fis ->
        FileOutputStream(File(path)).use { os ->
            val buffer = ByteArray(1024)
            var len: Int
            while (fis.read(buffer).also { len = it } != -1) {
                os.write(buffer, 0, len)
            }
        }
    }
    File(path).convertToFileApp()?.let { SingletonListFileApp.getInstance().listFileApp.add(it) }
}

fun File.copyDirectory(destinationPath: String) {
    var path = destinationPath
    var count = 1
    /**
     * Create path
     */
    while (!File(path).mkdir()) {
        path = "$destinationPath($count)"
        count++
    }

    if (this.listFiles()?.isNotEmpty() == true) {
        this.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                file.copyDirectory("$path/${file.name}")
            } else {
                FileInputStream(file).use { fis ->
                    FileOutputStream(File("$path/${file.name}")).use { os ->
                        val buffer = ByteArray(1024)
                        var len: Int
                        while (fis.read(buffer).also { len = it } != -1) {
                            os.write(buffer, 0, len)
                        }
                    }
                }
                File("$path/${file.name}").convertToFileApp()
                    ?.let { SingletonListFileApp.getInstance().listFileApp.add(it) }
            }
        }
    }
}

fun File.moveFile(destinationPath: String, extension: String) {
    var path = "$destinationPath.$extension"
    var count = 1
    while (!File(path).createNewFile()) {
        path = "${destinationPath}($count).${extension}"
        count++
    }
    FileInputStream(this).use { fis ->
        FileOutputStream(File(path)).use { os ->
            val buffer = ByteArray(1024)
            var len: Int
            while (fis.read(buffer).also { len = it } != -1) {
                os.write(buffer, 0, len)
            }
        }
    }
    if (path != destinationPath) {
        File(path).convertToFileApp()
            ?.let { SingletonListFileApp.getInstance().listFileApp.add(it) }
    }
    this.delete()
}

fun File.moveDirectory(destinationPath: String) {
    var path = destinationPath
    var count = 1
    while (!File(path).mkdir()) {
        path = "$destinationPath($count)"
        count++
    }
    if (this.listFiles()?.isNotEmpty() == true) {
        this.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                file.copyDirectory("$path/${file.name}")
            } else {
                FileInputStream(file).use { fis ->
                    FileOutputStream(File("$path/${file.name}")).use { os ->
                        val buffer = ByteArray(1024)
                        var len: Int
                        while (fis.read(buffer).also { len = it } != -1) {
                            os.write(buffer, 0, len)
                        }
                    }
                }
                if (path != destinationPath) {
                    File("$path/${file.name}").convertToFileApp()
                        ?.let { SingletonListFileApp.getInstance().listFileApp.add(it) }
                }
            }
        }
    }
    this.deleteRecursively()
}

fun File.moveFileRecycleBin(destinationPath: String) {
    try {
        FileInputStream(this).use { fis ->
            FileOutputStream(File(destinationPath)).use { os ->
                val buffer = ByteArray(1024)
                var len: Int
                while (fis.read(buffer).also { len = it } != -1) {
                    os.write(buffer, 0, len)
                }
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}

fun File.moveFolderRecycleBin(destinationPath: String) {
    if (this.listFiles()?.isNotEmpty() == true) {
        this.listFiles()?.forEach { file ->
            if (!file.name.startsWith(".") && file.exists()) {
                if (file.isDirectory) {
                    file.moveFolderRecycleBin("$destinationPath/${file.name}")
                } else {
                    file.moveFileRecycleBin("$destinationPath/${file.name}")
                }
            }
        }
    }
}

fun FileApp.isDocument(): Boolean {
    return this.path.endsWith("xlsx") || this.path.endsWith("docx") || this.path.endsWith("pptx") || this.path.endsWith(
        "pdf"
    ) || this.path.endsWith("txt")
}

fun FileApp.nameAndExtension(): String {
    return this.path.substringAfterLast("/")
}


