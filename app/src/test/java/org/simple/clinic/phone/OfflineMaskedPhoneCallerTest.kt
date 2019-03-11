package org.simple.clinic.phone

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.util.RxErrorsRule

class OfflineMaskedPhoneCallerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private lateinit var maskedPhoneCaller: MaskedPhoneCaller
  private lateinit var config: PhoneNumberMaskerConfig
  private val caller: Caller = mock()
  private val activity: TheActivity = mock()

  @Before
  fun setUp() {
    maskedPhoneCaller = OfflineMaskedPhoneCaller(Single.fromCallable { config }, activity)
  }

  @Test
  fun `when masking is disabled then plain phone numbers should be called`() {
    config = PhoneNumberMaskerConfig(maskingEnabled = false)

    val plainNumber = "123"

    maskedPhoneCaller.maskAndCall(plainNumber, caller = caller).blockingAwait()

    verify(caller).call(activity, "123")
  }

  @Test
  fun `when masking is enabled then masked phone number should be called`() {
    config = PhoneNumberMaskerConfig(maskingEnabled = true)
    val plainNumber = "123"

    maskedPhoneCaller.maskAndCall(plainNumber, caller = caller).blockingAwait()

    verify(caller).call(activity, "+1 111 111 1111,123#")
  }
}
