package org.simple.clinic.introvideoscreen

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding3.view.clicks
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ScreenIntroVideoBinding
import org.simple.clinic.di.injector
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.platform.crash.CrashReporter
import org.simple.clinic.registration.register.RegistrationLoadingScreen
import org.simple.clinic.simplevideo.SimpleVideo
import org.simple.clinic.simplevideo.SimpleVideoConfig
import org.simple.clinic.simplevideo.SimpleVideoConfig.Type.TrainingVideo
import org.simple.clinic.user.OngoingRegistrationEntry
import javax.inject.Inject

class IntroVideoScreen : BaseScreen<
    IntroVideoScreen.Key,
    ScreenIntroVideoBinding,
    IntroVideoModel,
    IntroVideoEvent,
    IntroVideoEffect,
    IntroVideoViewEffect>(), UiActions {

  private val introVideoSubtitle
    get() = binding.introVideoSubtitle

  private val introVideoImageView
    get() = binding.introVideoImageView

  private val watchVideoButton
    get() = binding.watchVideoButton

  private val skipButton
    get() = binding.skipButton

  @Inject
  lateinit var router: Router

  @Inject
  @SimpleVideoConfig(TrainingVideo)
  lateinit var simpleVideo: SimpleVideo

  @Inject
  lateinit var introVideoEffectHandler: IntroVideoEffectHandler.Factory

  override fun onAttach(context: Context) {
    super.onAttach(context)
    requireContext().injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    introVideoSubtitle.text = resources.getString(R.string.simple_video_duration, simpleVideo.duration)
  }

  override fun openVideo() {
    openYoutubeLinkForSimpleVideo()
  }

  override fun openHome() {
    router.push(RegistrationLoadingScreen.Key(screenKey.registrationEntry))
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
    val packageManager = requireContext().packageManager
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(simpleVideo.url))

    if (intent.resolveActivity(packageManager) != null) {
      requireContext().startActivity(intent)
    } else {
      CrashReporter.report(ActivityNotFoundException("Unable to play simple video because no supporting apps were found."))
    }
  }

  override fun bindView(
      layoutInflater: LayoutInflater,
      container: ViewGroup?
  ) = ScreenIntroVideoBinding.inflate(layoutInflater, container, false)

  override fun defaultModel() = IntroVideoModel.default()

  override fun createUpdate() = IntroVideoUpdate()

  override fun createEffectHandler(
      viewEffectsConsumer: Consumer<IntroVideoViewEffect>
  ) = introVideoEffectHandler
      .create(viewEffectsConsumer)
      .build()

  override fun viewEffectHandler() = IntroVideoViewEffectHandler(this)

  override fun events() = Observable
      .mergeArray(
          videoClicks(),
          skipClicks()
      )
      .compose(ReportAnalyticsEvents())
      .cast<IntroVideoEvent>()

  @Parcelize
  data class Key(
      val registrationEntry: OngoingRegistrationEntry,
      override val analyticsName: String = "Onboarding Intro Video"
  ) : ScreenKey() {
    override fun instantiateFragment() = IntroVideoScreen()
  }

  interface Injector {
    fun inject(target: IntroVideoScreen)
  }
}
