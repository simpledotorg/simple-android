package org.simple.clinic.summary.linkId

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import dagger.Lazy
import io.reactivex.ObservableTransformer
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.User
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.uuid.UuidGenerator

class LinkIdWithPatientEffectHandler @AssistedInject constructor(
    private val currentUser: Lazy<User>,
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
      .addAction(CloseSheetWithOutIdLinked::class.java, uiActions::closeSheetWithoutIdLinked, schedulersProvider.ui())
      .addAction(CloseSheetWithLinkedId::class.java, uiActions::closeSheetWithIdLinked, schedulersProvider.ui())
      .addTransformer(AddIdentifierToPatient::class.java, addIdentifierToPatient())
      .addTransformer(GetPatientNameFromId::class.java, getPatientNameFromId())
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
                    assigningUser = currentUser.get()
                )
          }
          .map { IdentifierAddedToPatient }
    }
  }

  private fun getPatientNameFromId(): ObservableTransformer<GetPatientNameFromId, LinkIdWithPatientEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { patientRepository.patientImmediate(it.patientUuid)!!.fullName }
          .map(::PatientNameReceived)
    }

  }
}
