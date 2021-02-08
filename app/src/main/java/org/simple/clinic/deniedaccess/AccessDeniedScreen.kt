package org.simple.clinic.deniedaccess

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import org.simple.clinic.databinding.ScreenAccessDeniedBinding
import org.simple.clinic.di.injector
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import javax.inject.Inject

class AccessDeniedScreen : BaseScreen<
    AccessDeniedScreenKey,
    ScreenAccessDeniedBinding,
    AccessDeniedModel,
    AccessDeniedEvent,
    AccessDeniedEffect>() {

  @Inject
  lateinit var activity: AppCompatActivity

  private val userFullNameText
    get() = binding.userFullNameText

  override fun defaultModel() = AccessDeniedModel()

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) =
      ScreenAccessDeniedBinding.inflate(layoutInflater, container, false)

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<AccessDeniedScreenInjector>().inject(this)
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    binding = ScreenAccessDeniedBinding.bind(this)

    userFullNameText.text = screenKey.fullName
  }

  override fun onDetachedFromWindow() {
    binding = null
    super.onDetachedFromWindow()
  }
}
