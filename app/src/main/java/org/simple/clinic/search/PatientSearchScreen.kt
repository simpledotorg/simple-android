package org.simple.clinic.search

import android.content.Context
import android.support.design.widget.TextInputLayout
import android.support.transition.ChangeBounds
import android.support.transition.Fade
import android.support.transition.TransitionManager
import android.support.transition.TransitionSet
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.newentry.DateOfBirthAndAgeVisibility
import org.simple.clinic.newentry.PatientEntryScreen
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.search.results.PatientSearchResultsScreen
import org.simple.clinic.widgets.showKeyboard
import javax.inject.Inject

class PatientSearchScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    val KEY = PatientSearchScreenKey()
  }

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var activity: TheActivity

  @Inject
  lateinit var controller: PatientSearchScreenController

  private val backButton by bindView<ImageButton>(R.id.patientsearch_back)
  private val fullNameEditText by bindView<EditText>(R.id.patientsearch_fullname)
  private val fullNameInputLayout by bindView<TextInputLayout>(R.id.patientsearch_fullname_inputlayout)
  private val ageEditText by bindView<EditText>(R.id.patientsearch_age)
  private val ageInputLayout by bindView<TextInputLayout>(R.id.patientsearch_age_inputlayout)
  private val ageEditTextContainer by bindView<ViewGroup>(R.id.patientsearch_age_container)
  private val dateOfBirthEditText by bindView<EditText>(R.id.patientsearch_dateofbirth)
  private val dateOfBirthInputLayout by bindView<TextInputLayout>(R.id.patientsearch_dateofbirth_inputlayout)
  private val dateOfBirthEditTextContainer by bindView<ViewGroup>(R.id.patientsearch_dateofbirth_container)
  private val dateOfBirthAndAgeSeparator by bindView<View>(R.id.patientsearch_dateofbirth_and_age_separator)
  private val ageOrDateOfBirthErrorTextView by bindView<TextView>(R.id.patientsearch_age_or_dateofbirth_error)
  private val searchButton by bindView<Button>(R.id.patientsearch_search)
  private val searchButtonFrame by bindView<ViewGroup>(R.id.patientsearch_search_frame)

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

    Observable
        .mergeArray(nameChanges(), dateOfBirthChanges(), ageChanges(), searchClicks())
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { uiChange -> uiChange(this) }
  }

  private fun nameChanges() =
      RxTextView
          .textChanges(fullNameEditText)
          .map(CharSequence::toString)
          .map(::SearchQueryNameChanged)

  private fun dateOfBirthChanges() =
      RxTextView
          .textChanges(dateOfBirthEditText)
          .map(CharSequence::toString)
          .map(::SearchQueryDateOfBirthChanged)

  private fun ageChanges() = RxTextView
      .textChanges(ageEditText)
      .map(CharSequence::toString)
      .map(::SearchQueryAgeChanged)

  private fun searchClicks(): Observable<SearchClicked> {
    val imeSearchClicks = RxTextView
        .editorActionEvents(dateOfBirthEditText)
        .mergeWith(RxTextView.editorActionEvents(ageEditText))
        .filter { it.actionId() == EditorInfo.IME_ACTION_SEARCH }

    return RxView
        .clicks(searchButton)
        .mergeWith(imeSearchClicks)
        .map { SearchClicked() }
  }

  fun showSearchButtonAsEnabled() {
    searchButton.setBackgroundResource(R.color.patientsearch_search_button_enabled)
    searchButtonFrame.setBackgroundResource(R.color.patientsearch_search_button_frame_enabled)
  }

  fun showSearchButtonAsDisabled() {
    searchButton.setBackgroundResource(R.color.patientsearch_search_button_disabled)
    searchButtonFrame.setBackgroundResource(R.color.patientsearch_search_button_frame_disabled)
  }

  fun openPatientEntryScreen() {
    screenRouter.push(PatientEntryScreen.KEY)
  }

  fun openPatientSearchResultsScreen(name: String, age: String, dateOfBirth: String) {
    screenRouter.push(PatientSearchResultsScreen.KEY(name, age, dateOfBirth))
  }

  fun setDateOfBirthAndAgeVisibility(visibility: DateOfBirthAndAgeVisibility) {
    val transition = TransitionSet()
        .addTransition(ChangeBounds())
        .addTransition(Fade())
        .setOrdering(TransitionSet.ORDERING_TOGETHER)
        .setDuration(250)
        .setInterpolator(FastOutSlowInInterpolator())
    TransitionManager.beginDelayedTransition(this, transition)

    dateOfBirthEditTextContainer.visibility = when (visibility) {
      DateOfBirthAndAgeVisibility.DATE_OF_BIRTH_VISIBLE, DateOfBirthAndAgeVisibility.BOTH_VISIBLE -> View.VISIBLE
      else -> View.GONE
    }

    dateOfBirthAndAgeSeparator.visibility = when (visibility) {
      DateOfBirthAndAgeVisibility.BOTH_VISIBLE -> View.VISIBLE
      else -> View.GONE
    }

    ageEditTextContainer.visibility = when (visibility) {
      DateOfBirthAndAgeVisibility.AGE_VISIBLE, DateOfBirthAndAgeVisibility.BOTH_VISIBLE -> View.VISIBLE
      else -> View.GONE
    }
  }

  fun setEmptyFullNameErrorVisible(visible: Boolean) {
    fullNameInputLayout.error = if (visible) {
      resources.getString(R.string.patientsearch_error_empty_fullname)
    } else {
      null
    }
  }

  fun setEmptyDateOfBirthAndAgeErrorVisible(visible: Boolean) {
    ageOrDateOfBirthErrorTextView.visibility = if (visible) View.VISIBLE else View.GONE
    ageOrDateOfBirthErrorTextView.apply {
      visibility = if (visible) View.VISIBLE else View.GONE
      text = if (visible) resources.getString(R.string.patientsearch_error_both_dateofbirth_and_age_empty) else null
    }
    ageInputLayout.error = if (visible) " " else null
  }

  fun setInvalidDateOfBirthErrorVisible(visible: Boolean) {
    ageOrDateOfBirthErrorTextView.visibility = if (visible) View.VISIBLE else View.GONE
    ageOrDateOfBirthErrorTextView.apply {
      visibility = if (visible) View.VISIBLE else View.GONE
      text = if (visible) resources.getString(R.string.patientsearch_error_invalid_dateofbirth) else null
    }
    dateOfBirthInputLayout.error = if (visible) " " else null
  }

  fun setDateOfBirthIsInFutureErrorVisible(visible: Boolean) {
    ageOrDateOfBirthErrorTextView.visibility = if (visible) View.VISIBLE else View.GONE
    ageOrDateOfBirthErrorTextView.apply {
      visibility = if (visible) View.VISIBLE else View.GONE
      text = if (visible) resources.getString(R.string.patientsearch_error_dateofbirth_is_in_future) else null
    }
    dateOfBirthInputLayout.error = if (visible) " " else null
  }
}
