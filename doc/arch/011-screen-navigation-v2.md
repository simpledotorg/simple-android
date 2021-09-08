# ADR 011: Screen Navigation (v2)

## Status

Accepted (Supersedes [002](./002-bottom-sheets.md), [005](./005-single-activity-view-based-navigation.md)) on 2020-01-12

## Context

We currently use a navigation framework built on top of [Flow](./005-single-activity-view-based-navigation.md) in order to provide a single-activity
navigation setup where individual screens are built using [`android.view.View`](https://developer.android.com/reference/android/view/View) instances.
This approach works well for most use cases, but it has a bunch of issues.

### No in-built support for modals

The framework does not have in-built support for modals (bottom sheets, dialogs, etc). We currently have workarounds (
See [ADR #002](https://github.com/simpledotorg/simple-android/blob/master/doc/arch/002-bottom-sheets.md)), but the app has grown increasingly reliant
on bottom sheets and using the workaround for them is tedious and adds a lot of friction in building screens.

A good example being that moving the app to a different screen based on the result selected in a bottom sheet requires us to open the bottom sheet
using the [`Start activity for result`](https://developer.android.com/reference/android/app/Activity#StartingActivities) flow and reading the result
instead of directly loading the screen that is required.

### No in-built support for the Lifecycle component

We currently use some libraries and frameworks that depend on the
Android [Lifecycle](https://developer.android.com/topic/libraries/architecture/lifecycle) component. This is officially supported only
in [Activities](https://developer.android.com/reference/android/app/Activity)
and [Fragments](https://developer.android.com/guide/components/fragments), but not
in [Views](https://developer.android.com/reference/android/view/View), which is what our current framework is built on. We do have some workarounds
available, but it will be increasingly harder to integrate with libraries that depend on this component the longer we wait.

### Opening a screen and expecting a result

When it comes to screens that are expected to send a result back to the previous screen, the current framework falls woefully short. It is currently
implemented using a [`io.reactivex.subjects.PublishSubject`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/subjects/PublishSubject.html), which
can sometimes lead to issues where results are not delivered correctly based on subtle race conditions when used in conjunction with the bottom sheets
described earlier.

### Layout editor previews are broken

Our current framework is setup to expect our individual screens to be added to the XML layout file and inflated by the system. While this is easy, it
makes building non-trivial UIs hard because the editor framework cannot instantiate classes that our screen depends on that are normally initialized
by the dependency injection framework. This requires us to deploy on a real device for testing UI, which increases the time of the feedback loop.

#### Overall impact

Our current navigation framework is ill-suited to build complex navigation flows, especially those which involve modals like bottom sheets. The
application at the moment is heavily reliant on the use of bottom sheets, and adds additional complexity whenever we have to work with navigation
flows which incorporate them since it requires us to implement several workarounds in order to support the correctly.

## Goals

We need a new navigation framework, one that is better suited for our needs. It needs to fulfill the following requirements:

1. Have in-built support for modals (bottom sheets and dialogs). Ideally, there should be minimal difference between using a modal vs a normal screen
2. Support the Android Lifecycle component
3. Support opening screens and returning a result without requiring workarounds
4. Support overriding back presses at the screen/modal level
5. Support layout editor previews in the IDE
6. Be easy to incrementally migrate from our current navigation architecture without requiring a lot of upfront work

## Solution

We will build a new navigation framework based on [Fragments](https://developer.android.com/guide/components/fragments), inspired
by [simple-stack](https://github.com/Zhuinden/simple-stack), and designed to work with our screen architecture (
See [ADR #008](https://github.com/simpledotorg/simple-android/blob/master/doc/arch/008-screen-architecture-v3.md)).

A high level description of the framework and its core components are described below:

### Framework

The framework overall will have three primary components:

#### `ScreenKey`

This will be an abstract class that will need to be inherited by all classes that represent individual screens, similar to the current implementation.
This will need to be `Parcelable` so that we can save and restore the backstack when the activity state restoration mechanism is triggered.

#### `History`

This is a class that represents the overall backstack. This will encapsulate a list of `ScreenKey` instances and expose some helper methods to
manipulate the backstack. This will also need to be `Parcelable` so that we can save and restore it as necessary.

#### `Router`

This is the class that will be used externally to push and pop screens from the backstack. This will manage converting the `History` into fragment
transactions that will then be applied in order to update the UI.

### Usage

Instantiating a `Router` is as simple as

```kotlin
val router = Router(
  FirstScreen.Key(), // Initial screen
  supportFragmentManager,
  R.id.content // Reference to the fragment container viewgroup
)
```

Pushing a new screen onto the stack can be performed using the `push()` method.

```kotlin
nextScreenButton.setOnClickListener { router.push(SecondScreen.Key()) }
```

Going to the previous screen is done using the `pop()` method.

```kotlin
previousScreenButton.setOnClickListener { router.pop() }
```

#### Building a screen

Implementing a screen is quite simple, it's a standard fragment.

```kotlin
class FirstScreen : Fragment(R.layout.screen_navigation_first) {

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    // Set up UI events here
  }

  @Parcelize
  object Key : ScreenKey() {

    override fun instantiateFragment(): Fragment {
      return FirstScreen()
    }
  }
}
```

Passing arguments to a screen is quite easy, as the framework puts the instantiated `ScreenKey` instance as a fragment argument with the argument
key, `ScreenKey#ARGS_KEY`. This can be easily retrieved from within the fragment by a standard arguments call.

#### Overriding back presses

The framework provides an interface `HandlesBack`. Any screen which wishes to override the default back behaviour (popping the stack) can implement
this and handle back presses as required. This works for both normal screens as well as modals.

```kotlin
class SecondScreen : Fragment(R.layout.screen_navigation_second), HandlesBack {

  private val router: Router by unsafeLazy {
    (requireActivity() as NavigationTestActivity).router
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    nextScreenButton.setOnClickListener { router.push(ThirdScreen.Key()) }
  }

  override fun onBackPressed(): Boolean {
    router.push(ConfirmationDialog.Key())
    return true
  }

  @Parcelize
  class Key : ScreenKey() {

    override fun instantiateFragment(): Fragment {
      return SecondScreen()
    }
  }
}
```

#### Opening screens for results

The `Router` provides two methods, `pushExpectingResult()` and `popWithResult()`, which should be used with the `ExpectsResult` interface in order to
implement a request <-> response flow.

##### Screen which accepts a result

```kotlin
class ThirdScreen : Fragment(R.layout.screen_navigation_third), ExpectsResult {

  private val router: Router by unsafeLazy {
    (requireActivity() as NavigationTestActivity).router
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    confirmScreenButton.setOnClickListener { router.pushExpectingResult(EnterText, TextEntrySheet.Key()) }
  }

  override fun onScreenResult(requestType: Parcelable, result: ScreenResult) {
    if (requestType == EnterText && result is Succeeded) {
      val enteredText = TextEntrySheet.readEnteredText(result)

      thirdScreenLabel.text = thirdScreenLabel.text.toString() + " : " + enteredText
    }
  }

  @Parcelize
  class Key : ScreenKey() {

    override fun instantiateFragment(): Fragment {
      return ThirdScreen()
    }
  }

  @Parcelize
  object EnterText : Parcelable
}
``` 

##### Screen which returns the result to the previous one

```kotlin
class TextEntrySheet : BottomSheetDialogFragment() {

  companion object {
    fun readEnteredText(result: Succeeded): String {
      return (result.result as ResultData).text
    }
  }

  private val router: Router by unsafeLazy {
    (requireActivity() as NavigationTestActivity).router
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setStyle(DialogFragment.STYLE_NORMAL, R.style.Clinic_V2_Theme_BottomSheetFragment)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.screen_navigation_textentrysheet, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    doneButton.setOnClickListener { router.popWithResult(Succeeded(ResultData(textEntryField.text.toString()))) }
  }

  @Parcelize
  private data class ResultData(val text: String) : Parcelable

  @Parcelize
  class Key : ScreenKey() {
    override fun instantiateFragment(): Fragment {
      return TextEntrySheet()
    }

    override val type: ScreenType
      get() = ScreenType.Modal
  }
}
```

##### Support for modals

Marking a particular screen as a modal is as easy as overriding the `screenType` property on the `ScreenKey` implementation and
returning `ScreenType.Modal` as the value.

```kotlin
class TextEntrySheet : BottomSheetDialogFragment() {

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.screen_navigation_textentrysheet, container, false)
  }

  @Parcelize
  class Key : ScreenKey() {
    override fun instantiateFragment(): Fragment {
      return TextEntrySheet()
    }

    override val type: ScreenType
      get() = ScreenType.Modal
  }
}
```

##### Incremental migration from the current architecture

Since our current screens are built using `android.view.View` instances and this framework uses `androidx.fragment.app.Fragment` instances, we need an
intermediate solution where we can switch to using this framework for new screens while the older screens continue to function with minimal changes.
In order to facilitate this, the framework will provide a wrapper fragment `ScreenWrapperFragment`, which will accept an older `FullScreenKey` as a
property and embed the screen within itself.

The only change that will need to be done is the way that individual screens get access to the screen key for any screen arguments. These screens will
need to use the [static method](https://developer.android.com/reference/androidx/fragment/app/FragmentManager#findFragment(android.view.View))
provided by `FragmentManager` in order to get the fragment instance which encapsulates this view and then get the screen key through the arguments of
that fragment instance. We might choose to provide helper methods to do this instead of having to do this in every screen, but it's fairly
straightforward.

There's a particular use case which would require us to move our screens to fragments, however. If we have a situation where we auto-submit values in
a text field based on a given length (like the PIN entry screens), we would need to migrate these screens specifically. This is needed because of the
way that screen state restoration works with the current screen architecture does not play well together when it is wrapped in a fragment. Apart from
this use case, we can migrate the other screens as and when required.

##### Misc

Comparing the usage to the goals that we've defined earlier, the only ones that we haven't considered are the following.

1. Support for the layout editor preview
2. Support for the Lifecycle component

Both of these are given to us for free with this screen architecture because they are based on fragments. Our layout resource file is provided to the
fragment via the constructor and will not require us to embed custom classes within layout resource files anymore. In addition, since it's based on
fragments, we can get access to the default lifecycle provided by the framework and use that when needed.

A proof of concept implementation is
available [HERE](https://github.com/simpledotorg/simple-android/tree/custom-nav/app/src/main/java/org/simple/clinic/navigation).

## Consequences

### Fragment navigation

Fragments are notoriously complicated, given their additional lifecycle decoupled from the one of the parent activity as well as the separate
lifecycle for the fragment instance and the child view. However, I feel it's okay to start using them for a couple of reasons:

1. We do not work with the navigation directly. The `Router` instance will encapsulate the complexities related to fragment transactions and all
   screens that we write will be enforced to participate in that flow that we enforce.
2. The Android tools team is planning to simplify the fragment lifecycle over time, so we can opt into the simpler fragment lifecycle as and when it
   is available.

### Additional maintenance

The code that we write to handle the navigation ourselves is additional code that we need to maintain. However, if we consider the additional
boilerplate that we have to write to deal with all the problems of the current architecture, this should be significantly lesser effort to maintain.

## Alternatives

We did try out the Android recommended way to handle navigation,
the [Navigation Component](https://developer.android.com/guide/navigation/navigation-getting-started), in
a [small section](https://github.com/simpledotorg/simple-android/tree/9d043b61401e3c950bd17f15175fa1041d84aaf6/app/src/main/java/org/simple/clinic/setup)
of the project, but decided not to go with it for a couple of reasons:

### Navigation state is defined in multiple places

We need to create a
navigation [XML file](https://github.com/simpledotorg/simple-android/blob/9d043b61401e3c950bd17f15175fa1041d84aaf6/app/src/main/res/navigation/setup_activity_nav_graph.xml)
that defines navigation destinations and actions and then use those actions
to [trigger navigation flows](https://github.com/simpledotorg/simple-android/blob/9d043b61401e3c950bd17f15175fa1041d84aaf6/app/src/main/java/org/simple/clinic/splash/SplashScreen.kt#L34)
in the code.

The problem with doing it in this way is that navigation behaviour is defined in two different places and it is hard to get the entire picture of what
is happening at the call site. The trade-off is that we lose the ability to get a visual representation of the entire screen navigation graph, but
this is an acceptable trade-off given the simpler navigation code.

### No support for opening a destination and getting a result

The navigation framework does not support opening a destination and waiting for a result from it. The official recommendation is to use
the [`ViewModel` architecture component](https://developer.android.com/topic/libraries/architecture/viewmodel), but we have decided not to use this
component for this project. Adding support for this would require additional workarounds and we could rather spend that effort in creating a framework
that works for our needs.
