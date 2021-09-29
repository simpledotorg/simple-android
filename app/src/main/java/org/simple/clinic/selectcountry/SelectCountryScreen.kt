package org.simple.clinic.selectcountry

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.appconfig.AppConfigRepository
import org.simple.clinic.appconfig.Country
import org.simple.clinic.appconfig.displayname.CountryDisplayNameFetcher
import org.simple.clinic.databinding.ListSelectcountryCountryViewBinding
import org.simple.clinic.databinding.ScreenSelectcountryBinding
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.selectcountry.adapter.Event
import org.simple.clinic.selectcountry.adapter.SelectableCountryItem
import org.simple.clinic.selectcountry.adapter.SelectableCountryItemDiffCallback
import org.simple.clinic.selectstate.SelectStateScreen
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.indexOfChildId
import javax.inject.Inject

class SelectCountryScreen : BaseScreen<
    SelectCountryScreen.Key,
    ScreenSelectcountryBinding,
    SelectCountryModel,
    SelectCountryEvent,
    SelectCountryEffect,
    Unit>(), SelectCountryUi, UiActions {

  var binding: ScreenSelectcountryBinding? = null

  private val countrySelectionViewFlipper
    get() = binding!!.countrySelectionViewFlipper

  private val supportedCountriesList
    get() = binding!!.supportedCountriesList

  private val tryAgain
    get() = binding!!.tryAgain

  private val errorMessageTextView
    get() = binding!!.errorMessageTextView

  @Inject
  lateinit var appConfigRepository: AppConfigRepository

  @Inject
  lateinit var schedulersProvider: SchedulersProvider

  @Inject
  lateinit var countryDisplayNameFetcher: CountryDisplayNameFetcher

  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var router: Router

  private val uiRenderer = SelectCountryUiRenderer(this)

  private val events by unsafeLazy {
    Observable
        .merge(
            retryClicks(),
            countrySelectionChanges()
        )
        .compose(ReportAnalyticsEvents())
        .cast<SelectCountryEvent>()
  }

  private val delegate by unsafeLazy {
    MobiusDelegate.forView(
        events = events,
        defaultModel = SelectCountryModel.FETCHING,
        init = SelectCountryInit(),
        update = SelectCountryUpdate(),
        effectHandler = SelectCountryEffectHandler.create(appConfigRepository, this, schedulersProvider),
        modelUpdateListener = uiRenderer::render
    )
  }

  private val supportedCountriesAdapter = ItemAdapter(
      diffCallback = SelectableCountryItemDiffCallback(),
      bindings = mapOf(
          R.layout.list_selectcountry_country_view to { layoutInflater, parent ->
            ListSelectcountryCountryViewBinding.inflate(layoutInflater, parent, false)
          }
      )
  )

  private val progressBarViewIndex: Int by unsafeLazy {
    countrySelectionViewFlipper.indexOfChildId(R.id.progressBar)
  }

  private val countryListViewIndex: Int by unsafeLazy {
    countrySelectionViewFlipper.indexOfChildId(R.id.countryListContainer)
  }

  private val errorViewIndex: Int by unsafeLazy {
    countrySelectionViewFlipper.indexOfChildId(R.id.errorContainer)
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<SelectCountryScreenInjector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setupCountriesList()
  }

  override fun onFinishInflate() {
    super.onFinishInflate()

    binding = ScreenSelectcountryBinding.bind(this)
    if (isInEditMode) {
      return
    }

    delegate.prepare()
  }

  private fun setupCountriesList() {
    supportedCountriesList.apply {
      setHasFixedSize(false)
      layoutManager = LinearLayoutManager(context)
      adapter = supportedCountriesAdapter
    }
  }

  private fun countrySelectionChanges(): Observable<CountryChosen> {
    return supportedCountriesAdapter
        .itemEvents
        .ofType<Event.CountryClicked>()
        .map { CountryChosen(it.country) }
  }

  private fun retryClicks(): Observable<RetryClicked> {
    return tryAgain
        .clicks()
        .map { RetryClicked }
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

  override fun onSaveInstanceState(): Parcelable? {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(delegate.onRestoreInstanceState(state))
  }

  override fun showProgress() {
    countrySelectionViewFlipper.displayedChild = progressBarViewIndex
  }

  override fun displaySupportedCountries(countries: List<Country>, chosenCountry: Country?) {
    supportedCountriesAdapter.submitList(SelectableCountryItem.from(countries, chosenCountry, countryDisplayNameFetcher))
    countrySelectionViewFlipper.displayedChild = countryListViewIndex
  }

  override fun displayNetworkErrorMessage() {
    errorMessageTextView.setText(R.string.selectcountry_networkerror)
    countrySelectionViewFlipper.displayedChild = errorViewIndex
  }

  override fun displayServerErrorMessage() {
    errorMessageTextView.setText(R.string.selectcountry_servererror)
    countrySelectionViewFlipper.displayedChild = errorViewIndex
  }

  override fun displayGenericErrorMessage() {
    errorMessageTextView.setText(R.string.selectcountry_genericerror)
    countrySelectionViewFlipper.displayedChild = errorViewIndex
  }

  override fun goToStateSelectionScreen() {
    router.push(SelectStateScreen.Key())
  }

  @Parcelize
  data class Key(
      override val analyticsName: String = "Select Country"
  ) : ScreenKey() {

    override fun instantiateFragment() = SelectCountryScreen()
  }
}
