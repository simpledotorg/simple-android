package org.resolvetosavelives.red.newentry.search

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import kotterknife.bindView
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.TheActivity
import org.resolvetosavelives.red.newentry.personal.PatientPersonalDetailsEntryScreen
import org.resolvetosavelives.red.router.screen.ScreenRouter
import org.resolvetosavelives.red.widgets.showKeyboard
import javax.inject.Inject

class PatientSearchByMobileScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    val KEY = PatientSearchByMobileScreenKey()
  }

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: PatientSearchByMobileScreenController

  private val mobileNumberEditText by bindView<EditText>(R.id.patientsearch_mobile_number)
  private val newPatientButton by bindView<Button>(R.id.patientsearch_new_patient)
  private val patientRecyclerView by bindView<RecyclerView>(R.id.patientsearch_recyclerview)
  private val resultsAdapter = PatientSearchResultsAdapter()

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    Observable.merge(mobileNumberTextChanges(), proceedButtonClicks())
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { uiChange -> uiChange(this) }
  }

  private fun mobileNumberTextChanges() = RxTextView.textChanges(mobileNumberEditText)
      .map(CharSequence::toString)
      .map(::PatientMobileNumberTextChanged)

  private fun proceedButtonClicks() = RxView.clicks(newPatientButton)
      .map { PatientSearchByMobileProceedClicked() }

  fun showKeyboardOnMobileNumberField() {
    mobileNumberEditText.showKeyboard()
  }

  fun setupSearchResultsList() {
    patientRecyclerView.adapter = resultsAdapter
    patientRecyclerView.layoutManager = LinearLayoutManager(context)
  }

  fun updatePatientSearchResults(patients: List<Patient>) {
    resultsAdapter.updateAndNotifyChanges(patients)
  }

  fun openPersonalDetailsEntryScreen() {
    screenRouter.push(PatientPersonalDetailsEntryScreen.KEY)
  }
}
