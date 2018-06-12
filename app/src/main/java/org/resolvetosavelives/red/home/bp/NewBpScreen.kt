package org.resolvetosavelives.red.home.bp

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import kotterknife.bindView
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.TheActivity
import org.resolvetosavelives.red.newentry.PatientEntryScreen
import org.resolvetosavelives.red.patient.OngoingPatientEntry
import org.resolvetosavelives.red.patient.PatientRepository
import org.resolvetosavelives.red.router.screen.ScreenRouter
import org.resolvetosavelives.red.search.PatientSearchScreen
import javax.inject.Inject

open class NewBpScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    val KEY = NewBpScreenKey()
  }

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: NewBpScreenController

  @Inject
  lateinit var patientRepository: PatientRepository

  private val phoneButton by bindView<View>(R.id.newbp_search_by_phone)
  private val nameButton by bindView<View>(R.id.newbp_search_by_name)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    RxView.clicks(phoneButton)
        .map { NewPatientClicked() }
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { uiChange -> uiChange(this) }

    // TODO: This is a temporary placeholder because there is no other
    // TODO: entry-point for creating a new patient.
    nameButton.setOnClickListener {
      patientRepository.saveOngoingEntry(OngoingPatientEntry(phoneNumber = OngoingPatientEntry.PhoneNumber("1234567890")))
          .subscribeOn(io())
          .observeOn(mainThread())
          .subscribe { screenRouter.push(PatientEntryScreen.KEY) }
    }
  }

  fun openNewPatientScreen() {
    screenRouter.push(PatientSearchScreen.KEY)
  }
}
