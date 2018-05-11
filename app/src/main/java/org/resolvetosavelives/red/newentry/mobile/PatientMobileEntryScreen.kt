package org.resolvetosavelives.red.newentry.mobile

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import kotterknife.bindView
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.TheActivity
import org.resolvetosavelives.red.newentry.search.OngoingPatientEntry
import org.resolvetosavelives.red.newentry.success.PatientSavedScreen
import org.resolvetosavelives.red.widgets.showKeyboard
import java.util.UUID

class PatientMobileEntryScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    val KEY = PatientMobileEntryScreenKey()
  }

  private val primaryNumberEditText by bindView<EditText>(R.id.patientmobile_primary_number)
  private val proceedButton by bindView<Button>(R.id.patiententry_mobile_proceed)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    primaryNumberEditText.showKeyboard()

    val repository = TheActivity.patientRepository()
    repository
        .ongoingEntry()
        .subscribeOn(io())
        .observeOn(mainThread())
        .subscribe({ entry ->
          primaryNumberEditText.setText(entry.mobileNumber)
        })

    proceedButton.setOnClickListener({
      val saveOngoingEntry = repository.ongoingEntry()
          .map { entry -> entry.copy(mobileNumber = primaryNumberEditText.text.toString()) }
          .flatMapCompletable { entry: OngoingPatientEntry -> repository.save(entry) }
          .andThen(repository.markOngoingEntryAsComplete(UUID.randomUUID()))

      saveOngoingEntry
          .subscribeOn(io())
          .observeOn(mainThread())
          .subscribe({
            TheActivity.screenRouter().push(PatientSavedScreen.KEY)
          })
    })
  }
}

