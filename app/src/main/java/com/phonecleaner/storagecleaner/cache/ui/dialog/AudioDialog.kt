package com.phonecleaner.storagecleaner.cache.ui.dialog

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
import com.phonecleaner.storagecleaner.cache.databinding.LayoutAudioControlBinding
import com.phonecleaner.storagecleaner.cache.utils.Constants
import com.phonecleaner.storagecleaner.cache.utils.formatTime
import com.phonecleaner.storagecleaner.cache.utils.playOrPause
import timber.log.Timber

class AudioDialog : DialogFragment(), MediaPlayer.OnPreparedListener,
    MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private lateinit var binding: LayoutAudioControlBinding
    private val mHandler: Handler = Handler()

    //set up MediaPlayer
    val mp = MediaPlayer()
    var fileApp: FileApp? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = LayoutAudioControlBinding.inflate(layoutInflater, container, false)

        fileApp?.let { it ->
            try {
                setCallBackMedia()
                mp.reset()
                mp.setDataSource(it.path)
                mp.prepare()
                mp.start()
                updateProgressBar()
                val millis = mp.duration.toLong()
                binding.tvEnd.text = formatTime(millis)
                binding.tvTitle.text = it.name
                binding.imgControls.playOrPause(true)

                Glide.with(this)
                    .load(Constants.AUDIO.plus(it.path))
                    .error(R.drawable.song_default)
                    .into(binding.imgPreview)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
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

    override fun onPrepared(media: MediaPlayer?) {
    }

    fun updateProgressBar() {
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

            // Updating progress bar
            val progress = getProgressPercentage(currentDuration, totalDuration).toInt()
            Timber.d("vao nhe $progress")
            //Log.d("Progress", ""+progress);
            binding.prTime.progress = progress

            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 100)
        }
    }

    fun getProgressPercentage(currentDuration: Long, totalDuration: Long): Float {
        return (currentDuration.toFloat() / totalDuration.toFloat()) * 100
    }

    fun setCallBackMedia() {
        mp.setOnPreparedListener(this)
        mp.setOnCompletionListener(this)
        mp.setOnErrorListener(this)
    }

    override fun onCompletion(p0: MediaPlayer?) {
        Timber.d("vao nhe end")
        mHandler.removeCallbacks(mUpdateTimeTask)
        binding.imgControls.playOrPause(false)
    }

    override fun onError(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
        binding.imgControls.playOrPause(false)
        return false
    }

    companion object {
        fun onSetupView(file: FileApp): AudioDialog {
            val dialog = AudioDialog()
            dialog.fileApp = file
            return dialog
        }
    }
}