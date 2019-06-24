# Refactoring : In-Progress Towards Mobius
**Note:** This is a WIP doc until we figure out what the new arch finally looks like. At that point, it will become an ADR.
## Reasons

- We currently recreate application state from the UI state, which is dependent on some hidden behaviour
	- RxBinding has initial value observables that emit the current event immediately.
	- All widgets on the screen save and restore state properly.
- The entire event stream is replayed because the system is setup in a way that they begin emission of events like ScreenCreated before the entire event handling loop is setup.
- Controller handles both the business logic and the view logic.

## Changes

- Define a view state for whatever view/screen is being built
- Create a view state producer which is an ObservableTransformer<UiEvent, UiState>. This is responsible for business logic.
- Create a view change producer which is an ObservableTransformer<UiState, (Ui) -> Unit>. This is responsible for presentation logic.
- Create a controller which is an ObservableTransformer<UiEvent, (Ui) -> Unit> which will compose the ui state producer and ui change producer internally.

### Example
```kotlin
	class AllPatientsInFacilityUiController @Inject constructor(
    	private val uiStateProducer: AllPatientsInFacilityUiStateProducer,
    	private val uiChangeProducer: AllPatientsInFacilityUiChangeProducer
	) : ObservableTransformer<UiEvent, AllPatientsInFacilityUiChange> {

	  override fun apply(uiEvents: Observable<UiEvent>): ObservableSource<AllPatientsInFacilityUiChange> {
	    return uiEvents
	        .compose(uiStateProducer)
	        // This needs to happen here because we need only one subscription to happen
	        // downstream.
	        .share()
	        .compose(uiChangeProducer)
	  }
	}
```

### Opt in for Replay (?)
In legacy controllers, they are responsible for replaying events by using `ReplayUntilScreenIsDestroyed()` internally before setting up business logic.

In the new architecture, controllers are no longer responsible for this and should be setup externally. In order to facilitate this, the `bindUiWithController` glue method has been modified to accept a `replayTransformer` parameter which has a no-op default value. Controllers which follow the new architecture should opt into event replaying by using the `ReplayUiEventsTransformer` class.
