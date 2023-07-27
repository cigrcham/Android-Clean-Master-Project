package com.phonecleaner.storagecleaner.cache.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.phonecleaner.storagecleaner.cache.databinding.FragmentViewstubBinding

abstract class BaseViewStubFragment<T : ViewDataBinding> : BaseFragment() {
    private lateinit var stubBinding: FragmentViewstubBinding
    private var mSavedInstanceState: Bundle? = null
    private var hasInflated = false
    private var mViewStub: ViewStub? = null
    private var visible = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        stubBinding = FragmentViewstubBinding.inflate(inflater, container, false)
        mViewStub = stubBinding.fragmentViewStub.viewStub
        mViewStub?.layoutResource = getViewStubLayoutResource()
        mSavedInstanceState = savedInstanceState

        if (visible && !hasInflated) {
            val inflatedView = mViewStub?.inflate()
            inflatedView?.let {
                DataBindingUtil.bind<T>(inflatedView)?.let {
                    onCreateViewAfterViewStubInflated(
                        it, inflatedView, mSavedInstanceState
                    )
                    hasInflated = true
                }
            }
        }
        return stubBinding.root
    }

    protected abstract fun onCreateViewAfterViewStubInflated(
        binding: T, inflatedView: View, savedInstanceState: Bundle?
    )

    @LayoutRes
    protected abstract fun getViewStubLayoutResource(): Int

    override fun onResume() {
        super.onResume()
        visible = true
        if (mViewStub != null && !hasInflated) {
            val inflatedView = mViewStub?.inflate()
            inflatedView?.let {
                DataBindingUtil.bind<T>(inflatedView)?.let {
                    onCreateViewAfterViewStubInflated(
                        it, inflatedView, mSavedInstanceState
                    )
                    hasInflated = true
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hasInflated = false
    }

    override fun onPause() {
        super.onPause()
        visible = false
    }

    override fun onDetach() {
        super.onDetach()
        hasInflated = false
    }
}