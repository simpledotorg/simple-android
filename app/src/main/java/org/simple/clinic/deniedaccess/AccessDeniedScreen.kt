package org.simple.clinic.deniedaccess

import androidx.appcompat.app.AppCompatActivity
import org.simple.clinic.databinding.ScreenAccessDeniedBinding
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.navigation.v2.keyprovider.ScreenKeyProvider
import org.simple.clinic.util.unsafeLazy
import javax.inject.Inject

class AccessDeniedScreen : BaseScreen<
    AccessDeniedScreenKey,
    ScreenAccessDeniedBinding,
    AccessDeniedModel,
    AccessDeniedEvent,
    AccessDeniedEffect>() {

  @Inject
  lateinit var screenKeyProvider: ScreenKeyProvider

  @Inject
  lateinit var activity: AppCompatActivity

  private val screenKey by unsafeLazy {
    screenKeyProvider.keyFor<AccessDeniedScreenKey>(this)
  }

  private var binding: ScreenAccessDeniedBinding? = null

  private val userFullNameText
    get() = binding!!.userFullNameText

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    binding = ScreenAccessDeniedBinding.bind(this)

    context.injector<AccessDeniedScreenInjector>().inject(this)

    userFullNameText.text = screenKey.fullName
  }

  override fun onDetachedFromWindow() {
    binding = null
    super.onDetachedFromWindow()
  }
}
