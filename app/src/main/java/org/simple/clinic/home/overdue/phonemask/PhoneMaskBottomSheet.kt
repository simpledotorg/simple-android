package org.simple.clinic.home.overdue.phonemask

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.jakewharton.rxbinding2.view.RxView
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.sheet_phone_mask.*
import org.simple.clinic.ClinicApp
import org.simple.clinic.R
import org.simple.clinic.bindUiToController
import org.simple.clinic.di.InjectorProviderContextWrapper
import org.simple.clinic.home.overdue.phonemask.di.PhoneMaskBottomSheetComponent
import org.simple.clinic.patient.displayLetterRes
import org.simple.clinic.router.screen.ActivityPermissionResult
import org.simple.clinic.util.LocaleOverrideContextWrapper
import org.simple.clinic.util.RequestPermissions
import org.simple.clinic.util.RuntimePermissions
import org.simple.clinic.util.wrap
import org.simple.clinic.widgets.BottomSheetActivity
import org.simple.clinic.widgets.ScreenDestroyed
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

class PhoneMaskBottomSheet : BottomSheetActivity(), PhoneMaskBottomSheetUi {

  @Inject
  lateinit var controller: PhoneMaskBottomSheetController

  @Inject
  lateinit var locale: Locale

  @Inject
  lateinit var runtimePermissions: RuntimePermissions

  private lateinit var component: PhoneMaskBottomSheetComponent

  private val onDestroys = PublishSubject.create<ScreenDestroyed>()
  private val permissionResults = PublishSubject.create<ActivityPermissionResult>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.sheet_phone_mask)

    bindUiToController(
        ui = this,
        events = Observable
            .mergeArray(
                sheetCreates(),
                normalCallClicks(),
                secureCallClicks()
            )
            .compose(RequestPermissions(runtimePermissions, this, permissionResults)),
        controller = controller,
        screenDestroys = onDestroys
    )
  }

  override fun attachBaseContext(baseContext: Context) {
    setupDiGraph()

    val wrappedContext = baseContext
        .wrap { LocaleOverrideContextWrapper.wrap(it, locale) }
        .wrap { InjectorProviderContextWrapper.wrap(it, component) }
        .wrap { ViewPumpContextWrapper.wrap(it) }

    super.attachBaseContext(wrappedContext)
  }

  private fun setupDiGraph() {
    component = ClinicApp.appComponent
        .phoneMaskBottomSheetComponentBuilder()
        .activity(this)
        .build()

    component.inject(this)
  }

  @SuppressLint("SetTextI18n")
  override fun setupView(patient: PatientDetails) {
    patient.apply {
      val genderLetter = resources.getString(gender.displayLetterRes)
      nameTextView.text = "$name, $genderLetter, $age"
      phoneNumberTextView.text = phoneNumber
    }
  }

  private fun normalCallClicks() =
      RxView
          .clicks(normalCallButton)
          .map { NormalCallClicked() }

  private fun secureCallClicks() =
      RxView
          .clicks(secureCallButton)
          .map { SecureCallClicked() }

  private fun sheetCreates() =
      Observable.just(PhoneMaskBottomSheetCreated(patientUuid()))

  private fun patientUuid(): UUID =
      intent.getSerializableExtra(PATIENT_KEY) as UUID

  override fun onDestroy() {
    onDestroys.onNext(ScreenDestroyed())
    super.onDestroy()
  }

  override fun closeSheet() {
    finish()
  }

  override fun hideSecureCallButton() {
    secureCallButton.visibility = View.GONE
    helpTextView.visibility = View.GONE
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    permissionResults.onNext(ActivityPermissionResult(requestCode))
  }

  companion object {

    private const val PATIENT_KEY = "PATIENT_KEY"

    fun intentForPhoneMaskBottomSheet(context: Context, patientUuid: UUID): Intent =
        Intent(context, PhoneMaskBottomSheet::class.java).apply {
          putExtra(PATIENT_KEY, patientUuid)
        }
  }
}
