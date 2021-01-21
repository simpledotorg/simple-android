# Implementing a new screen in Mobius

#### Core components
It is recommended to read the [Concepts](https://github.com/spotify/mobius/wiki/Concepts) page on the Mobius wiki before proceeding.

We will take an example feature along with sample implementations for the various components and key classes:

##### `Model`
[**DOC**](https://github.com/spotify/mobius/wiki/Concepts#model)

This will typically be represented as a data class that has all the properties required to render a given screen. We will also make these classes `Parcelable` so that we can save and restore the state across screen configuration changes.

```kotlin
@Parcelize
data class ChangeLanguageModel(
    val currentLanguage: Language?,
    val userSelectedLanguage: Language?,
    val supportedLanguages: List<Language>,
    val manuallyRestarted: Boolean
): Parcelable {

  val haveLanguagesBeenFetched: Boolean
    get() = currentLanguage != null && supportedLanguages.isNotEmpty()

  companion object {
    val FETCHING_LANGUAGES = ChangeLanguageModel(
        currentLanguage = null,
        userSelectedLanguage = null,
        supportedLanguages = emptyList(),
        manuallyRestarted = false
    )
  }

  fun withCurrentLanguage(currentLanguage: Language): ChangeLanguageModel {
    return copy(currentLanguage = currentLanguage)
        .coerceUserSelectedLanguage()
  }

  fun withSupportedLanguages(supportedLanguages: List<Language>): ChangeLanguageModel {
    return copy(supportedLanguages = supportedLanguages)
        .coerceUserSelectedLanguage()
  }

  fun withUserSelectedLanguage(userSelectedLanguage: Language): ChangeLanguageModel {
    return copy(userSelectedLanguage = userSelectedLanguage)
  }

  private fun coerceUserSelectedLanguage(): ChangeLanguageModel {
    val isCurrentLanguageInSupportedLanguages = haveLanguagesBeenFetched && currentLanguage in supportedLanguages

    return if (isCurrentLanguageInSupportedLanguages) copy(userSelectedLanguage = currentLanguage) else this
  }

  fun restarted(): ChangeLanguageModel {
    return copy(manuallyRestarted = true)
  }
}
```

##### `Event`
[**DOC**](https://github.com/spotify/mobius/wiki/Concepts#event)

There can be one or more events for a given screen. These will be represented as a sealed class with all the different events represented as types of this sealed class. These will be consumed by the `Update` component to either change the `Model` or emit `Effect` objects.

```kotlin
sealed class ChangeLanguageEvent

data class CurrentLanguageLoadedEvent(val language: Language) : ChangeLanguageEvent()

data class SupportedLanguagesLoadedEvent(val languages: List<Language>) : ChangeLanguageEvent()

data class SelectLanguageEvent(val newLanguage: Language) : ChangeLanguageEvent()

object CurrentLanguageChangedEvent : ChangeLanguageEvent()

object SaveCurrentLanguageEvent : ChangeLanguageEvent()
```

##### `Effect`
[**DOC**](https://github.com/spotify/mobius/wiki/Concepts#effect)

There can be one or more effects for a given screen. These will be represented as a sealed class with all the different effects represented as types of this sealed class. These will be consumed by the `EffectHandler` component to either perform one-time `UI actions` or emit `Event` objects.

```kotlin
sealed class ChangeLanguageEffect

object LoadCurrentLanguageEffect : ChangeLanguageEffect()

object LoadSupportedLanguagesEffect : ChangeLanguageEffect()

data class UpdateCurrentLanguageEffect(val newLanguage: Language) : ChangeLanguageEffect()

object GoBack : ChangeLanguageEffect()

object RestartActivity : ChangeLanguageEffect()
```

##### `Update`
[**DOC**](https://github.com/spotify/mobius/wiki/Concepts#update-function)

This will be a class that implements `Update<Model, Event, Effect>`. The sole responsibility of this class is to decide the business logic (What needs to happen). This component should be [pure](https://github.com/spotify/mobius/wiki/Pure-vs-Impure-Functions) which makes testing business logic very easy.

```kotlin
class ChangeLanguageUpdate : Update<ChangeLanguageModel, ChangeLanguageEvent, ChangeLanguageEffect> {

  override fun update(
      model: ChangeLanguageModel,
      event: ChangeLanguageEvent
  ): Next<ChangeLanguageModel, ChangeLanguageEffect> {
    return when (event) {
      is CurrentLanguageLoadedEvent -> next(model.withCurrentLanguage(event.language))
      is SupportedLanguagesLoadedEvent -> next(model.withSupportedLanguages(event.languages))
      is SelectLanguageEvent -> next(model.withUserSelectedLanguage(event.newLanguage))
      is CurrentLanguageChangedEvent -> next(model.restarted(), RestartActivity)
      is SaveCurrentLanguageEvent -> dispatch(UpdateCurrentLanguageEffect(model.userSelectedLanguage!!))
    }
  }
}
```

##### `EffectHandler`
[**DOC**](https://github.com/spotify/mobius/wiki/Concepts#effect-handler)

This will be a class that provides a factory function that can build an `ObservableTransformer<Event, Effect>`. We generally use [`@AssistedInject`](https://github.com/square/AssistedInject) to construct and inject these instances since this class needs a reference to `UiActions`. This is the class which will actually be responsible for doing **actual** work and changing the state of the system (mostly I/O, sometimes UI).

```kotlin
class ChangeLanguageEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    private val settingsRepository: SettingsRepository,
    @Assisted private val uiActions: UiActions
) {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: UiActions): ChangeLanguageEffectHandler
  }

  fun build(): ObservableTransformer<ChangeLanguageEffect, ChangeLanguageEvent> {
    return RxMobius
        .subtypeEffectHandler<ChangeLanguageEffect, ChangeLanguageEvent>()
        .addTransformer(LoadCurrentLanguageEffect::class.java, loadCurrentSelectedLanguage(schedulersProvider.io()))
        .addTransformer(LoadSupportedLanguagesEffect::class.java, loadSupportedLanguages(schedulersProvider.io()))
        .addTransformer(UpdateCurrentLanguageEffect::class.java, updateCurrentLanguage(schedulersProvider.io()))
        .addAction(GoBack::class.java, uiActions::goBackToPreviousScreen, schedulersProvider.ui())
        .addAction(RestartActivity::class.java, uiActions::restartActivity, schedulersProvider.ui())
        .build()
  }

  private fun loadCurrentSelectedLanguage(scheduler: Scheduler): ObservableTransformer<LoadCurrentLanguageEffect, ChangeLanguageEvent> {
    return ObservableTransformer { effectStream ->
      effectStream
          .flatMapSingle {
            settingsRepository
                .getCurrentLanguage()
                .subscribeOn(scheduler)
          }
          .map(::CurrentLanguageLoadedEvent)
    }
  }

  private fun loadSupportedLanguages(scheduler: Scheduler): ObservableTransformer<LoadSupportedLanguagesEffect, ChangeLanguageEvent> {
    return ObservableTransformer { effectStream ->
      effectStream
          .flatMapSingle {
            settingsRepository
                .getSupportedLanguages()
                .subscribeOn(scheduler)
          }
          .map(::SupportedLanguagesLoadedEvent)
    }
  }

  private fun updateCurrentLanguage(scheduler: Scheduler): ObservableTransformer<UpdateCurrentLanguageEffect, ChangeLanguageEvent> {
    return ObservableTransformer { effectStream ->
      effectStream
          .map { it.newLanguage }
          .flatMapSingle { newLanguage ->
            settingsRepository
                .setCurrentLanguage(newLanguage)
                .subscribeOn(scheduler)
                .toSingleDefault(newLanguage)
          }
          .map { CurrentLanguageChangedEvent }
    }
  }
}
```

##### `Init`
[**DOC**](https://github.com/spotify/mobius/wiki/Concepts#starting-and-resuming-a-loop)

This will be a class that implements `Init<Model, Effect>`. This class is responsible for kicking off the initial effects when the screen is created (Load the initial set of data, etc). Note that this class is not responsible for actually **doing** the initial setup itself, but is only responsible for emitting the `Effect` instances which will then be fed into the `EffectHandler`. Like `Update`, this component should be [pure](https://github.com/spotify/mobius/wiki/Pure-vs-Impure-Functions) which makes testing business logic very easy.

We will use the following components which we have built on top of Mobius to build the screens going forward:

##### `Ui`
This is an interface that represents a contract for rendering a particular screen. Will typically be implemented by a concrete subtype of an [`Activity`](https://developer.android.com/reference/androidx/appcompat/app/AppCompatActivity), [`Fragment`](https://developer.android.com/reference/androidx/fragment/app/Fragment), or [`View`](https://developer.android.com/reference/android/view/View) as per requirement.

```kotlin
interface ChangeLanguageUi {
  fun displayLanguages(supportedLanguages: List<Language>, selectedLanguage: Language?)
  fun setDoneButtonDisabled()
  fun setDoneButtonEnabled()
}
```

##### `UiRenderer`
This is a class that is responsible for rendering `Model` objects on the screen. These will be classes that implement the `ViewRenderer<M>` interface.

```kotlin
class ChangeLanguageUiRenderer(val ui: ChangeLanguageUi) : ViewRenderer<ChangeLanguageModel> {

  override fun render(model: ChangeLanguageModel) {
    if (model.haveLanguagesBeenFetched) {
      ui.displayLanguages(model.supportedLanguages, model.userSelectedLanguage)
      toggleDoneButtonEnabledState(model.userSelectedLanguage)
    }
  }

  private fun toggleDoneButtonEnabledState(userSelectedLanguage: Language?) {
    if (userSelectedLanguage == null) {
      ui.setDoneButtonDisabled()
    } else {
      ui.setDoneButtonEnabled()
    }
  }
}
```

##### `UiActions`
This is an interface that is responsible for one-off actions related to the `Ui` (navigating to another screen, showing an alert, etc). From an implementation perspective, this will generally be the same class that implements `Ui`, but we separate it out because from an *intention* perspective, the two are completely different.

```kotlin
interface UiActions {
  fun goBackToPreviousScreen()
  fun restartActivity()
}
```

### Wiring it all together
We have implemented a class, [`MobiusDelegate`](https://github.com/simpledotorg/simple-android/blob/6da548b36c3cceb3e3db344c09a0f5ae588fc2c0/mobius-base/src/main/java/org/simple/clinic/mobius/MobiusDelegate.kt), which is a light wrapper around `Mobius` and `RxJava`, and is designed to encapsulate the work of tying the `Mobius` loop to the Android component lifecycle.

```kotlin
class ChangeLanguageScreen(
    context: Context,
    attributeSet: AttributeSet
) : ConstraintLayout(context, attributeSet), ChangeLanguageUi, UiActions {

  @Inject
  lateinit var schedulersProvider: SchedulersProvider

  @Inject
  lateinit var activity: AppCompatActivity
  
  @Inject
  lateinit var effectHandlerFactory: ChangeLanguageEffectHandler.Factory

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
    MobiusDelegate.forView(
        events = events,
        defaultModel = ChangeLanguageModel.FETCHING_LANGUAGES,
        init = ChangeLanguageInit(),
        update = ChangeLanguageUpdate(),
        effectHandler = effectHandlerFactory.create(this).build(),
        modelUpdateListener = uiRenderer::render
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
```

This class also has an alternate factory function, `MobiusDelegate#forActivity`, which is meant to be used when the screen being built is an independent `Activity` or `Fragment` instead of a custom `View`. This was done because of the differences in how a `View` and an `Activity` manage their state saving and restoration.

A future plan is to unify these two factories in some fashion. An upcoming release of `Mobius` with support for the Android Architecture Components is in active development and this might be a possible solution when it's ready.

### Testing
Testing a screen built with Mobius involves testing three (sometimes four) components independently. These are the `Update`, `EffectHandler`, `UiRenderer`, and (optionally) `Init` implementations.

While it is true that we are testing more things now compared to the earlier v1 and v2 architecture, the difference here is that the tests (and the components) themselves are significantly smaller and more focused. This means that introducing changes is far less likely to affect  components and tests that are not related to the thing we are changing and maintenance becomes significantly easier.

A reference implementation of testing these four components can be found at [this commit](https://github.com/simpledotorg/simple-android/tree/e51a6b2fad295f9da4064b95f51d95e3ab2f0e6c/app/src/test/java/org/simple/clinic/settings/changelanguage).

#### Testing business logic
Testing business logic involves testing the `Update` and `Init` classes. `Mobius` provides an artifact, `mobius-test`, which contains useful classes and assertions for testing them.

The testing artifacts allow us to describe tests in a [`Given-When-Then`](https://www.martinfowler.com/bliki/GivenWhenThen.html) fashion. The workflow for testing them will generally look like this:

###### *Given* a situation
There are two classes, `UpdateSpec` and `InitSpec`, both of which allow for setting up a model in a given state.

```kotlin
val spec = UpdateSpec<ChangeLanguageModel, ChangeLanguageEvent, ChangeLanguageEffect>(ChangeLanguageUpdate())

spec.given(ChangeLanguageModel.FETCHING_LANGUAGES)
```

###### *When* something happens
The `UpdateSpec` class has two methods, `whenEvent` and `whenEvents`, which let us forward one more events to the `Update` loop and simulate actual events that would happen.

```kotlin
spec
  .given(ChangeLanguageModel.FETCHING_LANGUAGES)
  .whenEvent(CurrentLanguageLoadedEvent(englishIndia))
```

This step is skipped when testing `Init` classes since they do not process events and just emit effects for a given `Model`.

###### *Then* something else should happen
The `Spec` classes provide two methods, `then` and `thenError`, in order to verify behaviour that we expect should happen. Something to note here is that while the `thenError` method exists, we should never use it because we model errors as types which become part of the loop.

The `Spec` class also provide `NextMatchers` and `FirstMatchers` which we can use to conveniently verify that the loop behaves as expected. These include methods to:

- Verify that no change to the model happened (`hasNoModel`)
- Verify that the model was changed in a specific way (`hasModel`)
- Verify that one or more effects happened (`hasEffects`)
- Verify that no effects happened (`hasNoEffects`)

```kotlin
spec
  .given(defaultModel)
  .whenEvent(CurrentLanguageLoadedEvent(englishIndia))
  .then(assertThatNext(
    hasModel(defaultModel.withCurrentLanguage(englishIndia)),
    hasNoEffects()
  ))
```

Following is a sample of a complete test for an `Update` class.

```kotlin
class ChangeLanguageUpdateTest {

  private val defaultModel = ChangeLanguageModel.FETCHING_LANGUAGES

  private val englishIndia = ProvidedLanguage(displayName = "English", languageCode = "en_IN")
  private val hindiIndia = ProvidedLanguage(displayName = "हिंदी", languageCode = "hi_IN")

  private val spec = UpdateSpec<ChangeLanguageModel, ChangeLanguageEvent, ChangeLanguageEffect>(ChangeLanguageUpdate())

  @Test
  fun `when the current language is loaded, the ui must be updated`() {
    spec
        .given(defaultModel)
        .whenEvent(CurrentLanguageLoadedEvent(englishIndia))
        .then(assertThatNext(
            hasModel(defaultModel.withCurrentLanguage(englishIndia)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when the supported languages are loaded, the ui must be updated`() {
    val supportedLanguages = listOf(englishIndia, hindiIndia)

    spec
        .given(defaultModel)
        .whenEvent(SupportedLanguagesLoadedEvent(supportedLanguages))
        .then(assertThatNext(
            hasModel(defaultModel.withSupportedLanguages(supportedLanguages)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when the user selects a language, the ui must be updated`() {
    val model = defaultModel
        .withCurrentLanguage(englishIndia)
        .withSupportedLanguages(listOf(englishIndia, hindiIndia))

    spec
        .given(model)
        .whenEvent(SelectLanguageEvent(hindiIndia))
        .then(assertThatNext(
            hasModel(model.withUserSelectedLanguage(hindiIndia)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when the current language is changed, then restart the activity`() {
    val model = defaultModel
        .withCurrentLanguage(englishIndia)
        .withSupportedLanguages(listOf(englishIndia, hindiIndia))

    spec
        .given(model)
        .whenEvent(CurrentLanguageChangedEvent)
        .then(assertThatNext(
            hasModel(model.restarted()),
            hasEffects(RestartActivity as ChangeLanguageEffect)
        ))
  }

  @Test
  fun `when the user clicks save, the current language must be set to the user selected language`() {
    val model = defaultModel
        .withCurrentLanguage(englishIndia)
        .withSupportedLanguages(listOf(englishIndia, hindiIndia))
        .withUserSelectedLanguage(hindiIndia)

    spec
        .given(model)
        .whenEvent(SaveCurrentLanguageEvent)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(UpdateCurrentLanguageEffect(hindiIndia) as ChangeLanguageEffect)
        ))
  }
}
```

#### Testing effects
Testing changes to the system will usually done by testing two components:

###### `UiRenderer`
This will be used to verify that the right methods are called on the `Ui` interface by the `UiRenderer`, *given* a specific `Model`. We usually will mock the `Ui` interface using `Mockito`, render a `Model`, and then verify the methods invoked on the mock.

```kotlin
class ChangeLanguageUiRendererTest {

  private val ui = mock<ChangeLanguageUi>()
  private val defaultModel = ChangeLanguageModel.FETCHING_LANGUAGES

  private val englishIndia = ProvidedLanguage(displayName = "English", languageCode = "en_IN")
  private val hindiIndia = ProvidedLanguage(displayName = "हिंदी", languageCode = "hi_IN")
  private val supportedLanguages = listOf(englishIndia, hindiIndia)

  private val renderer = ChangeLanguageUiRenderer(ui)

  @Test
  fun `when the model is being initialized, do nothing`() {
    // when
    renderer.render(defaultModel)

    // then
    verifyZeroInteractions(ui)
  }

  @Test
  fun `when the current language has not been fetched, do nothing`() {
    // given
    val model = defaultModel
        .withSupportedLanguages(listOf(englishIndia, hindiIndia))

    // when
    renderer.render(model)

    // then
    verifyZeroInteractions(ui)
  }

  @Test
  fun `when the supported languages have not been fetched, do nothing`() {
    // given
    val model = defaultModel
        .withCurrentLanguage(englishIndia)

    // when
    renderer.render(model)

    // then
    verifyZeroInteractions(ui)
  }

  @Test
  fun `if the user has selected a language, render the ui with the selected language`() {
    // given
    val model = defaultModel
        .withSupportedLanguages(supportedLanguages)
        .withCurrentLanguage(englishIndia)

    // when
    renderer.render(model)

    // then
    verify(ui).displayLanguages(supportedLanguages, englishIndia)
    verify(ui).setDoneButtonEnabled()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `if the user is using the system default language, render the ui without any selected language`() {
    // given
    val model = defaultModel
        .withCurrentLanguage(SystemDefaultLanguage)
        .withSupportedLanguages(supportedLanguages)

    // when
    renderer.render(model)

    // then
    verify(ui).displayLanguages(supportedLanguages, null)
    verify(ui).setDoneButtonDisabled()
    verifyNoMoreInteractions(ui)
  }
}

```

###### `EffectHandler`

The `EffectHandler` is responsible for changing the state of the system in two ways:

- Interact with the real world in some fashion (I/O, Hardware, etc) and emit `Event` instances which are fed back into the `Update` loop.
- Perform one-time actions on the user interface via `UiActions`

Both of these will be tested in different ways. For the former, we have implemented a helper class called `EffectHandler` test case which we will use to assert that specific effects result in emitting zero or more events. We also use `Mockito` here to stub out external dependencies.

```kotlin
private val testCase = EffectHandlerTestCase(ChangeLanguageEffectHandler.create(
      schedulersProvider = TrampolineSchedulersProvider(),
      settingsRepository = settingsRepository,
      uiActions = uiActions
))

testCase.dispatch(LoadSupportedLanguagesEffect)

testCase.assertOutgoingEvents(SupportedLanguagesLoadedEvent(supportedLanguages))
```

The latter case is tested by creating a mock instance of the `UiActions` interface, and then verifying that specific methods on this mock were called when certain effects are dispatched to the `EffectHandler`.

```kotlin
private val testCase = EffectHandlerTestCase(ChangeLanguageEffectHandler.create(
      schedulersProvider = TrampolineSchedulersProvider(),
      settingsRepository = settingsRepository,
      uiActions = uiActions
))

testCase.dispatch(RestartActivity)

verify(uiActions).restartActivity()
```

Following is a sample of a complete test for an `EffectHandler` class.

```kotlin
class ChangeLanguageEffectHandlerTest {

  private val settingsRepository = mock<SettingsRepository>()
  private val uiActions = mock<UiActions>()

  private val testCase = EffectHandlerTestCase(ChangeLanguageEffectHandler.create(
      schedulersProvider = TrampolineSchedulersProvider(),
      settingsRepository = settingsRepository,
      uiActions = uiActions
  ))

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `the current selected language must be fetched when the load current selected effect is received`() {
    // given
    val selectedLanguage = SystemDefaultLanguage
    whenever(settingsRepository.getCurrentLanguage()).doReturn(Single.just<Language>(selectedLanguage))

    // when
    testCase.dispatch(LoadCurrentLanguageEffect)

    // then
    testCase.assertOutgoingEvents(CurrentLanguageLoadedEvent(selectedLanguage))
  }

  @Test
  fun `the list of supported languages must be fetched when the load supported languages effect is received`() {
    // given
    val supportedLanguages = listOf(
        SystemDefaultLanguage,
        ProvidedLanguage(displayName = "English", languageCode = "en_IN"),
        ProvidedLanguage(displayName = "हिंदी", languageCode = "hi_IN")
    )
    whenever(settingsRepository.getSupportedLanguages()).doReturn(Single.just(supportedLanguages))

    // when
    testCase.dispatch(LoadSupportedLanguagesEffect)

    // then
    testCase.assertOutgoingEvents(SupportedLanguagesLoadedEvent(supportedLanguages))
  }

  @Test
  fun `when the update current language effect is received, the current language must be changed`() {
    // given
    val changeToLanguage = ProvidedLanguage(displayName = "हिंदी", languageCode = "hi_IN")
    whenever(settingsRepository.setCurrentLanguage(changeToLanguage)).doReturn(Completable.complete())

    // when
    testCase.dispatch(UpdateCurrentLanguageEffect(changeToLanguage))

    // then
    testCase.assertOutgoingEvents(CurrentLanguageChangedEvent)
  }

  @Test
  fun `when the go back to previous screen effect is received, the go back ui action must be invoked`() {
    // when
    testCase.dispatch(GoBack)

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).goBackToPreviousScreen()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when the restart activity effect is received, the restart activity ui action must be invoked`() {
    // given
    testCase.dispatch(RestartActivity)

    // when
    testCase.assertNoOutgoingEvents()

    // then
    verify(uiActions).restartActivity()
    verifyNoMoreInteractions(uiActions)
  }
}
```
