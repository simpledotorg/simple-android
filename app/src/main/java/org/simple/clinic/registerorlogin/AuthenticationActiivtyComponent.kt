package org.simple.clinic.registerorlogin

import dagger.Subcomponent
import org.simple.clinic.activity.BindsActivity
import org.simple.clinic.activity.BindsScreenResultBus
import org.simple.clinic.activity.BindsScreenRouter
import org.simple.clinic.deniedaccess.AccessDeniedScreenInjector
import org.simple.clinic.di.AssistedInjectModule
import org.simple.clinic.facilitypicker.FacilityPickerView
import org.simple.clinic.introvideoscreen.IntroVideoScreenInjector
import org.simple.clinic.login.pin.LoginPinScreen
import org.simple.clinic.navigation.di.FlowScreenKeyModule
import org.simple.clinic.registration.confirmpin.RegistrationConfirmPinScreen
import org.simple.clinic.registration.facility.RegistrationFacilitySelectionScreen
import org.simple.clinic.registration.location.RegistrationLocationPermissionScreen
import org.simple.clinic.registration.name.RegistrationFullNameScreen
import org.simple.clinic.registration.phone.RegistrationPhoneScreen
import org.simple.clinic.registration.phone.loggedout.LoggedOutOfDeviceDialog
import org.simple.clinic.registration.pin.RegistrationPinScreen
import org.simple.clinic.registration.register.RegistrationLoadingScreen
import org.simple.clinic.security.pin.PinEntryCardView
import org.simple.clinic.selectcountry.SelectCountryScreenInjector

@Subcomponent(modules = [
  AssistedInjectModule::class,
  FlowScreenKeyModule::class
])
interface AuthenticationActivityComponent :
    RegistrationPhoneScreen.Injector,
    AccessDeniedScreenInjector,
    LoginPinScreen.Injector,
    RegistrationFullNameScreen.Injector,
    RegistrationPinScreen.Injector,
    RegistrationConfirmPinScreen.Injector,
    RegistrationLocationPermissionScreen.Injector,
    RegistrationFacilitySelectionScreen.Injector,
    IntroVideoScreenInjector,
    RegistrationLoadingScreen.Injector,
    PinEntryCardView.Injector,
    LoggedOutOfDeviceDialog.Injector,
    SelectCountryScreenInjector,
    FacilityPickerView.Injector {
  fun inject(target: AuthenticationActivity)

  @Subcomponent.Builder
  interface Builder :
      BindsActivity<Builder>,
      BindsScreenRouter<Builder>,
      BindsScreenResultBus<Builder> {
    fun build(): AuthenticationActivityComponent
  }
}
