package org.simple.clinic.selectcountry

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.Observable
import kotlinx.android.synthetic.main.screen_selectcountry.view.*
import org.simple.clinic.R
import org.simple.clinic.appconfig.AppConfigRepository
import org.simple.clinic.appconfig.Country
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.platform.crash.CrashReporter
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

  @Inject
  lateinit var appConfigRepository: AppConfigRepository

  @Inject
  lateinit var schedulersProvider: SchedulersProvider

  @Inject
  lateinit var crashReporter: CrashReporter

  private val uiRenderer = SelectCountryUiRenderer(this)

  private val delegate by unsafeLazy {
    MobiusDelegate(
        events = Observable.never<SelectCountryEvent>(),
        defaultModel = SelectCountryModel.FETCHING,
        init = SelectCountryInit(),
        update = SelectCountryUpdate(),
        effectHandler = SelectCountryEffectHandler.create(appConfigRepository, this, schedulersProvider),
        modelUpdateListener = uiRenderer::render,
        crashReporter = crashReporter
    )
  }

  private val supportedCountriesAdapter: ItemAdapter<SelectableCountryItem, Event> = ItemAdapter(SelectableCountryItemDiffCallback())

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

  override fun showProgress() {
    countrySelectionViewFlipper.displayedChild = progressBarViewIndex
  }

  override fun displaySupportedCountries(countries: List<Country>, chosenCountry: Country?) {
    supportedCountriesAdapter.submitList(SelectableCountryItem.from(countries, chosenCountry))
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
    // TODO(vs): 2019-11-07 Open TheActivity
  }
}
