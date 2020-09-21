package org.simple.clinic.teleconsultlog.prescription.doctorinfo

import android.graphics.Bitmap
import com.f2prateek.rx.preferences2.Preference
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
import org.simple.clinic.signature.SignatureRepository
import org.simple.clinic.util.Optional
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.util.UUID

class TeleconsultDoctorInfoEffectHandlerTest {

  private val medicalRegistrationIdPreference = mock<Preference<Optional<String>>>()
  private val signatureRepository = mock<SignatureRepository>()
  private val currentUser = TestData.loggedInUser(
      uuid = UUID.fromString("a3c0b347-3031-40f4-b150-9e25bed3c334")
  )
  private val uiActions = mock<TeleconsultDoctorInfoUiActions>()
  private val effectHandler = TeleconsultDoctorInfoEffectHandler(
      medicalRegistrationIdPreference = medicalRegistrationIdPreference,
      signatureRepository = signatureRepository,
      currentUser = Lazy { currentUser },
      schedulersProvider = TestSchedulersProvider.trampoline(),
      uiActions = uiActions
  ).build()
  private val effectHandlerTestCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    effectHandlerTestCase.dispose()
  }

  @Test
  fun `when load medical registration id effect is received, then load medical registration id`() {
    // given
    val medicalRegistrationId = "1234567890"

    whenever(medicalRegistrationIdPreference.get()) doReturn Optional.of(medicalRegistrationId)

    // when
    effectHandlerTestCase.dispatch(LoadMedicalRegistrationId)

    // then
    effectHandlerTestCase.assertOutgoingEvents(MedicalRegistrationIdLoaded(medicalRegistrationId))

    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when set medical registration effect is received, then set medical registration id`() {
    // given
    val medicalRegistrationId = "1234567890"

    // when
    effectHandlerTestCase.dispatch(SetMedicalRegistrationId(medicalRegistrationId))

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()

    verify(uiActions).setMedicalRegistrationId(medicalRegistrationId)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when load signature effect is received, then load the signature`() {
    // given
    val bitmap = mock<Bitmap>()
    whenever(signatureRepository.getSignatureBitmap()) doReturn bitmap

    // when
    effectHandlerTestCase.dispatch(LoadSignatureBitmap)

    // then
    effectHandlerTestCase.assertOutgoingEvents(SignatureBitmapLoaded(bitmap))

    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when set signature bitmap effect is received, then set signature bitmap`() {
    // given
    val bitmap = mock<Bitmap>()

    // when
    effectHandlerTestCase.dispatch(SetSignatureBitmap(bitmap))

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()

    verify(uiActions).setSignatureBitmap(bitmap)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when load current user effect is received, then load the current user`() {
    // when
    effectHandlerTestCase.dispatch(LoadCurrentUser)

    // then
    effectHandlerTestCase.assertOutgoingEvents(CurrentUserLoaded(currentUser))

    verifyZeroInteractions(uiActions)
  }
}
