package org.simple.clinic.settings.changelanguage

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding3.view.clicks
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ListChangeLanguageViewBinding
import org.simple.clinic.databinding.ScreenChangeLanguageBinding
import org.simple.clinic.di.injector
import org.simple.clinic.main.TheActivity
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.settings.Language
import org.simple.clinic.settings.changelanguage.ChangeLanguageListItem.Event.ListItemClicked
import org.simple.clinic.widgets.ItemAdapter
import javax.inject.Inject

class ChangeLanguageScreen : BaseScreen<
    ChangeLanguageScreen.Key,
    ScreenChangeLanguageBinding,
    ChangeLanguageModel,
    ChangeLanguageEvent,
    ChangeLanguageEffect,
    Unit>(), ChangeLanguageUi, UiActions {

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var effectHandlerFactory: ChangeLanguageEffectHandler.Factory

  private val toolbar
    get() = binding.toolbar

  private val languagesList
    get() = binding.languagesList

  private val doneButton
    get() = binding.doneButton

  private val languagesAdapter = ItemAdapter(
      diffCallback = ChangeLanguageListItem.DiffCallback(),
      bindings = mapOf(
          R.layout.list_change_language_view to { layoutInflater, parent ->
            ListChangeLanguageViewBinding.inflate(layoutInflater, parent, false)
          }
      )
  )

  override fun defaultModel() = ChangeLanguageModel.FETCHING_LANGUAGES

  override fun createInit() = ChangeLanguageInit()

  override fun createUpdate() = ChangeLanguageUpdate()

  override fun createEffectHandler(viewEffectsConsumer: Consumer<Unit>) =
      effectHandlerFactory.create(this).build()

  override fun uiRenderer() = ChangeLanguageUiRenderer(this)

  override fun events() = Observable
      .merge(
          doneButtonClicks(),
          languageSelections()
      )
      .compose(ReportAnalyticsEvents())
      .cast<ChangeLanguageEvent>()

  override fun bindView(
      layoutInflater: LayoutInflater,
      container: ViewGroup?
  ) = ScreenChangeLanguageBinding.inflate(layoutInflater, container, false)

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setupLanguagesList()
    toolbar.setNavigationOnClickListener { router.pop() }
  }

  override fun onDestroyView() {
    languagesList.adapter = null
    super.onDestroyView()
  }

  private fun setupLanguagesList() {
    languagesList.apply {
      setHasFixedSize(true)
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
    // We are setting `isFreshAuthentication` as true to prevent from showing app lock screen
    // after restarting `TheActivity`
    startActivity(TheActivity.newIntent(requireContext(), isFreshAuthentication = true))
    requireActivity().finish()
  }

  interface Injector {
    fun inject(target: ChangeLanguageScreen)
  }

  @Parcelize
  data class Key(
      override val analyticsName: String = "Language Selection"
  ) : ScreenKey() {

    override fun instantiateFragment() = ChangeLanguageScreen()
  }
}
