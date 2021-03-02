package org.simple.clinic.summary.linkId

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.LinkIdWithPatientViewBinding
import org.simple.clinic.di.injector
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.fragments.BaseBottomSheet
import org.simple.clinic.widgets.ProgressMaterialButton.ButtonState.Enabled
import org.simple.clinic.widgets.ProgressMaterialButton.ButtonState.InProgress
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

class LinkIdWithPatientSheet :
    BaseBottomSheet<
        LinkIdWithPatientSheetKey,
        LinkIdWithPatientViewBinding,
        LinkIdWithPatientModel,
        LinkIdWithPatientEvent,
        LinkIdWithPatientEffect
        >(),
    LinkIdWithPatientViewUi,
    LinkIdWithPatientUiActions {

  private val addButton
    get() = binding.addButton

  private val cancelButton
    get() = binding.cancelButton

  private val idPatientNameTextView
    get() = binding.idPatientNameTextView

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var effectHandlerFactory: LinkIdWithPatientEffectHandler.Factory

  val downstreamUiEvents: Subject<UiEvent> = PublishSubject.create()
  private val upstreamUiEvents: Subject<UiEvent> = PublishSubject.create()

  override fun defaultModel() = LinkIdWithPatientModel.create()

  override fun bindView(inflater: LayoutInflater, container: ViewGroup?) =
      LinkIdWithPatientViewBinding.inflate(layoutInflater, container, false)

  override fun uiRenderer() = LinkIdWithPatientUiRenderer(this)

  override fun events() = Observable
      .merge(
          viewShows(),
          addClicks(),
          cancelClicks(),
          downstreamUiEvents
      )
      .compose(ReportAnalyticsEvents())
      .cast<LinkIdWithPatientEvent>()

  override fun createUpdate() = LinkIdWithPatientUpdate()

  override fun createEffectHandler() = effectHandlerFactory
      .create(this)
      .build()

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  private fun viewShows(): Observable<UiEvent> {
    return downstreamUiEvents
        .ofType<LinkIdWithPatientViewShown>()
        .map { it }
  }

  private fun addClicks(): Observable<UiEvent> {
    return addButton.clicks().map { LinkIdWithPatientAddClicked }
  }

  private fun cancelClicks(): Observable<UiEvent> {
    return cancelButton.clicks().map { LinkIdWithPatientCancelClicked }
  }

  fun uiEvents(): Observable<UiEvent> = upstreamUiEvents.hide()

  override fun renderPatientName(patientName: String) {
    idPatientNameTextView.text = resources.getString(R.string.linkidwithpatient_patient_text, patientName)
  }

  override fun closeSheetWithIdLinked() {
    router.pop()
  }

  override fun closeSheetWithoutIdLinked() {
    router.popUntilInclusive(screenKey.openedFrom)
  }

  override fun showAddButtonProgress() {
    addButton.setButtonState(InProgress)
  }

  override fun hideAddButtonProgress() {
    addButton.setButtonState(Enabled)
  }

  interface Injector {
    fun inject(target: LinkIdWithPatientSheet)
  }
}
