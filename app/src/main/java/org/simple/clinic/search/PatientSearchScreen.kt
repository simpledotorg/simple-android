package org.simple.clinic.search

import android.content.Context
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RelativeLayout
import com.google.android.material.textfield.TextInputLayout
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.bindUiToController
import org.simple.clinic.newentry.PatientEntryScreenKey
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.search.results.PatientSearchResultsScreenKey
import org.simple.clinic.widgets.PrimarySolidButtonWithFrame
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.showKeyboard
import javax.inject.Inject

class PatientSearchScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var activity: TheActivity

  @Inject
  lateinit var controller: PatientSearchScreenController

  private val backButton by bindView<ImageButton>(R.id.patientsearch_back)
  private val fullNameEditText by bindView<EditText>(R.id.patientsearch_fullname)
  private val fullNameInputLayout by bindView<TextInputLayout>(R.id.patientsearch_fullname_inputlayout)
  private val searchButtonFrame by bindView<PrimarySolidButtonWithFrame>(R.id.patientsearch_search_frame)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    fullNameEditText.showKeyboard()
    backButton.setOnClickListener {
      screenRouter.pop()
    }

    bindUiToController(
        ui = this,
        events = Observable.merge(nameChanges(), searchClicks()),
        controller = controller,
        screenDestroys = RxView.detaches(this).map { ScreenDestroyed() }
    )
  }

  private fun nameChanges() =
      RxTextView
          .textChanges(fullNameEditText)
          .map(CharSequence::toString)
          .map(::SearchQueryNameChanged)

  private fun searchClicks(): Observable<SearchClicked> {
    val imeSearchClicks = RxTextView
        .editorActionEvents(fullNameEditText)
        .filter { it.actionId() == EditorInfo.IME_ACTION_SEARCH }

    return RxView
        .clicks(searchButtonFrame.button)
        .mergeWith(imeSearchClicks)
        .map { SearchClicked() }
  }

  fun showSearchButtonAsEnabled() {
    searchButtonFrame.isEnabled = true
  }

  fun showSearchButtonAsDisabled() {
    searchButtonFrame.isEnabled = false
  }

  fun openPatientEntryScreen() {
    screenRouter.push(PatientEntryScreenKey())
  }

  fun openPatientSearchResultsScreen(name: String) {
    screenRouter.push(PatientSearchResultsScreenKey(name))
  }

  fun setEmptyFullNameErrorVisible(visible: Boolean) {
    fullNameInputLayout.error = if (visible) {
      resources.getString(R.string.patientsearch_error_empty_fullname)
    } else {
      null
    }
    fullNameInputLayout.isErrorEnabled = visible
  }
}
