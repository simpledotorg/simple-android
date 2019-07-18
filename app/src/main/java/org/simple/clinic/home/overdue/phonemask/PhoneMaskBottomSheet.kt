package org.simple.clinic.home.overdue.phonemask

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.bindUiToController
import org.simple.clinic.patient.displayLetterRes
import org.simple.clinic.router.screen.ActivityPermissionResult
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.util.RuntimePermissions
import org.simple.clinic.widgets.BottomSheetActivity
import org.simple.clinic.widgets.ScreenDestroyed
import java.util.UUID
import javax.inject.Inject

private const val REQUESTCODE_CALL_PHONE_PERMISSION = 21
private const val CALL_PHONE_PERMISSION = Manifest.permission.CALL_PHONE

class PhoneMaskBottomSheet : BottomSheetActivity() {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: PhoneMaskBottomSheetController

  private val onDestroys = PublishSubject.create<ScreenDestroyed>()
  private val permissionResults = PublishSubject.create<ActivityPermissionResult>()

  private val normalCallButton by bindView<View>(R.id.phonemask_normal_call_button)
  private val secureCallButton by bindView<View>(R.id.phonemask_secure_call_button)
  private val nameTextView by bindView<TextView>(R.id.phonemask_name)
  private val phoneNumberTextView by bindView<TextView>(R.id.phonemask_phone_number)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.sheet_phone_mask)

    TheActivity.component.inject(this)

    bindUiToController(
        ui = this,
        events = Observable.mergeArray(
            sheetCreates(),
            normalCallClicks(),
            secureCallClicks(),
            callPermissionChanges()
        ),
        controller = controller,
        screenDestroys = onDestroys
    )
  }

  @SuppressLint("SetTextI18n")
  fun setupView(patient: PatientDetails) {
    patient.apply {
      val genderLetter = resources.getString(gender.displayLetterRes)
      nameTextView.text = "$name, $genderLetter, $age"
      phoneNumberTextView.text = phoneNumber
    }
  }

  private fun normalCallClicks() =
      RxView
          .clicks(normalCallButton)
          .map { NormalCallClicked }

  private fun secureCallClicks() =
      RxView
          .clicks(secureCallButton)
          .map { SecureCallClicked }

  private fun sheetCreates() =
      Observable.just(PhoneMaskBottomSheetCreated(patientUuid()))

  private fun patientUuid(): UUID =
      intent.getSerializableExtra(PATIENT_KEY) as UUID

  override fun onDestroy() {
    onDestroys.onNext(ScreenDestroyed())
    super.onDestroy()
  }

  fun requestCallPermission() {
    RuntimePermissions.request(this, CALL_PHONE_PERMISSION, REQUESTCODE_CALL_PHONE_PERMISSION)
  }

  fun closeSheet() {
    finish()
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    permissionResults.onNext(ActivityPermissionResult(requestCode))
  }

  private fun callPermissionChanges() =
      permissionResults
          .filter { it.requestCode == REQUESTCODE_CALL_PHONE_PERMISSION }
          .map { RuntimePermissions.check(this, CALL_PHONE_PERMISSION) }
          .map(::CallPhonePermissionChanged)

  companion object {

    private const val PATIENT_KEY = "PATIENT_KEY"

    fun intentForPhoneMaskBottomSheet(context: Context, patientUuid: UUID): Intent =
        Intent(context, PhoneMaskBottomSheet::class.java).apply {
          putExtra(PATIENT_KEY, patientUuid)
        }
  }
}
