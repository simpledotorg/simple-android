package org.simple.clinic.registration.name

import android.animation.LayoutTransition
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import com.jakewharton.rxbinding3.widget.editorActions
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ScreenRegistrationNameBinding
import org.simple.clinic.di.injector
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.registration.pin.RegistrationPinScreenKey
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.widgets.setTextAndCursor
import javax.inject.Inject

class RegistrationFullNameScreen :
    BaseScreen<
        RegistrationNameScreenKey,
        ScreenRegistrationNameBinding,
        RegistrationNameModel,
        RegistrationNameEvent,
        RegistrationNameEffect,
        RegistrationNameUiRenderer>(),
    RegistrationNameUi,
    RegistrationNameUiActions {

  private val backButton
    get() = binding.backButton

  private val cardViewContentLayout
    get() = binding.cardViewContentLayout

  private val fullNameEditText
    get() = binding.fullNameEditText

  private val validationErrorTextView
    get() = binding.validationErrorTextView

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var effectHandlerFactory: RegistrationNameEffectHandler.Factory

  override fun defaultModel() = RegistrationNameModel.create(screenKey.registrationEntry)

  override fun uiRenderer() = RegistrationNameUiRenderer(this)

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) =
      ScreenRegistrationNameBinding.inflate(layoutInflater, container, false)

  override fun events() = Observable
      .merge(
          nameTextChanges(),
          doneClicks()
      )
      .compose(ReportAnalyticsEvents())
      .cast<RegistrationNameEvent>()

  override fun createUpdate() = RegistrationNameUpdate()

  override fun createInit() = RegistrationNameInit()

  override fun createEffectHandler() = effectHandlerFactory.create(this).build()

  private val events by unsafeLazy {
    Observable
        .merge(
            nameTextChanges(),
            doneClicks()
        )
        .compose(ReportAnalyticsEvents())
        .share()
  }

  private val delegate: MobiusDelegate<RegistrationNameModel, RegistrationNameEvent, RegistrationNameEffect> by unsafeLazy {


    MobiusDelegate.forView(
        events = events.ofType(),
        defaultModel = RegistrationNameModel.create(screenKey.registrationEntry),
        update = RegistrationNameUpdate(),
        effectHandler = effectHandlerFactory.create(this).build(),
        init = RegistrationNameInit(),
        modelUpdateListener = uiRenderer::render
    )
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    backButton.setOnClickListener {
      router.pop()
    }

    cardViewContentLayout.layoutTransition.setDuration(200)
    cardViewContentLayout.layoutTransition.setStagger(LayoutTransition.CHANGE_APPEARING, 0)
    cardViewContentLayout.layoutTransition.setStagger(LayoutTransition.CHANGE_DISAPPEARING, 0)
    cardViewContentLayout.layoutTransition.setStagger(LayoutTransition.CHANGING, 0)

    view.post { fullNameEditText.requestFocus() }
  }

  private fun nameTextChanges() =
      fullNameEditText
          .textChanges()
          .map(CharSequence::toString)
          .map { it.trim() }
          .map(::RegistrationFullNameTextChanged)

  private fun doneClicks() =
      fullNameEditText
          .editorActions() { it == EditorInfo.IME_ACTION_DONE }
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

  override fun openRegistrationPinEntryScreen(registrationEntry: OngoingRegistrationEntry) {
    router.push(RegistrationPinScreenKey(registrationEntry))
  }

  interface Injector {
    fun inject(target: RegistrationFullNameScreen)
  }
}
