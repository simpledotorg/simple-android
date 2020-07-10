package org.simple.clinic.summary.prescribeddrugs

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.jakewharton.rxbinding3.view.detaches
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.drugs_summary_view.view.*
import org.simple.clinic.R
import org.simple.clinic.bindUiToController
import org.simple.clinic.di.injector
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.drugs.selection.PrescribedDrugsScreenKey
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.alertchange.AlertFacilityChangeSheet
import org.simple.clinic.facility.alertchange.Continuation.ContinueToScreen
import org.simple.clinic.router.screen.ActivityResult
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.summary.DRUGS_REQCODE_ALERT_FACILITY_CHANGE
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.util.RelativeTimestampGenerator
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.extractSuccessful
import org.simple.clinic.util.toLocalDateAtZone
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.setCompoundDrawableStart
import org.simple.clinic.widgets.setPaddingBottom
import org.simple.clinic.widgets.visibleOrGone
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

class DrugSummaryView(
    context: Context,
    attributeSet: AttributeSet
) : CardView(context, attributeSet), DrugSummaryUi {

  @field:[Inject Named("full_date")]
  lateinit var fullDateFormatter: DateTimeFormatter

  @Inject
  lateinit var userClock: UserClock

  @Inject
  lateinit var timestampGenerator: RelativeTimestampGenerator

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var controllerFactory: DrugSummaryUiController.Factory

  private val screenKey by unsafeLazy {
    screenRouter.key<PatientSummaryScreenKey>(this)
  }

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

    val screenDestroys = detaches().map { ScreenDestroyed() }
    bindUiToController(
        ui = this,
        events = Observable.merge(screenCreates(), internalEvents),
        controller = controllerFactory.create(screenKey.patientUuid),
        screenDestroys = screenDestroys
    )
    setupAlertResults(screenDestroys)
  }

  @SuppressLint("CheckResult")
  private fun setupAlertResults(screenDestroys: Observable<ScreenDestroyed>) {
    screenRouter.streamScreenResults()
        .ofType<ActivityResult>()
        .extractSuccessful(DRUGS_REQCODE_ALERT_FACILITY_CHANGE) { intent ->
          AlertFacilityChangeSheet.readContinuationExtra<ContinueToScreen>(intent).screenKey
        }
        .takeUntil(screenDestroys)
        .subscribe(screenRouter::push)
  }

  private fun screenCreates(): Observable<UiEvent> = Observable.just(ScreenCreated())

  override fun populatePrescribedDrugs(prescribedDrugs: List<PrescribedDrug>) {
    val alphabeticallySortedPrescribedDrugs = prescribedDrugs.sortedBy { it.name }
    bind(
        prescriptions = alphabeticallySortedPrescribedDrugs,
        dateFormatter = fullDateFormatter,
        userClock = userClock
    )
  }

  override fun showUpdatePrescribedDrugsScreen(patientUuid: UUID, currentFacility: Facility) {
    val alertFacilityChangeIntent = AlertFacilityChangeSheet.intent(
        context,
        currentFacility.name,
        ContinueToScreen(PrescribedDrugsScreenKey(patientUuid))
    )

    activity.startActivityForResult(alertFacilityChangeIntent, DRUGS_REQCODE_ALERT_FACILITY_CHANGE)
  }

  private fun bind(
      prescriptions: List<PrescribedDrug>,
      dateFormatter: DateTimeFormatter,
      userClock: UserClock
  ) {
    updateButton.setOnClickListener { internalEvents.onNext(PatientSummaryUpdateDrugsClicked()) }

    drugsSummaryContainer.visibleOrGone(prescriptions.isNotEmpty())
    emptyMedicinesTextView.visibleOrGone(prescriptions.isEmpty())

    setButtonText(prescriptions)
    setButtonIcon(prescriptions)

    drugsSummaryContainer.removeAllViews()

    if (prescriptions.isNotEmpty()) {
      prescriptions
          .map { drug ->
            val drugItemView = LayoutInflater.from(context).inflate(R.layout.list_patientsummary_prescripton_drug, this, false) as DrugSummaryItemView
            drugItemView.render(drug.name, drug.dosage.orEmpty(), dateFormatter.format(drug.updatedAt.toLocalDateAtZone(userClock.zone)))
            drugItemView
          }
          .forEach(drugsSummaryContainer::addView)
    }

    val itemContainerBottomPadding = if (prescriptions.size > 1) {
      R.dimen.patientsummary_drug_summary_item_container_bottom_padding_8
    } else {
      R.dimen.patientsummary_drug_summary_item_container_bottom_padding_24
    }
    drugsSummaryContainer.setPaddingBottom(itemContainerBottomPadding)
  }

  private fun setButtonText(prescriptions: List<PrescribedDrug>) {
    updateButton.text = if (prescriptions.isEmpty()) {
      context.getString(R.string.patientsummary_prescriptions_add)
    } else {
      context.getString(R.string.patientsummary_prescriptions_update)
    }
  }

  private fun setButtonIcon(prescriptions: List<PrescribedDrug>) {
    val drawableRes = if (prescriptions.isEmpty()) {
      R.drawable.ic_add_circle_blue1_24dp
    } else {
      R.drawable.ic_edit_medicine
    }
    updateButton.setCompoundDrawableStart(drawableRes)
  }
}
