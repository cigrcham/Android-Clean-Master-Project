package com.phonecleaner.storagecleaner.cache.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.fragment.findNavController
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.databinding.FragmentCoolerBinding
import com.phonecleaner.storagecleaner.cache.base.BaseFragment
import com.phonecleaner.storagecleaner.cache.ui.activity.MainActivity
import com.phonecleaner.storagecleaner.cache.ui.dialog.AcceptNotificationDialog
import com.phonecleaner.storagecleaner.cache.ui.dialog.LauncherBlockDialogFragment
import com.phonecleaner.storagecleaner.cache.utils.showDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CoolerFragment : BaseFragment(), View.OnClickListener {
    private lateinit var binding: FragmentCoolerBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCoolerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
        activity?.let { act ->
            (act as MainActivity).getTemperatureCelsius()
        }
    }

    override fun initData() {
        super.initData()
        initObserve()
    }

    override fun initListener() {
        super.initListener()
        binding.tvCooler.setOnClickListener {
            goToCPUCooler()
        }
        binding.containerBlockNotification.setOnClickListener {
            gotoBlockNotify()
        }
        binding.containerRecoveryFile.setOnClickListener {
            goToRecoverFile()
        }
    }

    private fun initObserve() {
        activity?.let { act ->
            (act as MainActivity).celsiusPhoneLiveData.observe(viewLifecycleOwner) { value: String ->
                binding.tvCelsius.text = String.format("%.1f", value.toFloat())
            }
        }
    }

    private fun goToRecoverFile() {
        hideNavigationView()
        findNavController().navigate(R.id.scanRecoveryFileFragment)
    }


    private fun hideNavigationView() {
        (activity as MainActivity?)?.let {
            it.hideNavigationView()
        }
    }

    override fun onClick(v: View?) {
        when (view) {

        }
    }

    private fun goToCPUCooler() {
        activity?.let {
            hideNavigationView()
            findNavController().navigate(R.id.cpuCoolerFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        if (isPause) {
            showInBaseNavigationView()
        }
    }

    override fun onPause() {
        super.onPause()
        isPause = true
    }

    private var isPause: Boolean = false

    /**
     * Navigate to Open Dialog Block notification
     */
    private fun gotoBlockNotify() {
        activity?.let { act ->
            if (!NotificationManagerCompat.getEnabledListenerPackages(act)
                    .contains(act.packageCodePath)
            ) {
                val dialogPermission = AcceptNotificationDialog()
                dialogPermission.callBack = { isAccept ->
                    if (isAccept) {
                        try {
                            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                            startActivity(intent)
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                }
                act.showDialog(dialogPermission, parentFragmentManager)
            } else {
                val dialog = LauncherBlockDialogFragment.onSetupView()
                act.showDialog(dialog, parentFragmentManager)
            }
        }
    }
}