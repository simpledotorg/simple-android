package org.simple.clinic.editpatient

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.simple.clinic.mobius.ViewEffectsHandler
import org.simple.clinic.util.UserClock

class EditPatientViewEffectHandler @AssistedInject constructor(
    private val userClock: UserClock,
    @Assisted private val ui: EditPatientUi
) : ViewEffectsHandler<EditPatientViewEffect> {

  @AssistedFactory
  interface Factory {
    fun create(ui: EditPatientUi): EditPatientViewEffectHandler
  }

  override fun handle(viewEffect: EditPatientViewEffect) {
    // no-op
  }
}
