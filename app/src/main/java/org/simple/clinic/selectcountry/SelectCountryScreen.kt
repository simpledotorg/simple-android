package org.simple.clinic.selectcountry

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.appconfig.AppConfigRepository
import org.simple.clinic.appconfig.Country
import org.simple.clinic.appconfig.displayname.CountryDisplayNameFetcher
import org.simple.clinic.databinding.ListSelectcountryCountryViewBinding
import org.simple.clinic.databinding.ScreenSelectcountryBinding
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.platform.crash.CrashReporter
import org.simple.clinic.registration.phone.RegistrationPhoneScreenKey
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.selectcountry.adapter.Event
import org.simple.clinic.selectcountry.adapter.SelectableCountryItem
import org.simple.clinic.selectcountry.adapter.SelectableCountryItemDiffCallback
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.indexOfChildId
import javax.inject.Inject

class SelectCountryScreen(
    context: Context,
    attributeSet: AttributeSet
) : ConstraintLayout(context, attributeSet), SelectCountryUi, UiActions {

  var binding: ScreenSelectcountryBinding? = null

  private val countrySelectionViewFlipper
    get() = binding!!.countrySelectionViewFlipper

  private val supportedCountriesList
    get() = binding!!.supportedCountriesList

  private val tryAgain
    get() = binding!!.tryAgain

  private val nextButton
    get() = binding!!.nextButton

  private val errorMessageTextView
    get() = binding!!.errorMessageTextView

  private val nextButtonFrame
    get() = binding!!.nextButtonFrame

  @Inject
  lateinit var appConfigRepository: AppConfigRepository

  @Inject
  lateinit var schedulersProvider: SchedulersProvider

  @Inject
  lateinit var crashReporter: CrashReporter

  @Inject
  lateinit var countryDisplayNameFetcher: CountryDisplayNameFetcher

  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var screenRouter: ScreenRouter

  private val uiRenderer = SelectCountryUiRenderer(this)

  private val events by unsafeLazy {
    Observable
        .merge(
            nextClicks(),
            retryClicks(),
            countrySelectionChanges()
        )
        .compose(ReportAnalyticsEvents())
        .cast<SelectCountryEvent>()
  }

  private val delegate by unsafeLazy {
    MobiusDelegate(
        events = events,
        defaultModel = SelectCountryModel.FETCHING,
        init = SelectCountryInit(),
        update = SelectCountryUpdate(),
        effectHandler = SelectCountryEffectHandler.create(appConfigRepository, this, schedulersProvider),
        modelUpdateListener = uiRenderer::render,
        crashReporter = crashReporter
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

  override fun onFinishInflate() {
    super.onFinishInflate()

    binding = ScreenSelectcountryBinding.bind(this)
    if (isInEditMode) {
      return
    }

    context.injector<SelectCountryScreenInjector>().inject(this)

    setupCountriesList()

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
    return RxView
        .clicks(tryAgain)
        .map { RetryClicked }
  }

  private fun nextClicks(): Observable<NextClicked> {
    return RxView
        .clicks(nextButton)
        .map { NextClicked }
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

  override fun showNextButton() {
    nextButtonFrame.visibility = VISIBLE
  }

  override fun goToNextScreen() {
    screenRouter.push(RegistrationPhoneScreenKey())
  }
}
