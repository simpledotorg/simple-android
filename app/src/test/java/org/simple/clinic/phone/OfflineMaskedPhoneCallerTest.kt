package org.simple.clinic.phone

import android.content.Context
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.mock
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.util.RxErrorsRule

class OfflineMaskedPhoneCallerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private lateinit var maskedPhoneCaller: MaskedPhoneCaller
  private lateinit var config: PhoneNumberMaskerConfig

  @Before
  fun setUp() {
    maskedPhoneCaller = OfflineMaskedPhoneCaller(Single.fromCallable { config }, mock())
  }

  @Test
  fun `when masking is disabled then plain phone numbers should be called`() {
    config = PhoneNumberMaskerConfig(maskingEnabled = false)

    val caller = MockCaller()
    val plainNumber = "123"

    maskedPhoneCaller.maskAndCall(plainNumber, caller = caller).blockingAwait()

    assertThat(caller.calledNumber).isEqualTo(plainNumber)
  }

  class MockCaller : Caller {
    lateinit var calledNumber: String

    override fun call(context: Context, phoneNumber: String) {
      calledNumber = phoneNumber
    }
  }
}
