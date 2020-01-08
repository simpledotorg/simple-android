package org.simple.clinic.summary.prescribeddrugs

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import kotlinx.android.synthetic.main.drugs_summary_view.view.*
import org.simple.clinic.R
import org.simple.clinic.di.injector
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.util.RelativeTimestampGenerator
import org.simple.clinic.util.UserClock
import org.simple.clinic.widgets.visibleOrGone
import org.threeten.bp.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Named

private typealias UpdateClicked = () -> Unit

class DrugSummaryView(
    context: Context,
    attributeSet: AttributeSet
) : CardView(context, attributeSet), DrugSummaryUi {

  @field:[Inject Named("exact_date")]
  lateinit var exactDateFormatter: DateTimeFormatter

  @Inject
  lateinit var userClock: UserClock

  @Inject
  lateinit var timestampGenerator: RelativeTimestampGenerator

  init {
    LayoutInflater.from(context).inflate(R.layout.drugs_summary_view, this, true)
  }

  var updateClicked: UpdateClicked? = null

  override fun onFinishInflate() {
    super.onFinishInflate()
    if(isInEditMode) {
      return
    }

    context.injector<DrugSummaryViewInjector>().inject(this)
  }

  override fun populatePrescribedDrugs(prescribedDrugs: List<PrescribedDrug>) {
    bind(
        prescriptions = prescribedDrugs,
        dateFormatter = exactDateFormatter,
        userClock = userClock
    )
  }

  fun bind(
      prescriptions: List<PrescribedDrug>,
      dateFormatter: DateTimeFormatter,
      userClock: UserClock
  ) {
    updateButton.setOnClickListener { updateClicked?.invoke() }

    summaryViewGroup.visibleOrGone(prescriptions.isNotEmpty())

    setButtonText(prescriptions)

    removeAllDrugViews()

    if (prescriptions.isNotEmpty()) {
      prescriptions
          .map { drug -> DrugSummaryItemView.create(drugsSummaryContainer, drug) }
          .forEach { drugView -> drugsSummaryContainer.addView(drugView, drugsSummaryContainer.childCount - 1) }

      val lastUpdatedPrescription = prescriptions.maxBy { it.updatedAt }!!

      val lastUpdatedTimestamp = timestampGenerator.generate(lastUpdatedPrescription.updatedAt, userClock)

      lastUpdatedTimestampTextView.text = resources.getString(
          R.string.patientsummary_prescriptions_last_updated,
          lastUpdatedTimestamp.displayText(context, dateFormatter)
      )
    }
  }

  private fun removeAllDrugViews() {
    drugsSummaryContainer.removeAllViews()
  }

  private fun setButtonText(prescriptions: List<PrescribedDrug>) {
    updateButton.text = if (prescriptions.isEmpty()) {
      context.getString(R.string.patientsummary_prescriptions_add)
    } else {
      context.getString(R.string.patientsummary_prescriptions_update)
    }
  }
}
