package org.simple.clinic.monthlyscreeningreports.list

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import io.reactivex.subjects.Subject
import org.simple.clinic.R
import org.simple.clinic.databinding.MonthlyScreeningReportItemViewBinding
import org.simple.clinic.questionnaireresponse.QuestionnaireResponse
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.recyclerview.BindingViewHolder
import java.util.UUID

data class MonthlyScreeningReportItem(
    val uuid: UUID,
    val submitted: Boolean,
    val month: String
) : ItemAdapter.Item<MonthlyScreeningReportItem.Event> {

  companion object {
    fun from(
        questionnaireResponses: List<QuestionnaireResponse>
    ): List<MonthlyScreeningReportItem> {
      return questionnaireResponses.map {
        MonthlyScreeningReportItem(
            uuid = it.uuid,
            submitted = try {
              it.content["submitted"] as Boolean
            } catch (ex: Exception) {
              false
            },
            month = try {
              it.content["month_string"] as String
            } catch (ex: Exception) {
              ""
            }
        )
      }
    }
  }

  override fun layoutResId(): Int = R.layout.monthly_screening_report_item_view

  override fun render(holder: BindingViewHolder, subject: Subject<Event>) {
    val context = holder.itemView.context
    val binding = holder.binding as MonthlyScreeningReportItemViewBinding

    holder.itemView.setOnClickListener {
      subject.onNext(Event.ListItemClicked(uuid))
    }

    binding.statusImageView.setImageResource(
        if (submitted) R.drawable.ic_report_submitted
        else R.drawable.ic_submit_report
    )

    binding.statusTextView.text = context.resources.getString(
        if (submitted) R.string.monthly_screening_reports_submitted
        else R.string.monthly_screening_reports_submit_report
    )

    binding.statusTextView.setTextColor(ContextCompat.getColor(context,
        if (submitted) R.color.simple_green_500
        else R.color.color_on_surface_67
    ))

    binding.monthTextView.text = context.resources.getString(
        R.string.monthly_screening_reports_report,
        month
    )
  }

  sealed class Event {
    data class ListItemClicked(val id: UUID) : Event()
  }

  class DiffCallback : DiffUtil.ItemCallback<MonthlyScreeningReportItem>() {
    override fun areItemsTheSame(
        oldItem: MonthlyScreeningReportItem,
        newItem: MonthlyScreeningReportItem
    ): Boolean {
      return oldItem.uuid == newItem.uuid
    }

    override fun areContentsTheSame(
        oldItem: MonthlyScreeningReportItem,
        newItem: MonthlyScreeningReportItem
    ): Boolean {
      return oldItem == newItem
    }
  }
}
