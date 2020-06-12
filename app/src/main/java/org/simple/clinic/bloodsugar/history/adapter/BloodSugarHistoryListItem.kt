package org.simple.clinic.bloodsugar.history.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.style.TextAppearanceSpan
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.list_blood_sugar_history_item.*
import kotlinx.android.synthetic.main.list_new_blood_sugar_button.*
import org.simple.clinic.R
import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import org.simple.clinic.util.Truss
import org.simple.clinic.widgets.PagingItemAdapter
import org.simple.clinic.widgets.recyclerview.ViewHolderX
import org.simple.clinic.widgets.visibleOrGone

sealed class BloodSugarHistoryListItem : PagingItemAdapter.Item<Event> {

  object NewBloodSugarButton : BloodSugarHistoryListItem() {
    override fun layoutResId(): Int = R.layout.list_new_blood_sugar_button

    override fun render(holder: ViewHolderX, subject: Subject<Event>) {
      holder.newBloodSugarButton.setOnClickListener { subject.onNext(NewBloodSugarClicked) }
    }
  }

  data class BloodSugarHistoryItem(
      val measurement: BloodSugarMeasurement,
      val bloodSugarDate: String,
      val bloodSugarTime: String?,
      val isBloodSugarEditable: Boolean
  ) : BloodSugarHistoryListItem() {
    override fun layoutResId(): Int = R.layout.list_blood_sugar_history_item

    @SuppressLint("SetTextI18n")
    override fun render(holder: ViewHolderX, subject: Subject<Event>) {
      val context = holder.itemView.context
      val bloodSugarReading = measurement.reading
      val formattedBPDateTime = Truss()
          .pushSpan(dateTimeTextAppearance(context))
          .append(bloodSugarDateTime(context))
          .popSpan()
          .build()
      val isBloodSugarHigh = measurement.reading.isHigh
      val isBloodSugarLow = measurement.reading.isLow

      val displayUnit = context.getString(bloodSugarReading.displayUnit)
      val displayType = context.getString(bloodSugarReading.displayType)
      val readingPrefix = bloodSugarReading.displayValue
      val readingSuffix = "$displayUnit $displayType"

      holder.readingsTextView.text = "$readingPrefix${bloodSugarReading.displayUnitSeparator}$readingSuffix"
      holder.dateTimeTextView.text = formattedBPDateTime
      if (isBloodSugarHigh || isBloodSugarLow) {
        holder.bloodSugarIconImageView.setImageResource(R.drawable.ic_blood_sugar_filled)
      } else {
        holder.bloodSugarIconImageView.setImageResource(R.drawable.ic_blood_sugar_outline)
      }

      if (isBloodSugarEditable) {
        holder.itemView.setOnClickListener { subject.onNext(BloodSugarHistoryItemClicked(measurement)) }
      } else {
        holder.itemView.setOnClickListener(null)
      }
      holder.itemView.isClickable = isBloodSugarEditable
      holder.itemView.isFocusable = isBloodSugarEditable
      holder.editButton.visibleOrGone(isBloodSugarEditable)
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
        TextAppearanceSpan(context, R.style.Clinic_V2_TextAppearance_Caption_Grey1)
      } else {
        TextAppearanceSpan(context, R.style.Clinic_V2_TextAppearance_Body2Left_Grey1)
      }
    }
  }
}
