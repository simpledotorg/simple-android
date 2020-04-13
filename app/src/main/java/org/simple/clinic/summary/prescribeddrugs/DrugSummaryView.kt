package org.simple.clinic.summary.prescribeddrugs

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
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
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.setBottomMarginRes
import org.simple.clinic.widgets.setCompoundDrawableStart
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
        .filter { it.requestCode == DRUGS_REQCODE_ALERT_FACILITY_CHANGE && it.succeeded() }
        .map { AlertFacilityChangeSheet.readContinuationExtra<ContinueToScreen>(it.data!!).screenKey }
        .takeUntil(screenDestroys)
        .subscribe(screenRouter::push)
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

    summaryViewGroup.visibleOrGone(prescriptions.isNotEmpty())
    emptyMedicinesTextView.visibleOrGone(prescriptions.isEmpty())

    setButtonText(prescriptions)
    setButtonIcon(prescriptions)

    drugSummaryViewRoot.setBottomMarginRes(
        if (prescriptions.isEmpty()) R.dimen.spacing_0
        else R.dimen.spacing_16
    )

    removeAllDrugViews()

    if (prescriptions.isNotEmpty()) {
      prescriptions
          .map { drug -> DrugSummaryItemView_Old.create(drugsSummaryContainer, drug) }
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

  private fun setButtonIcon(prescriptions: List<PrescribedDrug>) {
    val drawableRes = if (prescriptions.isEmpty()) {
      R.drawable.ic_add_circle_blue1_24dp
    } else {
      R.drawable.ic_edit_medicine
    }
    updateButton.setCompoundDrawableStart(drawableRes)
  }
}
