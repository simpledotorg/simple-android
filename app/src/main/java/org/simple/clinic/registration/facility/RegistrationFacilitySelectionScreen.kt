package org.simple.clinic.registration.facility

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.support.v7.widget.RxRecyclerView
import com.jakewharton.rxbinding3.view.detaches
import com.jakewharton.rxbinding3.widget.textChanges
import com.mikepenz.itemanimators.SlideUpAlphaAnimator
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import kotlinx.android.synthetic.main.screen_registration_facility_selection.view.*
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.bindUiToController
import org.simple.clinic.di.injector
import org.simple.clinic.facility.change.FacilitiesUpdateType
import org.simple.clinic.facility.change.FacilitiesUpdateType.FIRST_UPDATE
import org.simple.clinic.facility.change.FacilitiesUpdateType.SUBSEQUENT_UPDATE
import org.simple.clinic.facility.change.FacilityListItem
import org.simple.clinic.introvideoscreen.IntroVideoScreenKey
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.registration.RegistrationConfig
import org.simple.clinic.registration.confirmfacility.ConfirmFacilitySheet
import org.simple.clinic.router.screen.ActivityResult
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.util.extractSuccessful
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.RecyclerViewUserScrollDetector
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.displayedChildResId
import org.simple.clinic.widgets.hideKeyboard
import java.util.UUID
import javax.inject.Inject

class RegistrationFacilitySelectionScreen(
    context: Context,
    attrs: AttributeSet
) : RelativeLayout(context, attrs), RegistrationFacilitySelectionUi {

  @Inject
  lateinit var controller: RegistrationFacilitySelectionScreenController

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var effectHandlerFactory: RegistrationFacilitySelectionEffectHandler.Factory

  @Inject
  lateinit var config: RegistrationConfig

  private val recyclerViewAdapter = FacilitiesAdapter()

  private val screenDestroys: Observable<ScreenDestroyed> = detaches()
      .map { ScreenDestroyed() }
      .share()

  private val events by unsafeLazy {
    Observable
        .mergeArray(
            screenCreates(),
            searchQueryChanges(),
            facilityClicks(),
            registrationFacilityConfirmations(screenDestroys)
        )
        .compose(ReportAnalyticsEvents())
        .share()
  }

  private val delegate by unsafeLazy {
    val uiRenderer = RegistrationFacilitySelectionUiRenderer(this)

    MobiusDelegate.forView(
        events = events.ofType(),
        defaultModel = RegistrationFacilitySelectionModel.create(),
        update = RegistrationFacilitySelectionUpdate(),
        effectHandler = effectHandlerFactory.create(this).build(),
        init = RegistrationFacilitySelectionInit.create(config),
        modelUpdateListener = uiRenderer::render
    )
  }

  @SuppressLint("CheckResult")
  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    context.injector<Injector>().inject(this)

    bindUiToController(
        ui = this,
        events = events,
        controller = controller,
        screenDestroys = screenDestroys
    )

    toolbarViewWithSearch.setNavigationOnClickListener {
      screenRouter.pop()
    }
    toolbarViewWithoutSearch.setNavigationOnClickListener {
      screenRouter.pop()
    }

    facilityRecyclerView.layoutManager = LinearLayoutManager(context)
    facilityRecyclerView.adapter = recyclerViewAdapter

    searchEditText.requestFocus()

    // Hiding the keyboard without adding a post{} block doesn't seem to work.
    post { hideKeyboard() }
    hideKeyboardOnListScroll(screenDestroys)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    delegate.stop()
  }

  override fun onSaveInstanceState(): Parcelable? {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(delegate.onRestoreInstanceState(state))
  }

  private fun screenCreates() = Observable.just(ScreenCreated())

  private fun searchQueryChanges() =
      searchEditText
          .textChanges()
          .map { text -> RegistrationFacilitySearchQueryChanged(text.toString()) }

  private fun facilityClicks() =
      recyclerViewAdapter
          .facilityClicks
          .map(::RegistrationFacilityClicked)

  private fun registrationFacilityConfirmations(onScreenDestroyed: Observable<ScreenDestroyed>): Observable<UiEvent> {
    return screenRouter
        .streamScreenResults()
        .ofType<ActivityResult>()
        .takeUntil(onScreenDestroyed)
        .extractSuccessful(CONFIRM_FACILITY_SHEET) { intent ->
          val confirmedFacilityUuid = ConfirmFacilitySheet.confirmedFacilityUuid(intent)
          RegistrationFacilityConfirmed(confirmedFacilityUuid)
        }
  }

  @SuppressLint("CheckResult")
  private fun hideKeyboardOnListScroll(onScreenDestroyed: Observable<ScreenDestroyed>) {
    val scrollEvents = RxRecyclerView.scrollEvents(facilityRecyclerView)
    val scrollStateChanges = RxRecyclerView.scrollStateChanges(facilityRecyclerView)

    Observables.combineLatest(scrollEvents, scrollStateChanges)
        .compose(RecyclerViewUserScrollDetector.streamDetections())
        .filter { it.byUser }
        .takeUntil(onScreenDestroyed)
        .subscribe {
          hideKeyboard()
        }
  }

  override fun showProgressIndicator() {
    progressView.visibility = VISIBLE
  }

  override fun hideProgressIndicator() {
    progressView.visibility = GONE
  }

  override fun showToolbarWithSearchField() {
    toolbarViewFlipper.displayedChildResId = R.id.toolbarViewWithSearch
  }

  override fun showToolbarWithoutSearchField() {
    toolbarViewFlipper.displayedChildResId = R.id.toolbarViewWithoutSearch
  }

  override fun updateFacilities(facilityItems: List<FacilityListItem>, updateType: FacilitiesUpdateType) {
    // Avoid animating the items on their first entry.
    facilityRecyclerView.itemAnimator = when (updateType) {
      FIRST_UPDATE -> null
      SUBSEQUENT_UPDATE -> SlideUpAlphaAnimator()
          .withInterpolator(FastOutSlowInInterpolator())
          .apply { moveDuration = 200 }
    }

    facilityRecyclerView.scrollToPosition(0)
    recyclerViewAdapter.submitList(facilityItems)
  }

  override fun openIntroVideoScreen() {
    screenRouter.push(IntroVideoScreenKey())
  }

  override fun showConfirmFacilitySheet(facilityUuid: UUID, facilityName: String) {
    val intent = ConfirmFacilitySheet.intentForConfirmFacilitySheet(context, facilityUuid, facilityName)
    activity.startActivityForResult(intent, CONFIRM_FACILITY_SHEET)
  }

  companion object {
    private const val CONFIRM_FACILITY_SHEET = 1
  }

  interface Injector {
    fun inject(target: RegistrationFacilitySelectionScreen)
  }
}
