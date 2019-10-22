package org.simple.clinic.settings.changelanguage

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import kotlinx.android.synthetic.main.screen_change_language.view.*
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.main.TheActivity
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.platform.crash.CrashReporter
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.settings.Language
import org.simple.clinic.settings.SettingsRepository
import org.simple.clinic.settings.changelanguage.ChangeLanguageListItem.Event.ListItemClicked
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ItemAdapter
import javax.inject.Inject

class ChangeLanguageScreen(
    context: Context,
    attributeSet: AttributeSet
) : ConstraintLayout(context, attributeSet), ChangeLanguageUi, UiActions {

  @Inject
  lateinit var schedulersProvider: SchedulersProvider

  @Inject
  lateinit var settingsRepository: SettingsRepository

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var crashReporter: CrashReporter

  @Inject
  lateinit var activity: AppCompatActivity

  private val languagesAdapter = ItemAdapter(ChangeLanguageListItem.DiffCallback())

  private val events: Observable<ChangeLanguageEvent> by unsafeLazy {
    Observable
        .merge(
            doneButtonClicks(),
            languageSelections()
        )
        .compose(ReportAnalyticsEvents())
        .cast<ChangeLanguageEvent>()
  }

  private val uiRenderer = ChangeLanguageUiRenderer(this)

  private val delegate: MobiusDelegate<ChangeLanguageModel, ChangeLanguageEvent, ChangeLanguageEffect> by unsafeLazy {
    MobiusDelegate(
        events = events,
        defaultModel = ChangeLanguageModel.FETCHING_LANGUAGES,
        init = ChangeLanguageInit(),
        update = ChangeLanguageUpdate(),
        effectHandler = ChangeLanguageEffectHandler.create(
            schedulersProvider = schedulersProvider,
            settingsRepository = settingsRepository,
            uiActions = this
        ),
        modelUpdateListener = uiRenderer::render,
        crashReporter = crashReporter
    )
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    TheActivity.component.inject(this)

    setupLanguagesList()
    toolbar.setNavigationOnClickListener { screenRouter.pop() }

    delegate.prepare()
  }

  private fun setupLanguagesList() {
    languagesList.apply {
      setHasFixedSize(true)
      layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
      adapter = languagesAdapter
    }
  }

  private fun languageSelections(): Observable<SelectLanguageEvent> {
    return languagesAdapter
        .itemEvents
        .ofType<ListItemClicked>()
        .map { it.language }
        .map(::SelectLanguageEvent)
  }

  private fun doneButtonClicks(): Observable<SaveCurrentLanguageEvent> {
    return RxView
        .clicks(doneButton)
        .map { SaveCurrentLanguageEvent }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    delegate.stop()
    super.onDetachedFromWindow()
  }

  override fun onSaveInstanceState(): Parcelable? {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(delegate.onRestoreInstanceState(state))
  }

  override fun displayLanguages(supportedLanguages: List<Language>, selectedLanguage: Language?) {
    languagesAdapter.submitList(ChangeLanguageListItem.from(supportedLanguages, selectedLanguage))
  }

  override fun setDoneButtonDisabled() {
    doneButton.isEnabled = false
  }

  override fun setDoneButtonEnabled() {
    doneButton.isEnabled = true
  }

  override fun goBackToPreviousScreen() {
    screenRouter.pop()
  }

  override fun restartActivity() {
    activity.recreate()
  }
}
