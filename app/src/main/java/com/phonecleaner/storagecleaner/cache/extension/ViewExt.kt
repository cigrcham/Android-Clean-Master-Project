package com.phonecleaner.storagecleaner.cache.extension

import android.content.res.Resources
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group

fun Group.getReferencedViews() = referencedIds.map { rootView.findViewById<View>(it) }

fun Group.setAlphaForChild(alpha: Float) = getReferencedViews().forEach {
    it.alpha = alpha
}

fun ImageView.loadImageByPath(path: String) {
    var type = "null"
    var extension = MimeTypeMap.getFileExtensionFromUrl(path)
    if (extension != null) {
        type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension).toString()
    }
}

fun ConstraintLayout.setUiMenuFunction(isState: Boolean) {
    if (isState) {
        this.isEnabled = true
        this.getChildAt(0).alpha = 1F
        this.getChildAt(1).alpha = 1F
    } else {
        this.isEnabled = false
        this.getChildAt(0).alpha = 0.3F
        this.getChildAt(1).alpha = 0.3F
    }
}

fun dpToPx(dp: Int): Int {
    return (dp * Resources.getSystem().displayMetrics.density).toInt()
}

