package org.simple.clinic.summary.prescribeddrugs

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.google.android.material.card.MaterialCardView
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.common.ui.theme.SimpleTheme
import org.simple.clinic.di.injector
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.drugs.selection.PrescribedDrugsScreenKey
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.alertchange.AlertFacilityChangeSheet
import org.simple.clinic.facility.alertchange.Continuation.ContinueToScreen
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenResultBus
import org.simple.clinic.navigation.v2.keyprovider.ScreenKeyProvider
import org.simple.clinic.summary.PatientSummaryChildView
import org.simple.clinic.summary.PatientSummaryModelUpdateCallback
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.summary.prescribeddrugs.ui.DrugSummary
import org.simple.clinic.util.RelativeTimestampGenerator
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.unsafeLazy
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

class DrugSummaryView(
    context: Context,
    attributeSet: AttributeSet
) : MaterialCardView(context, attributeSet), DrugSummaryUi, DrugSummaryUiActions, PatientSummaryChildView {

  @Inject
  @Named("full_date")
  lateinit var fullDateFormatter: DateTimeFormatter

  @Inject
  lateinit var userClock: UserClock

  @Inject
  lateinit var timestampGenerator: RelativeTimestampGenerator

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var screenResults: ScreenResultBus

  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var effectHandlerFactory: DrugSummaryEffectHandler.Factory

  @Inject
  lateinit var screenKeyProvider: ScreenKeyProvider

  private var modelUpdateCallback: PatientSummaryModelUpdateCallback? = null

  private val screenKey by unsafeLazy {
    screenKeyProvider.keyFor<PatientSummaryScreenKey>(this)
  }

  private val internalEvents = PublishSubject.create<DrugSummaryEvent>()
  private val events by unsafeLazy {
    internalEvents
        .compose(ReportAnalyticsEvents())
        .share()
  }

  private val delegate by unsafeLazy {
    val uiRenderer = DrugSummaryUiRenderer(this)

    MobiusDelegate.forView(
        events = events.ofType(),
        defaultModel = DrugSummaryModel.create(patientUuid = screenKey.patientUuid),
        init = DrugSummaryInit(),
        update = DrugSummaryUpdate(),
        effectHandler = effectHandlerFactory.create(this).build(),
        modelUpdateListener = { model ->
          modelUpdateCallback?.invoke(model)
          uiRenderer.render(model)
        }
    )
  }

  private var prescribedDrugs by mutableStateOf<List<PrescribedDrug>>(emptyList())

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    delegate.stop()
    super.onDetachedFromWindow()
  }

  override fun onSaveInstanceState(): Parcelable {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(delegate.onRestoreInstanceState(state))
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    context.injector<DrugSummaryViewInjector>().inject(this)

    addView(ComposeView(context).apply {
      setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

      setContent {
        SimpleTheme {
          DrugSummary(
              prescribedDrugs = prescribedDrugs,
              drugDateFormatter = fullDateFormatter,
              userClock = userClock,
              onEditMedicinesClick = {
                internalEvents.onNext(PatientSummaryUpdateDrugsClicked())
              }
          )
        }
      }
    })
  }

  override fun populatePrescribedDrugs(prescribedDrugs: List<PrescribedDrug>) {
    val alphabeticallySortedPrescribedDrugs = prescribedDrugs.sortedBy { it.name }
    this.prescribedDrugs = alphabeticallySortedPrescribedDrugs
  }

  override fun showUpdatePrescribedDrugsScreen(patientUuid: UUID, currentFacility: Facility) {
    router.push(AlertFacilityChangeSheet.Key(
        currentFacilityName = currentFacility.name,
        continuation = ContinueToScreen(PrescribedDrugsScreenKey(patientUuid))
    ))
  }

  override fun registerSummaryModelUpdateCallback(callback: PatientSummaryModelUpdateCallback?) {
    modelUpdateCallback = callback
  }
}
