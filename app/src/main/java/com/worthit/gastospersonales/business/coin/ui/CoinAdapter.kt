package com.worthit.gastospersonales.business.coin.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.worthit.gastospersonales.databinding.ItemCoinBinding

class CoinAdapter(
    private val onEdit: (String) -> Unit,
    private val onDelete: (String) -> Unit
) : ListAdapter<String, CoinAdapter.ViewHolder>(Diff) {

    class ViewHolder(val binding: ItemCoinBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemCoinBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val code = getItem(position)
        holder.binding.textCode.text = code
        holder.binding.buttonEdit.setOnClickListener { onEdit(code) }
        holder.binding.buttonDelete.setOnClickListener { onDelete(code) }
    }

    private object Diff : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(old: String, new: String) = old == new
        override fun areContentsTheSame(old: String, new: String) = old == new
    }
}
