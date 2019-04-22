package org.simple.clinic.patient.recent

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.xwray.groupie.ViewHolder
import io.reactivex.subjects.Subject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.patient.Gender
import org.simple.clinic.summary.GroupieItemWithUiEvents
import org.simple.clinic.summary.RelativeTimestamp
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.visibleOrGone
import java.util.UUID

sealed class RecentPatientItemType<T : ViewHolder>(adapterId: Long) : GroupieItemWithUiEvents<T>(adapterId) {
  override lateinit var uiEvents: Subject<UiEvent>
}

data class RecentPatientItem(
    val uuid: UUID,
    val name: String,
    val age: Int,
    val lastBp: LastBp?,
    val gender: Gender
) : RecentPatientItemType<RecentPatientItem.RecentPatientViewHolder>(uuid.hashCode().toLong()) {

  override fun getLayout(): Int = R.layout.recent_patient_item_view

  override fun createViewHolder(itemView: View): RecentPatientViewHolder {
    return RecentPatientViewHolder(itemView)
  }

  @SuppressLint("SetTextI18n")
  override fun bind(viewHolder: RecentPatientViewHolder, position: Int) {
    viewHolder.itemView.setOnClickListener {
      uiEvents.onNext(RecentPatientItemClicked(patientUuid = uuid))
    }

    viewHolder.render(name, age, lastBp, gender)
  }

  data class LastBp(
      val systolic: Int,
      val diastolic: Int,
      val updatedAtRelativeTimestamp: RelativeTimestamp
  )

  class RecentPatientViewHolder(rootView: View) : ViewHolder(rootView) {
    private val nameAgeTextView by bindView<TextView>(R.id.recentpatient_item_nameage)
    private val lastBpTextView by bindView<TextView>(R.id.recentpatient_item_last_bp)
    private val lastBpLabel by bindView<View>(R.id.recentpatient_item_last_bp_label)
    private val genderImageView by bindView<ImageView>(R.id.recentpatient_item_gender)

    fun render(
        name: String,
        age: Int,
        lastBp: LastBp?,
        gender: Gender
    ) {
      nameAgeTextView.text = itemView.resources.getString(R.string.patients_recentpatients_nameage, name, age)

      val lastBpText = lastBpText(itemView.context, lastBp)
      lastBpTextView.text = lastBpText
      lastBpLabel.visibleOrGone(lastBpText.isNotBlank())
      genderImageView.setImageResource(gender.displayIconRes)
    }

    private fun lastBpText(context: Context, lastBp: LastBp?): String {
      return lastBp?.run { "$systolic/$diastolic, ${updatedAtRelativeTimestamp.displayText(context)}" } ?: ""
    }
  }
}

object SeeAllItem : RecentPatientItemType<ViewHolder>(0) {
  override fun getLayout() = R.layout.see_all_item_view

  override fun bind(viewHolder: ViewHolder, position: Int) {
  }
}
