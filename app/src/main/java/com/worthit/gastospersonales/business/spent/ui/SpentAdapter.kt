package com.worthit.gastospersonales.business.spent.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.worthit.gastospersonales.core.ui.toDisplayText
import com.worthit.gastospersonales.databinding.ItemSpentBinding

class SpentAdapter(
    private val onShare: (SpentItem) -> Unit,
    private val onEdit: (SpentItem) -> Unit,
    private val onDelete: (SpentItem) -> Unit
) : ListAdapter<SpentItem, SpentAdapter.ViewHolder>(Diff) {

    class ViewHolder(val binding: ItemSpentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemSpentBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.textDescription.text = item.description
        val locale = holder.itemView.resources.configuration.locales[0]
        holder.binding.textDetail.text = "${item.date.toDisplayText(locale)} · ${item.category}"
        holder.binding.textAmount.text = item.amount
        holder.binding.buttonShare.setOnClickListener { onShare(item) }
        holder.binding.buttonEdit.setOnClickListener { onEdit(item) }
        holder.binding.buttonDelete.setOnClickListener { onDelete(item) }
    }

    private object Diff : DiffUtil.ItemCallback<SpentItem>() {
        override fun areItemsTheSame(old: SpentItem, new: SpentItem) = old.id == new.id
        override fun areContentsTheSame(old: SpentItem, new: SpentItem) = old == new
    }
}
