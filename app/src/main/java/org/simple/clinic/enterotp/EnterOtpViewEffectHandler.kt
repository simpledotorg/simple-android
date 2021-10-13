package org.simple.clinic.enterotp

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.simple.clinic.mobius.ViewEffectsHandler

class EnterOtpViewEffectHandler @AssistedInject constructor(
    private val uiActions: EnterOtpUiActions
) : ViewEffectsHandler<EnterOtpViewEffect> {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: EnterOtpUiActions): EnterOtpViewEffectHandler
  }

  override fun handle(viewEffect: EnterOtpViewEffect) {
    when (viewEffect) {
      ClearPin -> uiActions::clearPin
      GoBack -> uiActions::goBack
      ShowSmsSentMessage -> uiActions::showSmsSentMessage
      ShowNetworkError -> uiActions::showNetworkError
      ShowUnexpectedError -> uiActions::showUnexpectedError
    }
  }
}
