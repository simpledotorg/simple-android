package org.simple.clinic.home.overdue.phonemask

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.bindUiToController
import org.simple.clinic.home.overdue.OverdueListItem.Patient
import org.simple.clinic.widgets.BottomSheetActivity
import org.simple.clinic.widgets.ScreenDestroyed
import javax.inject.Inject

class PhoneMaskBottomSheet : BottomSheetActivity() {

  @Inject
  lateinit var controller: PhoneMaskBottomSheetController

  private val onDestroys = PublishSubject.create<ScreenDestroyed>()

  private val normalCallButton by bindView<View>(R.id.phonemask_normal_call_button)
  private val secureCallButton by bindView<View>(R.id.phonemask_secure_call_button)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.sheet_phone_mask)
    TheActivity.component.inject(this)

    bindUiToController(
        ui = this,
        events = Observable.mergeArray(
            sheetCreates(),
            normalCallClicks(),
            secureCallClicks()
        ),
        controller = controller,
        screenDestroys = onDestroys
    )
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
      Observable.just(PhoneMaskBottomSheetCreated(patient()))

  private fun patient(): Patient =
      intent.getParcelableExtra(PATIENT_KEY)

  override fun onDestroy() {
    onDestroys.onNext(ScreenDestroyed())
    super.onDestroy()
  }

  fun requestCallPermission() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  companion object {

    private const val PATIENT_KEY = "PATIENT_KEY"

    fun intentForPhoneMaskBottomSheet(context: Context, patient: Patient): Intent =
        Intent(context, PhoneMaskBottomSheet::class.java).apply {
          putExtra(PATIENT_KEY, patient)
        }
  }
}
