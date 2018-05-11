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
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotterknife.bindView
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.TheActivity
import org.resolvetosavelives.red.newentry.personal.PatientPersonalDetailsEntryScreen
import timber.log.Timber

class PatientSearchByMobileScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    val KEY = PatientSearchByMobileScreenKey()
  }

  private val mobileNumberEditText by bindView<EditText>(R.id.patientsearch_mobile_number)
  private val newPatientButton by bindView<Button>(R.id.patientsearch_new_patient)
  private val patientRecyclerView by bindView<RecyclerView>(R.id.patientsearch_recyclerview)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    setupPatientSearchResults()

    newPatientButton.setOnClickListener({
      val ongoingEntry = OngoingPatientEntry(null, mobileNumberEditText.text.toString())
      TheActivity.patientRepository()
          .save(ongoingEntry)
          .subscribe({
            TheActivity.screenRouter().push(PatientPersonalDetailsEntryScreen.KEY)
          })
    })
  }

  private fun setupPatientSearchResults() {
    val resultsAdapter = PatientSearchResultsAdapter()

    patientRecyclerView.adapter = resultsAdapter
    patientRecyclerView.layoutManager = LinearLayoutManager(context)

    RxTextView.textChanges(mobileNumberEditText)
        .switchMap { searchQuery ->
          TheActivity.patientRepository()
              .search(searchQuery.toString())
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
        }
        .doOnNext({ patients -> Timber.i("patients: %s", patients) })
        .takeUntil(RxView.detaches(this))
        .subscribe(resultsAdapter)
  }
}
