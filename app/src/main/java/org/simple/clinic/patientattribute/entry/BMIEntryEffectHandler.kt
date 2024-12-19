package org.simple.clinic.patientattribute.entry

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.Lazy
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.patientattribute.PatientAttributeRepository
import org.simple.clinic.user.User
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.uuid.UuidGenerator

class BMIEntryEffectHandler @AssistedInject constructor(
    @Assisted private val ui: BMIEntryUi,
    private val patientAttributeRepository: PatientAttributeRepository,
    private val currentUser: Lazy<User>,
    private val uuidGenerator: UuidGenerator,
    private val schedulersProvider: SchedulersProvider,
) {

  @AssistedFactory
  interface Factory {
    fun create(ui: BMIEntryUi): BMIEntryEffectHandler
  }

  fun build(): ObservableTransformer<BMIEntryEffect, BMIEntryEvent> {
    return RxMobius
        .subtypeEffectHandler<BMIEntryEffect, BMIEntryEvent>()
        .addTransformer(CreateNewBMIEntry::class.java, createNewBMIEntry())
        .build()
  }

  private fun createNewBMIEntry(): ObservableTransformer<CreateNewBMIEntry, BMIEntryEvent> {
    return ObservableTransformer { createNewBMIEntries ->
      createNewBMIEntries
          .observeOn(schedulersProvider.io())
          .map { createNewBMIEntry ->
            patientAttributeRepository.save(
                reading = createNewBMIEntry.reading,
                patientUuid = createNewBMIEntry.patientUUID,
                loggedInUserUuid = currentUser.get().uuid,
                uuid = uuidGenerator.v4(),
            )
            BMISaved
          }
    }
  }
}
