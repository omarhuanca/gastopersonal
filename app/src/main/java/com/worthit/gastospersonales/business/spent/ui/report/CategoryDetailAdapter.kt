package com.worthit.gastospersonales.business.spent.ui.report

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.worthit.gastospersonales.core.ui.toDisplayText
import com.worthit.gastospersonales.databinding.ItemDetailEntryBinding

class CategoryDetailAdapter : ListAdapter<DetailEntry, CategoryDetailAdapter.ViewHolder>(Diff) {

    class ViewHolder(val binding: ItemDetailEntryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemDetailEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = getItem(position)
        val locale = holder.itemView.resources.configuration.locales[0]
        holder.binding.textDescription.text = entry.description
        holder.binding.textDate.text = entry.date.toDisplayText(locale)
        holder.binding.textAmount.text = entry.amount
    }

    private object Diff : DiffUtil.ItemCallback<DetailEntry>() {
        override fun areItemsTheSame(old: DetailEntry, new: DetailEntry) = old == new
        override fun areContentsTheSame(old: DetailEntry, new: DetailEntry) = old == new
    }
}
