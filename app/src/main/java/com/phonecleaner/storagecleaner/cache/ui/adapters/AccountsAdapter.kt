package com.phonecleaner.storagecleaner.cache.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.phonecleaner.storagecleaner.cache.R
import com.phonecleaner.storagecleaner.cache.base.BaseFragment
import com.phonecleaner.storagecleaner.cache.data.model.entity.Account
import com.phonecleaner.storagecleaner.cache.databinding.ItemFolderLinearBinding
import com.phonecleaner.storagecleaner.cache.extension.setSelectedAccount
import timber.log.Timber

class AccountsAdapter(
    private val onItemSelectListener: (Int) -> Unit = {}
) : RecyclerView.Adapter<AccountsAdapter.ListViewHolder>() {

    private val accountList = mutableListOf<Account>()
    private var onClickEvent: BaseFragment.OnClickAccountEvent? = null
    private var isSelect = false
    private var accountAmountSelect = 0
    var onItemSelected: (Int) -> Unit = {}

    fun setOnClickEvent(callback: BaseFragment.OnClickAccountEvent) {
        this.onClickEvent = callback
    }

    fun setData(accountList: List<Account>) {
        Timber.d("load data Account set")
        this.accountList.clear()
        this.accountList.addAll(accountList)
        notifyDataSetChanged()
    }

    fun addItem(item: Account) {
        accountList.add(item)
        notifyItemInserted(accountList.size)
    }

    fun deleteItem(index: Int) {
        accountList.removeAt(index)
        if (accountList.isEmpty()) {
            accountList.addAll(listOf())
            notifyDataSetChanged()
        } else {
            notifyItemRemoved(index)
            notifyItemRangeChanged(index, accountList.size)
        }
    }

    fun multiSelect(isSelect: Boolean) {
        Timber.d("load data do select")
        this.isSelect = isSelect
        accountList.setSelectedAccount(isSelect)
        notifyDataSetChanged()
    }

    inner class ListViewHolder(itemView: ItemFolderLinearBinding) :
        RecyclerView.ViewHolder(itemView.root) {

        private var binding = itemView

        fun onBind(item: Account, position: Int) {

            binding.imgThumbnails.setImageResource(R.drawable.ic_dropbox)
            binding.tvTitle.text = item.name
            binding.tvDetail.text = item.email

//            binding.imgSelected.setImageResource(
//                if (item.isSelected) R.drawable.ic_file_selected else R.drawable.ic_file_unselected
//            )
            binding.imgSelected.isChecked = item.isSelected
            binding.imgSelected.setOnClickListener {
                item.isSelected = !item.isSelected
                binding.imgSelected.isChecked = item.isSelected
                onClickEvent?.onSelectItem(item, position)
                if (item.isSelected) {
                    accountAmountSelect++
                } else {
                    accountAmountSelect--
                }
                onItemSelectListener(accountAmountSelect)
                onItemSelected.invoke(position)
            }

            binding.root.setOnClickListener {
                onClickEvent?.onClickItem(item, position)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ListViewHolder {
        return ListViewHolder(
            ItemFolderLinearBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }


    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        accountList.getOrNull(position)?.let { account ->
            (holder).onBind(account, position)
        }
    }

    override fun getItemCount(): Int {
        return accountList.size
    }
}