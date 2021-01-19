package org.simple.clinic.bloodsugar.history.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.style.TextAppearanceSpan
import android.view.View
import io.reactivex.subjects.Subject
import org.simple.clinic.R
import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import org.simple.clinic.bloodsugar.BloodSugarReading
import org.simple.clinic.bloodsugar.BloodSugarUnitPreference
import org.simple.clinic.databinding.ListBloodSugarHistoryItemBinding
import org.simple.clinic.databinding.ListNewBloodSugarButtonBinding
import org.simple.clinic.util.Truss
import org.simple.clinic.widgets.PagingItemAdapter
import org.simple.clinic.widgets.recyclerview.BindingViewHolder
import org.simple.clinic.widgets.visibleOrGone

sealed class BloodSugarHistoryListItem : PagingItemAdapter.Item<Event> {

  object NewBloodSugarButton : BloodSugarHistoryListItem() {
    override fun layoutResId(): Int = R.layout.list_new_blood_sugar_button

    override fun render(holder: BindingViewHolder, subject: Subject<Event>) {
      val binding = holder.binding as ListNewBloodSugarButtonBinding

      binding.newBloodSugarButton.setOnClickListener { subject.onNext(NewBloodSugarClicked) }
    }
  }

  data class BloodSugarHistoryItem(
      val measurement: BloodSugarMeasurement,
      val bloodSugarDate: String,
      val bloodSugarTime: String?,
      val isBloodSugarEditable: Boolean,
      val bloodSugarUnitPreference: BloodSugarUnitPreference
  ) : BloodSugarHistoryListItem() {
    override fun layoutResId(): Int = R.layout.list_blood_sugar_history_item

    @SuppressLint("SetTextI18n")
    override fun render(holder: BindingViewHolder, subject: Subject<Event>) {
      val binding = holder.binding as ListBloodSugarHistoryItemBinding
      val context = holder.itemView.context
      val bloodSugarReading = measurement.reading
      val formattedBPDateTime = Truss()
          .pushSpan(dateTimeTextAppearance(context))
          .append(bloodSugarDateTime(context))
          .popSpan()
          .build()

      val displayUnit = context.getString(bloodSugarReading.displayUnit(bloodSugarUnitPreference))
      val displayType = context.getString(bloodSugarReading.displayType)
      val readingPrefix = bloodSugarReading.displayValue(bloodSugarUnitPreference)
      val readingSuffix = "$displayUnit $displayType"

      renderBloodSugarLevel(binding, context, measurement.reading)

      binding.readingsTextView.text = "$readingPrefix${bloodSugarReading.displayUnitSeparator}$readingSuffix"
      binding.dateTimeTextView.text = formattedBPDateTime

      if (isBloodSugarEditable) {
        holder.itemView.setOnClickListener { subject.onNext(BloodSugarHistoryItemClicked(measurement)) }
      } else {
        holder.itemView.setOnClickListener(null)
      }
      holder.itemView.isClickable = isBloodSugarEditable
      holder.itemView.isFocusable = isBloodSugarEditable
      binding.editButton.visibleOrGone(isBloodSugarEditable)
    }

    private fun renderBloodSugarLevel(binding: ListBloodSugarHistoryItemBinding, context: Context, reading: BloodSugarReading) {
      when {
        reading.isLow -> {
          binding.bloodSugarLevelTextView.visibility = View.VISIBLE
          binding.bloodSugarLevelTextView.text = context.getString(R.string.bloodsugar_level_low)
          binding.bloodSugarIconImageView.setImageResource(R.drawable.ic_blood_sugar_filled)
        }
        reading.isHigh -> {
          binding.bloodSugarLevelTextView.visibility = View.VISIBLE
          binding.bloodSugarLevelTextView.text = context.getString(R.string.bloodsugar_level_high)
          binding.bloodSugarIconImageView.setImageResource(R.drawable.ic_blood_sugar_filled)
        }
        else -> {
          binding.bloodSugarLevelTextView.visibility = View.GONE
          binding.bloodSugarIconImageView.setImageResource(R.drawable.ic_blood_sugar_outline)
        }
      }
    }

    private fun bloodSugarDateTime(context: Context): String {
      return if (bloodSugarTime != null) {
        context.getString(R.string.bloodsugarhistory_blood_sugar_date_time, bloodSugarDate, bloodSugarTime)
      } else {
        bloodSugarDate
      }
    }

    private fun dateTimeTextAppearance(context: Context): TextAppearanceSpan {
      return if (bloodSugarTime != null) {
        TextAppearanceSpan(context, R.style.TextAppearance_MaterialComponents_Caption)
      } else {
        TextAppearanceSpan(context, R.style.TextAppearance_Simple_Body2)
      }
    }
  }
}
