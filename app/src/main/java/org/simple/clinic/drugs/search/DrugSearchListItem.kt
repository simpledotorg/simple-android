package org.simple.clinic.drugs.search

import androidx.paging.PagingData
import androidx.paging.TerminalSeparatorType.SOURCE_COMPLETE
import androidx.paging.insertSeparators
import androidx.paging.map
import androidx.recyclerview.widget.DiffUtil
import io.reactivex.subjects.Subject
import org.simple.clinic.R
import org.simple.clinic.databinding.ListItemDrugSearchBinding
import org.simple.clinic.widgets.PagingItemAdapter
import org.simple.clinic.widgets.recyclerview.BindingViewHolder

sealed class DrugSearchListItem : PagingItemAdapter.Item<DrugSearchListItem.DrugClicked> {

  companion object {

    fun from(searchResults: PagingData<Drug>): PagingData<DrugSearchListItem> {
      return searchResults
          .map(::DrugSearchResult)
          .insertSeparators(SOURCE_COMPLETE) { oldItem, newItem ->
            if (oldItem != null && newItem != null) {
              Divider
            } else {
              null
            }
          }
    }
  }

  data class DrugClicked(val drug: Drug)

  data class DrugSearchResult(val drug: Drug) : DrugSearchListItem() {

    override fun layoutResId() = R.layout.list_item_drug_search

    override fun render(holder: BindingViewHolder, subject: Subject<DrugClicked>) {
      val binding = holder.binding as ListItemDrugSearchBinding

      binding.drugItemCard.setOnClickListener { subject.onNext(DrugClicked(drug)) }

      binding.drugNameTextView.text = drugName(drug)
    }

    private fun drugName(drug: Drug): String {
      return buildString {
        append(drug.name)

        if (!drug.dosage.isNullOrBlank()) {
          append(", ${drug.dosage}")
        }

        if (drug.frequency != null) {
          append(", ${drug.frequency}")
        }
      }
    }
  }

  object Divider : DrugSearchListItem() {

    override fun layoutResId() = R.layout.list_item_drug_search_divider

    override fun render(holder: BindingViewHolder, subject: Subject<DrugClicked>) {

    }
  }

  class DiffCallback : DiffUtil.ItemCallback<DrugSearchListItem>() {

    override fun areItemsTheSame(
        oldItem: DrugSearchListItem,
        newItem: DrugSearchListItem
    ): Boolean {
      return when {
        oldItem is DrugSearchResult && newItem is DrugSearchResult -> oldItem.drug.id == newItem.drug.id
        oldItem is Divider && newItem is Divider -> false
        else -> false
      }
    }

    override fun areContentsTheSame(
        oldItem: DrugSearchListItem,
        newItem: DrugSearchListItem
    ): Boolean {
      return oldItem == newItem
    }
  }
}
