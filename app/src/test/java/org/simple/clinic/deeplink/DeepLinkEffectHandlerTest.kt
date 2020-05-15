package org.simple.clinic.deeplink

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import java.util.UUID

class DeepLinkEffectHandlerTest {

  private val userSession = mock<UserSession>()
  private val patientRepository = mock<PatientRepository>()
  private val uiActions = mock<DeepLinkUiActions>()

  private val effectHandler = DeepLinkEffectHandler(
      userSession = Lazy { userSession },
      schedulerProvider = TrampolineSchedulersProvider(),
      patientRepository = patientRepository,
      uiActions = uiActions
  ).build()
  private val testCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when fetch user effect is received, then fetch the user`() {
    // given
    val user = TestData.loggedInUser(
        uuid = UUID.fromString("ee59391d-ede9-46ef-8d03-ef54125fbf86")
    )
    whenever(userSession.loggedInUserImmediate()) doReturn user

    // when
    testCase.dispatch(FetchUser)

    // then
    testCase.assertOutgoingEvents(UserFetched(user))
  }

  @Test
  fun `when navigate to setup effect is received, then navigate to setup activity`() {
    // when
    testCase.dispatch(NavigateToSetupActivity)

    // then
    verify(uiActions).navigateToSetupActivity()
    verifyNoMoreInteractions(uiActions)

    testCase.assertNoOutgoingEvents()
  }

  @Test
  fun `when navigate to main activity effect is received, then navigate to main activity`() {
    // when
    testCase.dispatch(NavigateToMainActivity)

    // then
    verify(uiActions).navigateToMainActivity()
    verifyNoMoreInteractions(uiActions)

    testCase.assertNoOutgoingEvents()
  }

  @Test
  fun `when fetch patient effect is received, then fetch the patient`() {
    // given
    val patientUuid = UUID.fromString("4db4acb9-5b0e-459f-b81d-650938029666")
    val patient = TestData.patient(
        uuid = patientUuid
    )

    whenever(patientRepository.patientImmediate(patientUuid)) doReturn patient

    // when
    testCase.dispatch(FetchPatient(patientUuid))

    // then
    verifyZeroInteractions(uiActions)

    testCase.assertOutgoingEvents(PatientFetched(patient))
  }
}
