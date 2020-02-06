package org.simple.clinic.summary

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcelable
import android.text.style.TextAppearanceSpan
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.detaches
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.screen_patient_summary.view.*
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.bindUiToController
import org.simple.clinic.editpatient.EditPatientScreenKey
import org.simple.clinic.home.HomeScreenKey
import org.simple.clinic.main.TheActivity
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.patient.DateOfBirth
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.businessid.BusinessId
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.displayLetterRes
import org.simple.clinic.platform.crash.CrashReporter
import org.simple.clinic.router.screen.ActivityResult
import org.simple.clinic.router.screen.BackPressInterceptCallback
import org.simple.clinic.router.screen.BackPressInterceptor
import org.simple.clinic.router.screen.RouterDirection.BACKWARD
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.scheduleappointment.ScheduleAppointmentSheet
import org.simple.clinic.summary.addphone.AddPhoneNumberDialog
import org.simple.clinic.summary.linkId.LinkIdWithPatientCancelled
import org.simple.clinic.summary.linkId.LinkIdWithPatientLinked
import org.simple.clinic.summary.linkId.LinkIdWithPatientViewShown
import org.simple.clinic.summary.updatephone.UpdatePhoneNumberDialog
import org.simple.clinic.util.Truss
import org.simple.clinic.util.Unicode
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.identifierdisplay.IdentifierDisplayAdapter
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.widgets.visibleOrGone
import java.util.UUID
import javax.inject.Inject

class PatientSummaryScreen(
    context: Context,
    attrs: AttributeSet
) : RelativeLayout(context, attrs), PatientSummaryScreenUi {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controllerFactory: PatientSummaryScreenController.Factory

  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var userClock: UserClock

  @Inject
  lateinit var identifierDisplayAdapter: IdentifierDisplayAdapter

  @Inject
  lateinit var crashReporter: CrashReporter

  @Inject
  lateinit var effectHandlerFactory: PatientSummaryEffectHandler.Factory

  private var linkIdWithPatientShown: Boolean = false

  private val events: Observable<UiEvent> by unsafeLazy {
    Observable
        .mergeArray(
            screenCreates(),
            backClicks(),
            doneClicks(),
            bloodPressureSaves(),
            appointmentScheduleSheetClosed(),
            identifierLinkedEvents(),
            identifierLinkCancelledEvents()
        )
        .compose(ReportAnalyticsEvents())
        .share()
  }

  private val viewRenderer = PatientSummaryViewRenderer(this)

  private val screenKey: PatientSummaryScreenKey by unsafeLazy {
    screenRouter.key<PatientSummaryScreenKey>(this)
  }

  private val mobiusDelegate: MobiusDelegate<PatientSummaryModel, PatientSummaryEvent, PatientSummaryEffect> by unsafeLazy {
    MobiusDelegate(
        events = events.ofType(),
        defaultModel = PatientSummaryModel.from(screenKey.patientUuid),
        init = PatientSummaryInit(),
        update = PatientSummaryUpdate(),
        effectHandler = effectHandlerFactory.create(this).build(),
        modelUpdateListener = viewRenderer::render,
        crashReporter = crashReporter
    )
  }

  override fun onSaveInstanceState(): Parcelable {
    val screenSavedState = PatientSummaryScreenSavedState(
        super.onSaveInstanceState(),
        linkIdWithPatientShown = linkIdWithPatientShown
    )
    return mobiusDelegate.onSaveInstanceState(screenSavedState)
  }

  override fun onRestoreInstanceState(state: Parcelable) {
    val savedState = mobiusDelegate.onRestoreInstanceState(state) as PatientSummaryScreenSavedState
    linkIdWithPatientShown = savedState.linkIdWithPatientShown

    super.onRestoreInstanceState(savedState.superSavedState)
  }

  @SuppressLint("CheckResult")
  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    // Not sure why but the keyboard stays visible when coming from search.
    rootLayout.hideKeyboard()

    setupEditButtonClicks()

    val controller = controllerFactory.create(screenKey.patientUuid, screenKey.intention, screenKey.screenCreatedTimestamp)

    bindUiToController(
        ui = this,
        events = events,
        controller = controller,
        screenDestroys = this.detaches().map { ScreenDestroyed() }
    )
    mobiusDelegate.prepare()
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    mobiusDelegate.start()
  }

  override fun onDetachedFromWindow() {
    mobiusDelegate.stop()
    super.onDetachedFromWindow()
  }

  private fun setupEditButtonClicks() {
    editPatientButton.setOnClickListener {
      screenRouter.push(createEditPatientScreenKey(mobiusDelegate.model.patientSummaryProfile!!))
    }
  }

  private fun createEditPatientScreenKey(
      patientSummaryProfile: PatientSummaryProfile
  ): EditPatientScreenKey {
    return EditPatientScreenKey.fromPatientData(
        patientSummaryProfile.patient,
        patientSummaryProfile.address,
        patientSummaryProfile.phoneNumber
    )
  }

  private fun screenCreates(): Observable<UiEvent> {
    return Observable.just(ScreenCreated())
  }

  private fun doneClicks() = doneButtonFrame.button.clicks().map { PatientSummaryDoneClicked(screenKey.patientUuid) }

  private fun backClicks(): Observable<UiEvent> {
    val hardwareBackKeyClicks = Observable.create<Unit> { emitter ->
      val interceptor = object : BackPressInterceptor {
        override fun onInterceptBackPress(callback: BackPressInterceptCallback) {
          emitter.onNext(Unit)
          callback.markBackPressIntercepted()
        }
      }
      emitter.setCancellable { screenRouter.unregisterBackPressInterceptor(interceptor) }
      screenRouter.registerBackPressInterceptor(interceptor)
    }

    return backButton.clicks()
        .mergeWith(hardwareBackKeyClicks)
        .map { PatientSummaryBackClicked(screenKey.patientUuid, screenKey.screenCreatedTimestamp) }
  }

  private fun bloodPressureSaves(): Observable<PatientSummaryBloodPressureSaved> {
    return Observable.create { emitter ->
      bloodPressureSummaryView.bpRecorded = { emitter.onNext(PatientSummaryBloodPressureSaved) }

      emitter.setCancellable { bloodPressureSummaryView.bpRecorded = null }
    }
  }

  private fun appointmentScheduleSheetClosed() = screenRouter.streamScreenResults()
      .ofType<ActivityResult>()
      .filter { it.requestCode == SUMMARY_REQCODE_SCHEDULE_APPOINTMENT && it.succeeded() }
      .map { ScheduleAppointmentSheetClosed() }

  private fun identifierLinkedEvents(): Observable<UiEvent> {
    return linkIdWithPatientView
        .uiEvents()
        .ofType<LinkIdWithPatientLinked>()
        .map { PatientSummaryLinkIdCompleted }
  }

  private fun identifierLinkCancelledEvents(): Observable<UiEvent> {
    return linkIdWithPatientView
        .uiEvents()
        .ofType<LinkIdWithPatientCancelled>()
        .map { PatientSummaryLinkIdCancelled }
  }

  @SuppressLint("SetTextI18n")
  override fun populatePatientProfile(patientSummaryProfile: PatientSummaryProfile) {
    val patient = patientSummaryProfile.patient
    val ageValue = DateOfBirth.fromPatient(patient, userClock).estimateAge(userClock)

    displayNameGenderAge(patient.fullName, patient.gender, ageValue)
    displayPhoneNumber(patientSummaryProfile.phoneNumber)
    displayPatientAddress(patientSummaryProfile.address)
    displayBpPassport(patientSummaryProfile.bpPassport)
    displayBangladeshNationalId(patientSummaryProfile.bangladeshNationalId, patientSummaryProfile.bpPassport != null)
  }

  private fun displayPatientAddress(address: PatientAddress) {
    addressTextView.text = when {
      address.colonyOrVillage.isNullOrBlank() -> resources.getString(R.string.patientsummary_address_without_colony, address.district, address.state)
      else -> resources.getString(R.string.patientsummary_address_with_colony, address.colonyOrVillage, address.district, address.state)
    }
  }

  private fun displayPhoneNumber(phoneNumber: PatientPhoneNumber?) {
    if (phoneNumber == null) {
      contactTextView.visibility = GONE

    } else {
      contactTextView.text = phoneNumber.number
      contactTextView.visibility = VISIBLE
    }
  }

  private fun displayNameGenderAge(name: String, gender: Gender, age: Int) {
    val genderLetter = resources.getString(gender.displayLetterRes)
    fullNameTextView.text = resources.getString(R.string.patientsummary_toolbar_title, name, genderLetter, age.toString())
  }

  private fun displayBpPassport(bpPassport: BusinessId?) {
    bpPassportTextView.visibleOrGone(bpPassport != null)

    bpPassportTextView.text = when (bpPassport) {
      null -> ""
      else -> {
        val identifier = bpPassport.identifier
        val numericSpan = TextAppearanceSpan(context, R.style.Clinic_V2_TextAppearance_Body2Left_Numeric_White72)
        Truss()
            .append(identifierDisplayAdapter.typeAsText(identifier))
            .append(": ")
            .pushSpan(numericSpan)
            .append(identifierDisplayAdapter.valueAsText(identifier))
            .popSpan()
            .build()
      }
    }
  }

  private fun displayBangladeshNationalId(bangladeshNationalId: BusinessId?, isBpPassportVisible: Boolean) {
    bangladeshNationalIdTextView.visibleOrGone(bangladeshNationalId != null)

    bangladeshNationalIdTextView.text = when (bangladeshNationalId) {
      null -> ""
      else -> {
        val identifier = bangladeshNationalId.identifier
        val numericSpan = TextAppearanceSpan(context, R.style.Clinic_V2_TextAppearance_Body2Left_Numeric_White72)

        val formattedIdentifier = Truss()
            .append(context.getString(R.string.patientsummary_bangladesh_national_id))
            .append(": ")
            .pushSpan(numericSpan)
            .append(identifierDisplayAdapter.valueAsText(identifier))
            .popSpan()
            .build()

        if (isBpPassportVisible) "${Unicode.bullet} $formattedIdentifier" else formattedIdentifier
      }
    }
  }

  override fun showScheduleAppointmentSheet(patientUuid: UUID) {
    val intent = ScheduleAppointmentSheet.intent(context, patientUuid)
    activity.startActivityForResult(intent, SUMMARY_REQCODE_SCHEDULE_APPOINTMENT)
  }

  override fun goToPreviousScreen() {
    screenRouter.pop()
  }

  override fun goToHomeScreen() {
    screenRouter.clearHistoryAndPush(HomeScreenKey(), direction = BACKWARD)
  }

  override fun showUpdatePhoneDialog(patientUuid: UUID) {
    UpdatePhoneNumberDialog.show(patientUuid, activity.supportFragmentManager)
  }

  override fun showAddPhoneDialog(patientUuid: UUID) {
    AddPhoneNumberDialog.show(patientUuid, activity.supportFragmentManager)
  }

  override fun showLinkIdWithPatientView(patientUuid: UUID, identifier: Identifier) {
    if (!linkIdWithPatientShown) {
      linkIdWithPatientShown = true
      linkIdWithPatientView.downstreamUiEvents.onNext(LinkIdWithPatientViewShown(patientUuid, identifier))
      linkIdWithPatientView.show { linkIdWithPatientView.visibility = View.VISIBLE }
    }
  }

  override fun hideLinkIdWithPatientView() {
    linkIdWithPatientView.hide { linkIdWithPatientView.visibility = View.GONE }
  }

  override fun showEditButton() {
    editPatientButton.visibility = View.VISIBLE
  }

  override fun showDiabetesView() {
    bloodSugarSummaryView.visibility = VISIBLE
  }

  override fun hideDiabetesView() {
    bloodSugarSummaryView.visibility = GONE
  }
}

@Parcelize
data class PatientSummaryScreenSavedState(
    val superSavedState: Parcelable?,
    val linkIdWithPatientShown: Boolean
) : Parcelable
