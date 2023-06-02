package org.simple.clinic.home

import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import io.reactivex.Observable
import org.junit.Test
import org.simple.clinic.facility.FacilityConfig
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.sharedTestCode.TestData
import java.util.UUID

class HomeScreenEffectHandlerTest {

  private val facility = TestData.facility(
      uuid = UUID.fromString("251deca2-d219-4863-80fc-e7d48cb22b1b"),
      name = "PHC Obvious",
      facilityConfig = FacilityConfig(
          diabetesManagementEnabled = true,
          teleconsultationEnabled = false,
          monthlyScreeningReportsEnabled = false,
          monthlySuppliesReportsEnabled = false
      )
  )
  private val patientRepository = mock<PatientRepository>()
  private val uiActions = mock<HomeScreenUiActions>()
  private val effectHandler = HomeScreenEffectHandler(
      patientRepository = patientRepository,
      schedulersProvider = TestSchedulersProvider.trampoline(),
      viewEffectsConsumer = HomeScreenViewEffectHandler(uiActions)::handle,
      currentFacilityStream = Observable.just(facility)
  ).build()
  private val effectHandlerTestCase = EffectHandlerTestCase(
      effectHandler = effectHandler
  )

  @Test
  fun `when notification permission denied effect is received, then show notification permission denied dialog`() {
    // when
    effectHandlerTestCase.dispatch(ShowNotificationPermissionDenied)

    // then
    verify(uiActions).showNotificationPermissionDeniedDialog()
    verifyNoMoreInteractions(uiActions)
  }
}
