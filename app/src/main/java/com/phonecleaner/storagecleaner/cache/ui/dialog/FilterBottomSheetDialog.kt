package com.phonecleaner.storagecleaner.cache.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.data.model.entity.FilterType
import com.phonecleaner.storagecleaner.cache.databinding.DialogFilterBinding
import com.phonecleaner.storagecleaner.cache.ui.adapters.FilterAdapter

class FilterBottomSheetDialog(
    var filterType: FilterType,
    val onSelectedItem: (FilterType) -> Unit
) : BottomSheetDialogFragment() {
    private lateinit var binding: DialogFilterBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style. BottomSheetDialog)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = FilterAdapter() {
            onSelectedItem.invoke(it)
            dismiss()
        }
        adapter.setFilterType(filterType)
        binding.recycler.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recycler.adapter = adapter
    }
}