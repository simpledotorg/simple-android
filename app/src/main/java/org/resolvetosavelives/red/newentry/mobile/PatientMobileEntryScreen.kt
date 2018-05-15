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
import org.resolvetosavelives.red.newentry.bp.PatientBpEntryScreen
import org.resolvetosavelives.red.newentry.search.OngoingPatientEntry
import org.resolvetosavelives.red.newentry.search.PatientRepository
import org.resolvetosavelives.red.router.screen.ScreenRouter
import org.resolvetosavelives.red.widgets.showKeyboard
import java.util.UUID
import javax.inject.Inject

class PatientMobileEntryScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    val KEY = PatientMobileEntryScreenKey()
  }

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var patientRepository: PatientRepository

  private val primaryNumberEditText by bindView<EditText>(R.id.patientmobile_primary_number)
  private val proceedButton by bindView<Button>(R.id.patiententry_mobile_proceed)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    primaryNumberEditText.showKeyboard()

    patientRepository
        .ongoingEntry()
        .subscribeOn(io())
        .observeOn(mainThread())
        .subscribe({ entry ->
          primaryNumberEditText.setText(entry.mobileNumber)
        })

    proceedButton.setOnClickListener({
      val saveOngoingEntry = patientRepository.ongoingEntry()
          .map { entry -> entry.copy(mobileNumber = primaryNumberEditText.text.toString()) }
          .flatMapCompletable { entry: OngoingPatientEntry -> patientRepository.save(entry) }
          .andThen(patientRepository.markOngoingEntryAsComplete(UUID.randomUUID()))

      saveOngoingEntry
          .subscribeOn(io())
          .observeOn(mainThread())
          .subscribe({
            screenRouter.push(PatientBpEntryScreen.KEY)
          })
    })
  }
}

