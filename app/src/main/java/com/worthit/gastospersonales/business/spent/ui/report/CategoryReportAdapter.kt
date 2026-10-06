package com.worthit.gastospersonales.business.spent.ui.report

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.worthit.gastospersonales.R
import com.worthit.gastospersonales.databinding.ItemReportGroupBinding

// Each group: the category, one "Total: 1400 BOB" line per coin, and "2 gastos".
class CategoryReportAdapter(
    private val onClick: (ReportGroup) -> Unit
) : ListAdapter<ReportGroup, CategoryReportAdapter.ViewHolder>(Diff) {

    class ViewHolder(val binding: ItemReportGroupBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemReportGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val group = getItem(position)
        val context = holder.itemView.context

        holder.binding.root.setOnClickListener { onClick(group) }
        holder.binding.textCategory.text = group.category
        holder.binding.textTotal.text = CategoryDetailTexts(context).totalLines(group.totals)
        holder.binding.textCount.text =
            context.resources.getQuantityString(R.plurals.report_spents_count, group.count, group.count)
    }

    private object Diff : DiffUtil.ItemCallback<ReportGroup>() {
        override fun areItemsTheSame(old: ReportGroup, new: ReportGroup) = old.category == new.category
        override fun areContentsTheSame(old: ReportGroup, new: ReportGroup) = old == new
    }
}
