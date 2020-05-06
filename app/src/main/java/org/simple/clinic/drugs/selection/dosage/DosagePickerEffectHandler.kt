package org.simple.clinic.drugs.selection.dosage

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.protocol.ProtocolRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.scheduler.SchedulersProvider
import java.util.UUID
import javax.inject.Inject

class DosagePickerEffectHandler @Inject constructor(
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
    private val protocolRepository: ProtocolRepository,
    private val schedulers: SchedulersProvider
) {

  fun build(): ObservableTransformer<DosagePickerEffect, DosagePickerEvent> {
    return RxMobius
        .subtypeEffectHandler<DosagePickerEffect, DosagePickerEvent>()
        .addTransformer(LoadProtocolDrugsByName::class.java, loadProtocolDrugs())
        .build()
  }

  private fun loadProtocolDrugs(): ObservableTransformer<LoadProtocolDrugsByName, DosagePickerEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .map { it.drugName }
          .switchMap { drugName ->
            // This is nasty, fix in the final clean up phase by making
            // a blocking call for the protocol UUID
            currentProtocolUuid().map { drugName to it }
          }
          .switchMap { (drugName, protocolUuid) -> protocolRepository.drugsByNameOrDefault(drugName, protocolUuid) }
          .map(::DrugsLoaded)
    }
  }

  private fun currentProtocolUuid(): Observable<UUID> {
    return userSession
        .requireLoggedInUser()
        .switchMap(facilityRepository::currentFacility)
        .map { it.protocolUuid }
  }
}
