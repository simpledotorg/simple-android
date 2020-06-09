package org.simple.clinic.registration.name

import android.animation.LayoutTransition
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.RelativeLayout
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import kotlinx.android.synthetic.main.screen_registration_name.view.*
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.bindUiToController
import org.simple.clinic.main.TheActivity
import org.simple.clinic.registration.pin.RegistrationPinScreenKey
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.setTextAndCursor
import javax.inject.Inject

class RegistrationFullNameScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs), RegistrationNameUi {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: RegistrationFullNameScreenController

  private val events by unsafeLazy {
    Observable
        .merge(
            screenCreates(),
            nameTextChanges(),
            doneClicks()
        )
        .compose(ReportAnalyticsEvents())
        .share()
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    backButton.setOnClickListener {
      screenRouter.pop()
    }

    bindUiToController(
        ui = this,
        events = events,
        controller = controller,
        screenDestroys = RxView.detaches(this).map { ScreenDestroyed() }
    )

    cardViewContentLayout.layoutTransition.setDuration(200)
    cardViewContentLayout.layoutTransition.setStagger(LayoutTransition.CHANGE_APPEARING, 0)
    cardViewContentLayout.layoutTransition.setStagger(LayoutTransition.CHANGE_DISAPPEARING, 0)
    cardViewContentLayout.layoutTransition.setStagger(LayoutTransition.CHANGING, 0)

    post { fullNameEditText.requestFocus() }
  }

  private fun screenCreates() = Observable.just(RegistrationFullNameScreenCreated())

  private fun nameTextChanges() =
      RxTextView.textChanges(fullNameEditText)
          .map(CharSequence::toString)
          .map { it.trim() }
          .map(::RegistrationFullNameTextChanged)

  private fun doneClicks() =
      RxTextView
          .editorActions(fullNameEditText) { it == EditorInfo.IME_ACTION_DONE }
          .map { RegistrationFullNameDoneClicked() }

  override fun preFillUserDetails(ongoingEntry: OngoingRegistrationEntry) {
    fullNameEditText.setTextAndCursor(ongoingEntry.fullName)
  }

  override fun showEmptyNameValidationError() {
    validationErrorTextView.visibility = View.VISIBLE
    validationErrorTextView.text = resources.getString(R.string.registrationname_error_empty_name)
  }

  override fun hideValidationError() {
    validationErrorTextView.visibility = View.GONE
  }

  override fun openRegistrationPinEntryScreen() {
    screenRouter.push(RegistrationPinScreenKey())
  }
}
