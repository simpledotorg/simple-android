package org.simple.clinic.summary.linkId

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.bindUiToController
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.text.style.TextAppearanceWithLetterSpacingSpan
import org.simple.clinic.util.Truss
import org.simple.clinic.util.identifierdisplay.IdentifierDisplayAdapter
import org.simple.clinic.widgets.ScreenDestroyed
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
class LinkIdWithPatientView(
    context: Context,
    attributeSet: AttributeSet
) : FrameLayout(context, attributeSet) {

  @Inject
  lateinit var controller: LinkIdWithPatientViewController

  @Inject
  lateinit var identifierDisplayAdapter: IdentifierDisplayAdapter

  val downstreamUiEvents: Subject<UiEvent> = PublishSubject.create()
  private val upstreamUiEvents: Subject<UiEvent> = PublishSubject.create()

  private val idTextView by bindView<TextView>(R.id.linkidwithpatient_text)
  private val addButton by bindView<Button>(R.id.linkidwithpatient_button_add)
  private val cancelButton by bindView<Button>(R.id.linkidwithpatient_button_cancel)
  private val backgroundView by bindView<View>(R.id.linkidwithpatient_background)
  private val contentContainer by bindView<View>(R.id.linkidwithpatient_content)

  @SuppressLint("CheckResult")
  override fun onFinishInflate() {
    super.onFinishInflate()
    View.inflate(context, R.layout.link_id_with_patient_view, this)
    if (isInEditMode) {
      return
    }

    TheActivity.component.inject(this)

    backgroundView.setOnClickListener {
      // Intentionally done to swallow click events.
    }

    bindUiToController(
        ui = this,
        events = Observable.merge(
            viewShows(),
            addClicks(),
            cancelClicks(),
            downstreamUiEvents
        ),
        controller = controller,
        screenDestroys = RxView.detaches(this).map { ScreenDestroyed() }
    )
  }

  private fun viewShows(): Observable<UiEvent> {
    return downstreamUiEvents
        .ofType<LinkIdWithPatientViewShown>()
        .map { it as UiEvent }
  }

  private fun addClicks(): Observable<UiEvent> {
    return RxView.clicks(addButton).map { LinkIdWithPatientAddClicked }
  }

  private fun cancelClicks(): Observable<UiEvent> {
    return RxView.clicks(cancelButton).map { LinkIdWithPatientCancelClicked }
  }

  fun uiEvents(): Observable<UiEvent> = upstreamUiEvents.hide()

  fun renderIdentifierText(identifier: Identifier) {
    val identifierType = identifierDisplayAdapter.typeAsText(identifier)
    val identifierValue = identifierDisplayAdapter.valueAsText(identifier)

    val identifierTextAppearanceSpan = TextAppearanceWithLetterSpacingSpan(context, R.style.Clinic_V2_TextAppearance_Body0Left_NumericBold_Grey0)

    idTextView.text = Truss()
        .append(resources.getString(R.string.linkidwithpatient_add_id_text, identifierType))
        .pushSpan(identifierTextAppearanceSpan)
        .append(identifierValue)
        .popSpan()
        .append(resources.getString(R.string.linkidwithpatient_to_patient_text))
        .build()
  }

  fun closeSheetWithIdLinked() {
    upstreamUiEvents.onNext(LinkIdWithPatientLinked)
  }

  fun closeSheetWithoutIdLinked() {
    upstreamUiEvents.onNext(LinkIdWithPatientCancelled)
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
}
