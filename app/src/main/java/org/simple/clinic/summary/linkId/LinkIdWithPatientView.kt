package org.simple.clinic.summary.linkId

import android.annotation.SuppressLint
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import com.jakewharton.rxbinding3.view.clicks
import com.spotify.mobius.Init
import com.spotify.mobius.Update
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.LinkIdWithPatientViewBinding
import org.simple.clinic.main.TheActivity
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.navigation.v2.fragments.BaseBottomSheet
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ProgressMaterialButton.ButtonState.Enabled
import org.simple.clinic.widgets.ProgressMaterialButton.ButtonState.InProgress
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.animateBottomSheetIn
import org.simple.clinic.widgets.animateBottomSheetOut
import javax.inject.Inject

/**
 *
 * One thing different about this bottom sheet, and the others is that
 * if this sheet is closed without linking the passport with the patient,
 * the summary sheet should also be closed.
 *
 * There was a weird bug happening where [Flow] would attempt to recreate
 * the state of the activity when it resumed before the command to pop the
 * summary screen would be invoked, which caused the link ID sheet to open
 * twice.
 *
 * There was a workaround to fix this by rewriting [Flow]'s history
 * before opening the [LinkIdWithPatientSheet] to one where the open
 * intention was [ViewExistingPatient], but it was a very wrong hack since
 * we might also need to use the original intention later.
 *
 * In addition, the screen result from [LinkIdWithBottomSheet] was being
 * delivered to [TheActivity] **BEFORE** the [PatientSummaryScreen] could
 * subscribe to the results stream, which would not allow the screen to
 * close when the [LinkIdWithBottomSheet] was closed as well. We cannot use
 * the imperative  * way of delivering results via the [ScreenRouter] because
 * that instance is scoped to [TheActivity] and is not accessible to
 * [LinkIdWithBottomSheet].
 *
 * Since this feature was a little time sensitive, instead of spending
 * time to make it work as a [BottomSheetActivity], we made the decision to
 * implement it as a child view of [PatientSummaryScreen] instead. This
 * means that [Flow] neither tries to recreate the history as well as the
 * summary screen can directly observe the results from
 * [LinkIdWithPatientView].
 *
 * A consequence of this, is that:
 * - All behaviour that comes with the [BottomSheetActivity] (animations,
 * background click handling) will have to be reimplemented here.
 * - Unlike other bottom sheets, this one will not cover the status bar.
 */
class LinkIdWithPatientView :
    BaseBottomSheet<
        LinkIdWithPatientSheetKey,
        LinkIdWithPatientViewBinding,
        LinkIdWithPatientModel,
        LinkIdWithPatientEvent,
        LinkIdWithPatientEffect
        >(),
    LinkIdWithPatientViewUi,
    LinkIdWithPatientUiActions {

  private var binding: LinkIdWithPatientViewBinding? = null

  private val backgroundView
    get() = binding!!.backgroundView

  private val addButton
    get() = binding!!.addButton

  private val cancelButton
    get() = binding!!.cancelButton

  private val idTextView
    get() = binding!!.idTextView

  private val idPatientNameTextView
    get() = binding!!.idPatientNameTextView

  private val contentContainer
    get() = binding!!.contentContainer

  @Inject
  lateinit var effectHandlerFactory: LinkIdWithPatientEffectHandler.Factory

  val downstreamUiEvents: Subject<UiEvent> = PublishSubject.create()
  private val upstreamUiEvents: Subject<UiEvent> = PublishSubject.create()

  private val events by unsafeLazy {
    Observable
        .merge(
            viewShows(),
            addClicks(),
            cancelClicks(),
            downstreamUiEvents
        )
        .compose(ReportAnalyticsEvents())
  }

  private val delegate by unsafeLazy {
    val uiRenderer = LinkIdWithPatientUiRenderer(this)

    MobiusDelegate.forView(
        events = events.ofType(),
        defaultModel = LinkIdWithPatientModel.create(),
        update = LinkIdWithPatientUpdate(),
        effectHandler = effectHandlerFactory.create(this).build(),
        modelUpdateListener = uiRenderer::render
    )
  }

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

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    delegate.stop()
    binding = null
    super.onDetachedFromWindow()
  }

  override fun onSaveInstanceState(): Parcelable {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(delegate.onRestoreInstanceState(state))
  }

  @SuppressLint("CheckResult")
  override fun onFinishInflate() {
    super.onFinishInflate()
    val layoutInflater = LayoutInflater.from(context)
    binding = LinkIdWithPatientViewBinding.inflate(layoutInflater, this)

    if (isInEditMode) {
      return
    }

    context.injector<Injector>().inject(this)

    backgroundView.setOnClickListener {
      // Intentionally done to swallow click events.
    }
  }

  private fun viewShows(): Observable<UiEvent> {
    return downstreamUiEvents
        .ofType<LinkIdWithPatientViewShown>()
        .map { it as UiEvent }
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
    upstreamUiEvents.onNext(LinkIdWithPatientLinked)
  }

  override fun closeSheetWithoutIdLinked() {
    upstreamUiEvents.onNext(LinkIdWithPatientCancelled)
  }

  override fun showAddButtonProgress() {
    addButton.setButtonState(InProgress)
  }

  override fun hideAddButtonProgress() {
    addButton.setButtonState(Enabled)
  }

  fun show(runBefore: () -> Unit) {
    animateBottomSheetIn(
        backgroundView = backgroundView,
        contentContainer = contentContainer,
        startAction = runBefore
    )
  }

  fun hide(runAfter: () -> Unit) {
    animateBottomSheetOut(
        backgroundView = backgroundView,
        contentContainer = contentContainer,
        endAction = runAfter
    )
  }

  interface Injector {
    fun inject(target: LinkIdWithPatientView)
  }
}
