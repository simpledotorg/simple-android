package org.simple.clinic.summary.linkId

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.Lazy
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
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
    @Assisted private val viewEffectsConsumer: Consumer<LinkIdWithPatientViewEffect>
) {

  @AssistedFactory
  interface Factory {
    fun create(
        viewEffectsConsumer: Consumer<LinkIdWithPatientViewEffect>
    ): LinkIdWithPatientEffectHandler
  }

  fun build(): ObservableTransformer<LinkIdWithPatientEffect, LinkIdWithPatientEvent> = RxMobius
      .subtypeEffectHandler<LinkIdWithPatientEffect, LinkIdWithPatientEvent>()
      .addTransformer(AddIdentifierToPatient::class.java, addIdentifierToPatient())
      .addTransformer(GetPatientNameFromId::class.java, getPatientNameFromId())
      .addConsumer(LinkIdWithPatientViewEffect::class.java, viewEffectsConsumer::accept)
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
