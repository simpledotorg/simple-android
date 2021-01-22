package org.simple.clinic.settings.changelanguage

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ListChangeLanguageViewBinding
import org.simple.clinic.databinding.ScreenChangeLanguageBinding
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.settings.Language
import org.simple.clinic.settings.changelanguage.ChangeLanguageListItem.Event.ListItemClicked
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ItemAdapter
import javax.inject.Inject

class ChangeLanguageScreen(
    context: Context,
    attributeSet: AttributeSet
) : ConstraintLayout(context, attributeSet), ChangeLanguageUi, UiActions {

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var effectHandler: ChangeLanguageEffectHandler.Factory

  private var binding: ScreenChangeLanguageBinding? = null

  private val toolbar
    get() = binding!!.toolbar

  private val languagesList
    get() = binding!!.languagesList

  private val doneButton
    get() = binding!!.doneButton

  private val languagesAdapter = ItemAdapter(
      diffCallback = ChangeLanguageListItem.DiffCallback(),
      bindings = mapOf(
          R.layout.list_change_language_view to { layoutInflater, parent ->
            ListChangeLanguageViewBinding.inflate(layoutInflater, parent, false)
          }
      )
  )

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
    MobiusDelegate.forView(
        events = events,
        defaultModel = ChangeLanguageModel.FETCHING_LANGUAGES,
        init = ChangeLanguageInit(),
        update = ChangeLanguageUpdate(),
        effectHandler = effectHandler.create(uiActions = this).build(),
        modelUpdateListener = uiRenderer::render
    )
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    binding = ScreenChangeLanguageBinding.bind(this)

    context.injector<Injector>().inject(this)

    setupLanguagesList()
    toolbar.setNavigationOnClickListener { router.pop() }
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
    return doneButton
        .clicks()
        .map { SaveCurrentLanguageEvent }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    delegate.stop()
    binding = null
    super.onDetachedFromWindow()
  }

  override fun onSaveInstanceState(): Parcelable {
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
    router.pop()
  }

  override fun restartActivity() {
    activity.recreate()
  }

  interface Injector {
    fun inject(target: ChangeLanguageScreen)
  }
}
