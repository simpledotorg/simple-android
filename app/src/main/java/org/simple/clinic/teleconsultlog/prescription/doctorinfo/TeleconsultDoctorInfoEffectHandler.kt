package org.simple.clinic.teleconsultlog.prescription.doctorinfo

import com.f2prateek.rx.preferences2.Preference
import com.spotify.mobius.rx2.RxMobius
import dagger.Lazy
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.MedicalRegistrationId
import org.simple.clinic.signature.SignatureRepository
import org.simple.clinic.user.User
import org.simple.clinic.util.Optional
import org.simple.clinic.util.extractIfPresent
import org.simple.clinic.util.scheduler.SchedulersProvider

class TeleconsultDoctorInfoEffectHandler @AssistedInject constructor(
    @TypedPreference(MedicalRegistrationId) private val medicalRegistrationIdPreference: Preference<Optional<String>>,
    private val signatureRepository: SignatureRepository,
    private val currentUser: Lazy<User>,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: TeleconsultDoctorInfoUiActions
) {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: TeleconsultDoctorInfoUiActions): TeleconsultDoctorInfoEffectHandler
  }

  fun build(): ObservableTransformer<TeleconsultDoctorInfoEffect, TeleconsultDoctorInfoEvent> {
    return RxMobius
        .subtypeEffectHandler<TeleconsultDoctorInfoEffect, TeleconsultDoctorInfoEvent>()
        .addTransformer(LoadMedicalRegistrationId::class.java, loadMedicalRegistrationId())
        .addConsumer(SetMedicalRegistrationId::class.java, { uiActions.setMedicalRegistrationId(it.medicalRegistrationId) }, schedulersProvider.ui())
        .addTransformer(LoadSignatureBitmap::class.java, loadSignatureBitmap())
        .addConsumer(SetSignatureBitmap::class.java, { uiActions.setSignatureBitmap(it.bitmap) }, schedulersProvider.ui())
        .addTransformer(LoadCurrentUser::class.java, loadCurrentUser())
        .addAction(ShowAddSignatureDialog::class.java, uiActions::showAddSignatureDialog, schedulersProvider.ui())
        .addAction(ShowAddSignatureButton::class.java, uiActions::showAddSignatureButton, schedulersProvider.ui())
        .build()
  }

  private fun loadCurrentUser(): ObservableTransformer<LoadCurrentUser, TeleconsultDoctorInfoEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { currentUser.get() }
          .map(::CurrentUserLoaded)
    }
  }

  private fun loadSignatureBitmap(): ObservableTransformer<LoadSignatureBitmap, TeleconsultDoctorInfoEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map {
            val signature = signatureRepository.getSignatureBitmap()
            SignatureBitmapLoaded(signature)
          }
    }
  }

  private fun loadMedicalRegistrationId(): ObservableTransformer<LoadMedicalRegistrationId, TeleconsultDoctorInfoEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { medicalRegistrationIdPreference.get() }
          .extractIfPresent()
          .map(::MedicalRegistrationIdLoaded)
    }
  }
}
