package com.phonecleaner.storagecleaner.cache.ui.dialog

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
import com.phonecleaner.storagecleaner.cache.databinding.LayoutVideoControlBinding
import com.phonecleaner.storagecleaner.cache.utils.formatTime
import com.phonecleaner.storagecleaner.cache.utils.playOrPause
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VideoDialog : DialogFragment(), MediaPlayer.OnPreparedListener, SurfaceHolder.Callback,
    MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    private lateinit var binding: LayoutVideoControlBinding
    private val myTag: String = this::class.java.simpleName

    /**
     * Set up MediaPlayer
     */
    val mp = MediaPlayer()
    var fileApp: FileApp? = null
    private val mHandler: Handler = Handler()
    private lateinit var surfaceHolder: SurfaceHolder

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        binding = LayoutVideoControlBinding.inflate(layoutInflater, container, false)

        fileApp?.let {
            try {
                setCallBackMedia()
                mp.reset()
                mp.setDataSource(it.path)
                mp.prepare()
                mp.start()
                updateProgressBar()
                val millis: Long = mp.duration.toLong()
                binding.tvEnd.text = formatTime(millis)
                binding.tvTitle.text = it.name
                binding.imgControls.playOrPause(true)
//                val layoutParamsSurface = if (mp.videoWidth >= mp.videoHeight) {
//                    ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, 0)
//                } else {
//                    ConstraintLayout.LayoutParams(0, ConstraintLayout.LayoutParams.MATCH_PARENT)
//                }
                val layoutParamsSurface =
                    ConstraintLayout.LayoutParams(mp.videoWidth, mp.videoHeight)
                layoutParamsSurface.dimensionRatio = "${mp.videoWidth} : l${mp.videoHeight}"
                layoutParamsSurface.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                layoutParamsSurface.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                layoutParamsSurface.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                layoutParamsSurface.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                binding.sfVideo.layoutParams = layoutParamsSurface
                surfaceHolder = binding.sfVideo.holder
                surfaceHolder.addCallback(this)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        binding.imgControls.setOnClickListener {
            if (mp.isPlaying) {
                mp.pause()
                mHandler.removeCallbacks(mUpdateTimeTask)
                binding.imgControls.playOrPause(false)
            } else {
                binding.imgControls.playOrPause(true)
                mp.start()
                updateProgressBar()
            }
        }
        return binding.root
    }

    private fun setCallBackMedia() {
        mp.setOnPreparedListener(this@VideoDialog)
        mp.setOnErrorListener(this@VideoDialog)
        mp.setOnCompletionListener(this@VideoDialog)
    }

    override fun getTheme(): Int {
        return R.style.AlertDialog
    }

    override fun onPause() {
        super.onPause()
        if (mp.isPlaying) {
            mp.pause()
            binding.imgControls.playOrPause(false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mp.isPlaying) {
            mp.stop()
        }
        mHandler.removeCallbacks(mUpdateTimeTask)
    }

    private fun updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100)
    }

    /**
     * Background Runnable thread
     */
    private val mUpdateTimeTask: Runnable = object : Runnable {
        override fun run() {
            val totalDuration = mp.duration.toLong()
            val currentDuration = mp.currentPosition.toLong()
            binding.tvStart.text = formatTime(currentDuration)

            // Update progress bar
            val progress: Int = getProgressPercentage(currentDuration, totalDuration).toInt()
            Log.d(myTag, "Progress: $progress")
            binding.prTime.progress = progress
            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 100)
        }
    }

    fun getProgressPercentage(currentDuration: Long, totalDuration: Long): Float {
        return (currentDuration.toFloat() / totalDuration.toFloat()) * 100
    }

    override fun surfaceCreated(sfView: SurfaceHolder) {
        mp.setDisplay(sfView)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun onPrepared(mp: MediaPlayer?) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {}

    override fun onCompletion(mp: MediaPlayer?) {
        mHandler.removeCallbacks(mUpdateTimeTask)
        binding.prTime.progress = 100
        binding.imgControls.playOrPause(false)
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        binding.imgControls.playOrPause(false)
        return false
    }

    companion object {
        fun onSetupView(file: FileApp): VideoDialog {
            val dialog = VideoDialog()
            dialog.fileApp = file
            return dialog
        }
    }
}