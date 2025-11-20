package org.simple.clinic.medicalhistory.newentry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.spotify.mobius.rx2.RxMobius
import org.simple.clinic.appconfig.Country
import org.simple.clinic.feature.Feature
import org.simple.clinic.feature.Features
import org.simple.clinic.mobius.MobiusViewModel

class NewMedicalHistoryViewModel(
    country: Country,
    features: Features,
    savedStateHandle: SavedStateHandle,
    effectHandlerFactory: NewMedicalHistoryEffectHandler.Factory
) : MobiusViewModel<NewMedicalHistoryModel, NewMedicalHistoryEvent, NewMedicalHistoryEffect, NewMedicalHistoryViewEffect>(
    modelKey = NewMedicalHistoryModel::class.java.name,
    savedStateHandle = savedStateHandle,
    defaultModel = NewMedicalHistoryModel.default(
        country = country,
        showIsSmokingQuestion = features.isEnabled(Feature.NonLabBasedStatinNudge) ||
            features.isEnabled(Feature.LabBasedStatinNudge),
        showSmokelessTobaccoQuestion = country.isoCountryCode != Country.ETHIOPIA,
        isScreeningFeatureEnabled = features.isEnabled(Feature.Screening),
    ),
    init = NewMedicalHistoryInit(),
    loopFactoryProvider = { viewEffectsConsumer ->
      RxMobius.loop(
          NewMedicalHistoryUpdate(),
          effectHandlerFactory.create(viewEffectsConsumer).build()
      )
    }
) {

  companion object {
    fun factory(
        country: Country,
        features: Features,
        effectHandlerFactory: NewMedicalHistoryEffectHandler.Factory
    ) = viewModelFactory {
      initializer {
        NewMedicalHistoryViewModel(
            country = country,
            features = features,
            savedStateHandle = createSavedStateHandle(),
            effectHandlerFactory = effectHandlerFactory
        )
      }
    }
  }
}
