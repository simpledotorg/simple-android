package org.simple.clinic.summary.linkId

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.uuid.UuidGenerator

class LinkIdWithPatientEffectHandler @AssistedInject constructor(
    private val userSession: UserSession,
    private val patientRepository: PatientRepository,
    private val uuidGenerator: UuidGenerator,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: LinkIdWithPatientUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: LinkIdWithPatientUiActions): LinkIdWithPatientEffectHandler
  }

  fun build(): ObservableTransformer<LinkIdWithPatientEffect, LinkIdWithPatientEvent> = RxMobius
      .subtypeEffectHandler<LinkIdWithPatientEffect, LinkIdWithPatientEvent>()
      .addConsumer(RenderIdentifierText::class.java, { uiActions.renderIdentifierText(it.identifier) }, schedulersProvider.ui())
      .addAction(CloseSheetWithOutIdLinked::class.java, uiActions::closeSheetWithoutIdLinked, schedulersProvider.ui())
      .addAction(CloseSheetWithLinkedId::class.java, uiActions::closeSheetWithIdLinked, schedulersProvider.ui())
      .addTransformer(LoadCurrentUser::class.java, loadCurrentUser())
      .addTransformer(AddIdentifierToPatient::class.java, addIdentifierToPatient())
      .build()

  private fun addIdentifierToPatient(): ObservableTransformer<AddIdentifierToPatient, LinkIdWithPatientEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .switchMapSingle {
            patientRepository
                .addIdentifierToPatient(
                    uuid = uuidGenerator.v4(),
                    patientUuid = it.patientUuid,
                    identifier = it.identifier,
                    assigningUser = it.user
                )
          }
          .map { IdentifierAddedToPatient }
    }
  }

  private fun loadCurrentUser(): ObservableTransformer<LoadCurrentUser, LinkIdWithPatientEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .switchMap { userSession.requireLoggedInUser() }
          .map(::CurrentUserLoaded)
    }
  }
}
