package org.simple.clinic.selectstate

import android.content.Context
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
import org.simple.clinic.appconfig.State
import org.simple.clinic.databinding.ListSelectstateStateViewBinding
import org.simple.clinic.databinding.ScreenSelectStateBinding
import org.simple.clinic.di.injector
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.registration.phone.RegistrationPhoneScreenKey
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

class SelectStateScreen : BaseScreen<
    SelectStateScreen.Key,
    ScreenSelectStateBinding,
    SelectStateModel,
    SelectStateEvent,
    SelectStateEffect,
    SelectStateViewEffect>(), SelectStateUi, SelectStateUiActions {

  @Inject
  lateinit var effectHandlerFactory: SelectStateEffectHandler.Factory

  @Inject
  lateinit var router: Router

  private val statesListGroup
    get() = binding.statesListGroup

  private val errorGroup
    get() = binding.errorGroup

  private val errorMessageTextView
    get() = binding.errorMessageTextView

  private val statesList
    get() = binding.statesList

  private val progressBar
    get() = binding.progressBar

  private val retryButton
    get() = binding.tryAgain

  private val statesAdapter = ItemAdapter(
      diffCallback = SelectableStateItem.SelectableStateItemDiffCallback(),
      bindings = mapOf(
          R.layout.list_selectstate_state_view to { layoutInflater, parent ->
            ListSelectstateStateViewBinding.inflate(layoutInflater, parent, false)
          }
      )
  )

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    statesList.adapter = statesAdapter
  }

  override fun defaultModel() = SelectStateModel.create()

  override fun bindView(
      layoutInflater: LayoutInflater,
      container: ViewGroup?
  ) = ScreenSelectStateBinding.inflate(layoutInflater, container, false)

  override fun uiRenderer() = SelectStateUiRenderer(this)

  override fun viewEffectHandler() = SelectStateViewEffectHandler(this)

  override fun createInit() = SelectStateInit()

  override fun createUpdate() = SelectStateUpdate()

  override fun createEffectHandler(viewEffectsConsumer: Consumer<SelectStateViewEffect>) =
      effectHandlerFactory
          .create(viewEffectsConsumer)
          .build()

  override fun events() = Observable
      .mergeArray(
          retryButtonClicks(),
          stateSelectedEvents()
      )
      .compose(ReportAnalyticsEvents())
      .cast<SelectStateEvent>()

  override fun showStates(states: List<State>, selectedState: State?) {
    statesListGroup.visibility = View.VISIBLE
    statesAdapter.submitList(SelectableStateItem.from(states = states, selectedState = selectedState))
  }

  override fun hideStates() {
    statesListGroup.visibility = View.GONE
  }

  override fun showNetworkErrorMessage() {
    errorGroup.visibility = View.VISIBLE
    errorMessageTextView.setText(R.string.select_state_networkerror)
  }

  override fun showServerErrorMessage() {
    errorGroup.visibility = View.VISIBLE
    errorMessageTextView.setText(R.string.select_state_servererror)
  }

  override fun showGenericErrorMessage() {
    errorGroup.visibility = View.VISIBLE
    errorMessageTextView.setText(R.string.select_state_genericerror)
  }

  override fun hideErrorView() {
    errorGroup.visibility = View.GONE
  }

  override fun showProgress() {
    progressBar.visibility = View.VISIBLE
  }

  override fun hideProgress() {
    progressBar.visibility = View.GONE
  }

  override fun goToRegistrationScreen() {
    router.push(RegistrationPhoneScreenKey())
  }

  override fun replaceCurrentScreenToRegistrationScreen() {
    router.replaceTop(RegistrationPhoneScreenKey())
  }

  private fun stateSelectedEvents(): Observable<UiEvent> {
    return statesAdapter
        .itemEvents
        .map { StateChanged(it.state) }
  }

  private fun retryButtonClicks(): Observable<UiEvent> {
    return retryButton
        .clicks()
        .map { RetryButtonClicked }
  }

  @Parcelize
  data class Key(
      override val analyticsName: String = "Select State Screen"
  ) : ScreenKey() {

    override fun instantiateFragment() = SelectStateScreen()
  }

  interface Injector {
    fun inject(target: SelectStateScreen)
  }
}
