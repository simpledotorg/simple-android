package org.simple.clinic.deniedaccess

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import org.simple.clinic.databinding.ScreenAccessDeniedBinding
import org.simple.clinic.di.injector
import org.simple.clinic.router.screen.BackPressInterceptCallback
import org.simple.clinic.router.screen.BackPressInterceptor
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.util.unsafeLazy
import javax.inject.Inject

class AccessDeniedScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var activity: AppCompatActivity

  private val screenKey by unsafeLazy {
    screenRouter.key<AccessDeniedScreenKey>(this)
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
    handleBackClicks()
  }

  private fun handleBackClicks() {
    val interceptor = object : BackPressInterceptor {
      override fun onInterceptBackPress(callback: BackPressInterceptCallback) {
        activity.finish()
        callback.markBackPressIntercepted()
      }
    }
    screenRouter.registerBackPressInterceptor(interceptor)
  }

  override fun onDetachedFromWindow() {
    binding = null
    super.onDetachedFromWindow()
  }
}
