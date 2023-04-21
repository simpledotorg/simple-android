package org.simple.clinic.monthlyreports.list

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import io.reactivex.subjects.Subject
import org.simple.clinic.R
import org.simple.clinic.databinding.MonthlyReportListItemViewBinding
import org.simple.clinic.monthlyreports.util.parseMonthlyReportMonthStringToLocalDate
import org.simple.clinic.monthlyreports.util.getMonthlyReportFormattedMonthString
import org.simple.clinic.monthlyreports.util.getMonthlyReportSubmitStatus
import org.simple.clinic.questionnaireresponse.QuestionnaireResponse
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.recyclerview.BindingViewHolder
import java.time.format.DateTimeFormatter

data class MonthlyReportItem(
    val questionnaireResponse: QuestionnaireResponse,
    val dateTimeFormatter: DateTimeFormatter
) : ItemAdapter.Item<MonthlyReportItem.Event> {

  companion object {
    fun from(
        questionnaireResponses: List<QuestionnaireResponse>,
        dateTimeFormatter: DateTimeFormatter
    ): List<MonthlyReportItem> {
      return questionnaireResponses
          .sortedByDescending { parseMonthlyReportMonthStringToLocalDate(it.content) }
          .map {
            MonthlyReportItem(
                questionnaireResponse = it,
                dateTimeFormatter = dateTimeFormatter
            )
          }
    }
  }

  override fun layoutResId(): Int = R.layout.monthly_report_list_item_view

  override fun render(holder: BindingViewHolder, subject: Subject<Event>) {
    val context = holder.itemView.context
    val binding = holder.binding as MonthlyReportListItemViewBinding

    holder.itemView.setOnClickListener {
      subject.onNext(Event.ListItemClicked(questionnaireResponse))
    }

    val isSubmitted = getMonthlyReportSubmitStatus(questionnaireResponse.content)
    val month = getMonthlyReportFormattedMonthString(questionnaireResponse.content, dateTimeFormatter)

    binding.statusImageView.setImageResource(
        if (isSubmitted) R.drawable.ic_form_submitted
        else R.drawable.ic_form_not_submitted
    )

    binding.statusTextView.text = context.resources.getString(
        if (isSubmitted) R.string.reports_submitted
        else R.string.reports_submit_report
    )

    binding.statusTextView.setTextColor(ContextCompat.getColor(context,
        if (isSubmitted) R.color.simple_green_500
        else R.color.color_on_surface_67
    ))

    binding.monthTextView.text = context.resources.getString(
        R.string.reports_report,
        month
    )
  }

  sealed class Event {
    data class ListItemClicked(val questionnaireResponse: QuestionnaireResponse) : Event()
  }

  class DiffCallback : DiffUtil.ItemCallback<MonthlyReportItem>() {
    override fun areItemsTheSame(
        oldItem: MonthlyReportItem,
        newItem: MonthlyReportItem
    ): Boolean {
      return oldItem.questionnaireResponse.uuid == newItem.questionnaireResponse.uuid
    }

    override fun areContentsTheSame(
        oldItem: MonthlyReportItem,
        newItem: MonthlyReportItem
    ): Boolean {
      return oldItem == newItem
    }
  }
}
