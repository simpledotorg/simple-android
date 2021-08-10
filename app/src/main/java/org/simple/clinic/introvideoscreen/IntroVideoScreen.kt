package org.simple.clinic.introvideoscreen

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ScreenIntroVideoBinding
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.compat.wrap
import org.simple.clinic.navigation.v2.keyprovider.ScreenKeyProvider
import org.simple.clinic.platform.crash.CrashReporter
import org.simple.clinic.registration.register.RegistrationLoadingScreenKey
import org.simple.clinic.simplevideo.SimpleVideo
import org.simple.clinic.simplevideo.SimpleVideoConfig
import org.simple.clinic.simplevideo.SimpleVideoConfig.Type.TrainingVideo
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.util.unsafeLazy
import javax.inject.Inject

class IntroVideoScreen(
    context: Context,
    attrs: AttributeSet
) : ConstraintLayout(context, attrs), UiActions {

  var binding: ScreenIntroVideoBinding? = null

  private val introVideoSubtitle
    get() = binding!!.introVideoSubtitle

  private val introVideoImageView
    get() = binding!!.introVideoImageView

  private val watchVideoButton
    get() = binding!!.watchVideoButton

  private val skipButton
    get() = binding!!.skipButton

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var screenKeyProvider: ScreenKeyProvider

  @Inject
  @SimpleVideoConfig(TrainingVideo)
  lateinit var simpleVideo: SimpleVideo

  @Inject
  lateinit var introVideoEffectHandler: IntroVideoEffectHandler.Factory

  private val events: Observable<IntroVideoEvent> by unsafeLazy {
    Observable
        .mergeArray(
            videoClicks(),
            skipClicks()
        )
        .compose(ReportAnalyticsEvents())
        .cast<IntroVideoEvent>()
  }

  private val mobiusDelegate by unsafeLazy {
    MobiusDelegate.forView(
        events,
        IntroVideoModel.default(),
        IntroVideoUpdate(),
        introVideoEffectHandler.create(this).build()
    )
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    binding = ScreenIntroVideoBinding.bind(this)
    if (isInEditMode) return

    context.injector<IntroVideoScreenInjector>().inject(this)

    introVideoSubtitle.text = resources.getString(R.string.simple_video_duration, simpleVideo.duration)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    mobiusDelegate.start()
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    mobiusDelegate.stop()
    binding = null
  }

  override fun openVideo() {
    openYoutubeLinkForSimpleVideo()
  }

  override fun openHome() {
    val screenKey = screenKeyProvider.keyFor<Key>(this)
    router.push(RegistrationLoadingScreenKey(screenKey.registrationEntry).wrap())
  }

  private fun videoClicks(): Observable<IntroVideoEvent> {
    val clicksFromVideoImage = introVideoImageView.clicks().map { VideoClicked }
    val clicksFromWatchVideoButton = watchVideoButton.clicks().map { VideoClicked }

    return clicksFromVideoImage
        .mergeWith(clicksFromWatchVideoButton)
        .cast()
  }

  private fun skipClicks(): Observable<IntroVideoEvent> {
    return skipButton
        .clicks()
        .map { SkipClicked }
  }

  private fun openYoutubeLinkForSimpleVideo() {
    val packageManager = context.packageManager
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(simpleVideo.url))

    if (intent.resolveActivity(packageManager) != null) {
      context.startActivity(intent)
    } else {
      CrashReporter.report(ActivityNotFoundException("Unable to play simple video because no supporting apps were found."))
    }
  }

  @Parcelize
  data class Key(
      val registrationEntry: OngoingRegistrationEntry,
      override val analyticsName: String = "Onboarding Intro Video"
  ) : ScreenKey() {
    override fun instantiateFragment() = IntroVideoScreen()
  }
}
