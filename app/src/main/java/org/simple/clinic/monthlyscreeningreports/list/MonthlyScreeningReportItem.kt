package org.simple.clinic.monthlyscreeningreports.list

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import io.reactivex.subjects.Subject
import org.simple.clinic.R
import org.simple.clinic.databinding.MonthlyScreeningReportItemViewBinding
import org.simple.clinic.monthlyscreeningreports.util.formatScreeningMonthStringToLocalDate
import org.simple.clinic.monthlyscreeningreports.util.getScreeningMonth
import org.simple.clinic.monthlyscreeningreports.util.getScreeningSubmitStatus
import org.simple.clinic.questionnaireresponse.QuestionnaireResponse
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.recyclerview.BindingViewHolder
import java.time.format.DateTimeFormatter

data class MonthlyScreeningReportItem(
    val questionnaireResponse: QuestionnaireResponse,
    val dateTimeFormatter: DateTimeFormatter
) : ItemAdapter.Item<MonthlyScreeningReportItem.Event> {

  companion object {
    fun from(
        questionnaireResponses: List<QuestionnaireResponse>,
        dateTimeFormatter: DateTimeFormatter
    ): List<MonthlyScreeningReportItem> {
      return questionnaireResponses
          .sortedByDescending { formatScreeningMonthStringToLocalDate(it.content) }
          .map {
            MonthlyScreeningReportItem(
                questionnaireResponse = it,
                dateTimeFormatter = dateTimeFormatter
            )
          }
    }
  }

  override fun layoutResId(): Int = R.layout.monthly_screening_report_item_view

  override fun render(holder: BindingViewHolder, subject: Subject<Event>) {
    val context = holder.itemView.context
    val binding = holder.binding as MonthlyScreeningReportItemViewBinding

    holder.itemView.setOnClickListener {
      subject.onNext(Event.ListItemClicked(questionnaireResponse))
    }

    val isSubmitted = getScreeningSubmitStatus(questionnaireResponse.content)
    val month = getScreeningMonth(questionnaireResponse.content, dateTimeFormatter)

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

  class DiffCallback : DiffUtil.ItemCallback<MonthlyScreeningReportItem>() {
    override fun areItemsTheSame(
        oldItem: MonthlyScreeningReportItem,
        newItem: MonthlyScreeningReportItem
    ): Boolean {
      return oldItem.questionnaireResponse.uuid == newItem.questionnaireResponse.uuid
    }

    override fun areContentsTheSame(
        oldItem: MonthlyScreeningReportItem,
        newItem: MonthlyScreeningReportItem
    ): Boolean {
      return oldItem == newItem
    }
  }
}
