package org.simple.clinic.summary.prescribeddrugs

import android.content.Context
import android.util.AttributeSet
import androidx.cardview.widget.CardView
import com.jakewharton.rxbinding3.view.detaches
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.drugs_summary_view.view.*
import org.simple.clinic.R
import org.simple.clinic.bindUiToController
import org.simple.clinic.di.injector
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.drugs.selection.PrescribedDrugsScreenKey
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.util.RelativeTimestampGenerator
import org.simple.clinic.util.UserClock
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.visibleOrGone
import org.threeten.bp.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

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

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controllerFactory: DrugSummaryUiController.Factory

  private val internalEvents = PublishSubject.create<DrugSummaryEvent>()

  init {
    inflate(context, R.layout.drugs_summary_view, this)
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    context.injector<DrugSummaryViewInjector>().inject(this)

    val key = screenRouter.key<PatientSummaryScreenKey>(this)
    bindUiToController(
        ui = this,
        events = Observable.merge(screenCreates(), internalEvents),
        controller = controllerFactory.create(key.patientUuid),
        screenDestroys = detaches().map { ScreenDestroyed() }
    )
  }

  private fun screenCreates(): Observable<UiEvent> = Observable.just(ScreenCreated())

  override fun populatePrescribedDrugs(prescribedDrugs: List<PrescribedDrug>) {
    val alphabeticallySortedPrescribedDrugs = prescribedDrugs.sortedBy { it.name }
    bind(
        prescriptions = alphabeticallySortedPrescribedDrugs,
        dateFormatter = exactDateFormatter,
        userClock = userClock
    )
  }

  override fun showUpdatePrescribedDrugsScreen(patientUuid: UUID) {
    screenRouter.push(PrescribedDrugsScreenKey(patientUuid))
  }

  private fun bind(
      prescriptions: List<PrescribedDrug>,
      dateFormatter: DateTimeFormatter,
      userClock: UserClock
  ) {
    updateButton.setOnClickListener { internalEvents.onNext(PatientSummaryUpdateDrugsClicked()) }

    summaryViewGroup.visibleOrGone(prescriptions.isNotEmpty())

    setButtonText(prescriptions)

    removeAllDrugViews()

    if (prescriptions.isNotEmpty()) {
      prescriptions
          .map { drug -> DrugSummaryItemView.create(drugsSummaryContainer, drug) }
          .forEach { drugView -> drugsSummaryContainer.addView(drugView) }

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
