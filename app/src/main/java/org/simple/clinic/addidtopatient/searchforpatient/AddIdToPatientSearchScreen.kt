package org.simple.clinic.addidtopatient.searchforpatient

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RelativeLayout
import androidx.appcompat.widget.Toolbar
import com.google.android.material.textfield.TextInputLayout
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.search.results.PatientSearchResultsScreenKey
import org.simple.clinic.widgets.PrimarySolidButtonWithFrame
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.showKeyboard
import javax.inject.Inject

class AddIdToPatientSearchScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var activity: TheActivity

  @Inject
  lateinit var controller: AddIdToPatientSearchScreenController

  private val toolBar by bindView<Toolbar>(R.id.addidtopatientsearch_toolbar)
  private val fullNameEditText by bindView<EditText>(R.id.addidtopatientsearch_fullname)
  private val fullNameInputLayout by bindView<TextInputLayout>(R.id.addidtopatientsearch_fullname_inputlayout)
  private val searchButtonFrame by bindView<PrimarySolidButtonWithFrame>(R.id.addidtopatientsearch_search_frame)

  @SuppressLint("CheckResult")
  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    fullNameEditText.showKeyboard()
    toolBar.setOnClickListener {
      screenRouter.pop()
    }

    val screenDestroys = RxView.detaches(this).map { ScreenDestroyed() }

    Observable
        .mergeArray(
            screenDestroys,
            nameChanges(),
            searchClicks()
        )
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(screenDestroys)
        .subscribe { uiChange -> uiChange(this) }
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
        .map { SearchClicked }
  }

  fun showSearchButtonAsEnabled() {
    searchButtonFrame.isEnabled = true
  }

  fun showSearchButtonAsDisabled() {
    searchButtonFrame.isEnabled = false
  }

  fun openAddIdToPatientSearchResultsScreen(name: String) {
    screenRouter.push(PatientSearchResultsScreenKey(name))
  }

  fun setEmptyFullNameErrorVisible(visible: Boolean) {
    fullNameInputLayout.error = if (visible) {
      resources.getString(R.string.addidtopatientsearch_error_empty_fullname)
    } else {
      null
    }
    fullNameInputLayout.isErrorEnabled = visible
  }
}
