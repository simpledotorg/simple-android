package org.resolvetosavelives.red.newentry.drugs

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotterknife.bindView
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.TheActivity
import org.resolvetosavelives.red.newentry.search.PatientRepository
import org.resolvetosavelives.red.newentry.success.PatientSavedScreen
import org.resolvetosavelives.red.router.screen.ScreenRouter
import javax.inject.Inject

class PatientCurrentDrugsEntryScreen(context: Context?, attrs: AttributeSet?) : RelativeLayout(context, attrs) {

  companion object {
    val KEY = PatientCurrentDrugsEntryScreenKey()
  }

  private val patientNameTextView by bindView<TextView>(R.id.patiententry_drugs_patient_fullname)
  private val proceedButton by bindView<Button>(R.id.patiententry_drugs_proceed)

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var patientRepository: PatientRepository

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    patientRepository
        .ongoingEntry()
        .map { entry ->
          when {
            entry.personalDetails == null -> "(no name)"
            else -> entry.personalDetails.fullName
          }
        }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({ patientFullName -> patientNameTextView.text = patientFullName })

    proceedButton.setOnClickListener({
      screenRouter.push(PatientSavedScreen.KEY)
    })
  }
}
