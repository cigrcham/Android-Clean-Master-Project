package com.phonecleaner.storagecleaner.cache.utils.customView

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.phonecleaner.storagecleaner.cache.data.model.response.SetMenuFunction
import com.phonecleaner.storagecleaner.cache.databinding.ViewBottomMenuBinding
import com.phonecleaner.storagecleaner.cache.extension.setUiMenuFunction
import com.phonecleaner.storagecleaner.cache.utils.singleton.SingletonMenu

class BottomMenuView : ConstraintLayout {
    private lateinit var binding: ViewBottomMenuBinding

    var onClickEvent: OnClickEvent? = null

    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        initView(context)
    }

    private fun initView(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = ViewBottomMenuBinding.inflate(inflater, this, true)
        initListener()
    }


    fun show() {
        if (SingletonMenu.getInstance().type == -1) {
            isGone = true
        } else {
            isVisible = true
            for (ids in binding.group.referencedIds.indices) {
                binding.root.findViewById<View>(binding.group.referencedIds[ids]).isGone =
                    ids != SingletonMenu.getInstance().type
            }
        }
    }

    fun initData(title: String) {
        binding.layoutMoveTo.tvTitle.text = title
    }

    fun initUi(size: Int) {
        if (size > 1) {
            binding.layoutNormal.layoutRename.setUiMenuFunction(false)
            binding.layoutNormal.layoutOpen.setUiMenuFunction(false)
            binding.layoutNormal.layoutConvert.setUiMenuFunction(false)
//            binding.layoutNormal.layoutHide.setUiMenuFunction(false)
            binding.layoutNormal.layoutProperties.setUiMenuFunction(false)
            binding.layoutMenuAlbum.layoutProperties.setUiMenuFunction(false)
            binding.layoutMenuAlbum.layoutOpen.setUiMenuFunction(false)
            binding.layoutApk.layoutUninstall.setUiMenuFunction(false)
            binding.layoutApk.layoutProperties.setUiMenuFunction(false)
            binding.layoutApk.layoutShare.setUiMenuFunction(false)
            binding.layoutMenuDropBox.layoutProperties.setUiMenuFunction(false)
            binding.layoutAccount.layoutRemove.setUiMenuFunction(false)
        } else {
            binding.layoutNormal.layoutRename.setUiMenuFunction(true)
            binding.layoutNormal.layoutOpen.setUiMenuFunction(true)
            binding.layoutNormal.layoutConvert.setUiMenuFunction(true)
//            binding.layoutNormal.layoutHide.setUiMenuFunction(true)
            binding.layoutNormal.layoutProperties.setUiMenuFunction(true)
            binding.layoutMenuAlbum.layoutProperties.setUiMenuFunction(true)
            binding.layoutMenuAlbum.layoutOpen.setUiMenuFunction(true)
            binding.layoutApk.layoutUninstall.setUiMenuFunction(true)
            binding.layoutApk.layoutProperties.setUiMenuFunction(true)
            binding.layoutApk.layoutShare.setUiMenuFunction(true)
            binding.layoutMenuDropBox.layoutProperties.setUiMenuFunction(true)
            binding.layoutAccount.layoutRemove.setUiMenuFunction(true)
        }
    }

    fun initListener() {
        binding.layoutNormal.layoutCopy.setOnClickListener {
            onClickEvent?.onSelectedType(SetMenuFunction.COPY)
        }
        binding.layoutNormal.layoutMove.setOnClickListener {
            onClickEvent?.onSelectedType(SetMenuFunction.MOVE)
        }
        binding.layoutNormal.layoutDelete.setOnClickListener {
            onClickEvent?.onSelectedType(SetMenuFunction.DELETE)
        }
        binding.layoutNormal.layoutRename.setOnClickListener {
            onClickEvent?.onSelectedType(SetMenuFunction.RENAME)
        }
//        binding.layoutNormal.layoutZipFiles.setOnClickListener {
//            onClickEvent?.onSelectedType(SetMenuFunction.ZIP)
//        }
        binding.layoutNormal.layoutProperties.setOnClickListener {
            onClickEvent?.onSelectedType(SetMenuFunction.PROPERTIES)
        }
        binding.layoutNormal.layoutOpen.setOnClickListener {
            onClickEvent?.onSelectedType(SetMenuFunction.OPEN)
        }
        binding.layoutNormal.layoutShare.setOnClickListener {
            onClickEvent?.onSelectedType(SetMenuFunction.SHARE)
        }
        binding.layoutNormal.layoutFavorite.setOnClickListener {
            onClickEvent?.onSelectedType(SetMenuFunction.FAVORITE)
        }
//        binding.layoutNormal.layoutConvert.setOnClickListener {
//            onClickEvent?.onSelectedType(SetMenuFunction.CONVERT)
//        }
//        binding.layoutNormal.layoutHide.setOnClickListener {
//            onClickEvent?.onSelectedType(SetMenuFunction.HIDE)
//        }
        binding.layoutHandler.layoutPaste.setOnClickListener {
            onClickEvent?.onSelectedType(SetMenuFunction.PASTE)
        }
        binding.layoutHandler.layoutCreate.setOnClickListener {
            onClickEvent?.onSelectedType(SetMenuFunction.CREATE)
        }
        binding.layoutHandler.layoutCancel.setOnClickListener {
            onClickEvent?.onSelectedType(SetMenuFunction.CANCEL)
        }
        binding.layoutRecycleBin.layoutDelete.setOnClickListener {
            onClickEvent?.onSelectedType(SetMenuFunction.DELETE_RECYCLE_BIN)
        }
        binding.layoutRecycleBin.layoutRestock.setOnClickListener {
            onClickEvent?.onSelectedType(SetMenuFunction.RESTOCK)
        }
        binding.layoutApk.layoutUninstall.setOnClickListener {
            onClickEvent?.onSelectedType(SetMenuFunction.UNINSTALL)
        }
        binding.layoutApk.layoutProperties.setOnClickListener {
            onClickEvent?.onSelectedType(SetMenuFunction.PROPERTIES)
        }
        binding.layoutApk.layoutShare.setOnClickListener {
            onClickEvent?.onSelectedType(SetMenuFunction.APK_SHARE)
        }
        binding.layoutAccount.layoutRename.setOnClickListener {
            onClickEvent?.onSelectedType(SetMenuFunction.ACCOUNT_RENAME)
        }
        binding.layoutAccount.layoutRemove.setOnClickListener {
            onClickEvent?.onSelectedType(SetMenuFunction.REMOVE)
        }
        binding.layoutMenuExtract.layoutExtract.setOnClickListener {
            onClickEvent?.onSelectedType(SetMenuFunction.EXTRACT)
        }
        binding.layoutMenuExtract.layoutCreate.setOnClickListener {
            onClickEvent?.onSelectedType(SetMenuFunction.CREATE)
        }
        binding.layoutMenuExtract.layoutCancel.setOnClickListener {
            onClickEvent?.onSelectedType(SetMenuFunction.CANCEL)
        }
        binding.layoutMoveTo.layoutInternal.setOnClickListener {
            onClickEvent?.onSelectedType(SetMenuFunction.MOVE_TO_INTERNAL)
        }
        binding.layoutMoveTo.layoutDropbox.setOnClickListener {
            onClickEvent?.onSelectedType(SetMenuFunction.MOVE_TO_DROPBOX)
        }
        binding.layoutMenuDropBox.layoutDownload.setOnClickListener {
            onClickEvent?.onSelectedType(SetMenuFunction.DOWNLOAD)
        }
        binding.layoutMenuDropBox.layoutProperties.setOnClickListener {
            onClickEvent?.onSelectedType(SetMenuFunction.PROPERTIES)
        }
        binding.layoutMenuAlbum.layoutCopy.setOnClickListener {
            onClickEvent?.onSelectedType(SetMenuFunction.COPY)
        }
        binding.layoutMenuAlbum.layoutMove.setOnClickListener {
            onClickEvent?.onSelectedType(SetMenuFunction.MOVE)
        }
        binding.layoutMenuAlbum.layoutDelete.setOnClickListener {
            onClickEvent?.onSelectedType(SetMenuFunction.DELETE)
        }
        binding.layoutMenuAlbum.layoutProperties.setOnClickListener {
            onClickEvent?.onSelectedType(SetMenuFunction.PROPERTIES)
        }
        binding.layoutMenuAlbum.layoutOpen.setOnClickListener {
            onClickEvent?.onSelectedType(SetMenuFunction.OPEN)
        }
        binding.layoutMenuHide.layoutUnHide.setOnClickListener {
            onClickEvent?.onSelectedType(SetMenuFunction.UN_HIDE)
        }
    }

    interface OnClickEvent {
        fun onSelectedType(type: SetMenuFunction)
    }

}
