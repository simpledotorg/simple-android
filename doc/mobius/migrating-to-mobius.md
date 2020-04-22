# Migrating to Mobius

This document outlines the process and methodology we use to migrate from our [v1 screen architecture](../arch/001-screen-controllers.md) to our [v3 architcture](../arch/008-screen-architecture-v3.md), which is based on [Mobius](https://github.com/spotify/mobius), a reactive state management framework by Spotify.

The overall method we use to migrate to Mobius is similar to the **`StranglerFigApplication`** pattern detailed by Martin Fowler [here](https://martinfowler.com/bliki/StranglerFigApplication.html).

## Overall process
The high-level process to migrate a screen to our Mobius based architecture consists of three stages:

### Stabilize
This stage is where we prepare the controller in order for easier migration. We do this for a couple of reasons:

- Many of our older tests are not written very well and don't give us good information when we refactor.
- We overuse mock stubs in tests and have overly permissive stubs and verifications (using `Mockito#any()`, for example).

#### Steps
1. Wherever possible, use exact values for setting up stubs in tests instead of `any()`.
2. Reduce the amount of non-determinism in the tests. Especially for things like `UUID` instances, replace usages for random `UUID` creations with actual, static values.
3. Wherever possible, use actual objects instead of setting up stubs. For example, if a controller test uses a stub validator object for verifying behaviour on validation failures, replace the stubs with actual validator objects.
	- Ideally, the only stubs that should be used are stubs used to mock out platform details (I/O, Hardware, etc).
	- An exception could be cases where the actual objects are difficult to use in tests (because they might be, say, dependent on time and don't let us pass in a `Clock` so we can override in tests). In this case, if it's easy to make this change, do it and replace the stub with the actual object.
	- If it's too invasive to change the dependency to support using it in tests, then:
       - Make a copy of the entire controller test class.
       - Delete all the tests in the copy except the ones which need the dependency to be mocked.
       - Delete the tests which are left in the copy from the original test file. Use the actual dependency in this test class and the mocked dependency in the other test class.
4. Setup the controller in each test instead of in the `@Before` fixture. Add a method called `setupController` which instantiates the controller and subscribes to the event stream, and then invoke this in each test method after setting up the stubs.
5. If there are tests that have conditional assertion blocks, break them out into different tests (one for each conditional branch) that assert one idea each.
6. Verify that as much coverage is present before proceeding to the next stage.
   - Comment out **ONE** flow setup method in the `.apply()` method of the controller and run the tests and verify that some of the tests have broken.
   - If no tests have broken, add tests to capture the existing flow.
   - If some tests have broken, skim through them and verify that the current behaviour is captured completely. If it's not, add tests to capture it.
   - Repeat the above steps for all the flow setup methods in the controller.
7. If the controller accepts runtime dependencies via a custom `ScreenCreated` type, change the controller to accept the dependencies via `@AssistedInject` and remove the custom screen created event.

### Strangulate
This stage is where we break up the controller into the Mobius components. There are three sub-phases here:

#### Prepare the controller
1. Extract all screen functions invoked by the controller into a separate interface. Change the controller and the tests to only be aware of the interface and not the concrete screen type.
2. Add all the Mobius classes into the package with no-op implementations.
   - When creating the `Event` type, make it implement the `UiEvent` marker interface.
   - When the creating the `Model` type, make it `Parcelable`.
3. Set up the Mobius loop in the controller tests
   -  Construct a `MobiusTestFixture` and set it up in the `@Before` test fixture. Dispose it in the `@After` fixture.
   -  Add a method `startMobiusLoop` and invoke `MobiusTestFixture#start` in there.
   -  Wherever the `setupController` method is being used, also add the `startMobiusLoop` method.
4. Integrate the Mobius loop in the screen
   -  Extract the event stream in the screen to a `lazy` delegated property.
   -  Move the composition of the `ReportAnalyticsEvents` from the controller to this stream in the screen.
   -  Share this stream using `Observable#share()`
   -  Add the `MobiusDelegate` to the screen and hook into the lifecycle methods. Pass `events.ofType()` as events to the delegate.
5. If the UI interface includes one-off methods (navigating to another screen, showing a pop-up, etc), create a blank `UiActions` interface and implement this on the UI interface. This class should also be added as a dependency on the effect handler.

Run all tests in the screen (from the package if the test class has been split into multiple) now to ensure that nothing has been broken. If it has, investigate and fix before proceeding.

#### Migrate the logic
> It is critical here to *NOT* attempt to refactor the effect handler or the Mobius update loop during the process of migration. If you see duplication when migrating the methods, let it remain there. We will clean up and refactor everything in the end after all the flows are moved.

1. Pick one method from the UI interface to move to Mobius.
2. Find usages of this method in the controller.
   - If there is one single flow which is invoking it, comment the flow out from the controller's `apply` method.
   - If the flow itself invokes multiple methods on the UI interface, only comment out the parts of the flow where the method is invoked.
   - If multiple flows invoke this method, comment out the usages of this method.
3. Run tests now and take a note of which ones break, we will migrate the functionality and make these tests pass one by one.
   - One important thing to note here is that under no circumstances should an existing test be changed while the refactoring is happen.
   - Exceptions can be made for this, talk to someone else and be 100% sure that you know why you're changing a test during the process.
4. If the method is a one-off method(like navigating to a different screen, showing a dialog box, etc.), move it to the previously defined `UiActions` interface.
   - Make `Ui` interface implement `UiActions` if not done already.
   - Run the tests again. This should end up breaking the controller tests since the implementation is still missing for the one-off method call.
   - Add the implementation in `EffectHandler#uiActions` in tests as a anonymous class and invoke the method on the mock of `Ui` in order to let compilation continue.
5. Look at the events which trigger this method. Move it to the mobius event file and make it extend the sealed event class.
6. Look at the flows which trigger this method. The flows will be reaching into the Rx event streams (generally using `combineLatest` or `withLatestFrom`) to fetch the data that is needed to trigger this method. The data that is required to trigger these should be moved into the `Model`.
    - Look at the events that are generating this data. Move these to the Mobius loop and generate the data in the model (either in `Init` or `Update`, depending on what kicks off the data generation).
	- If the data is required in other flows, add a `() -> Model` supplier function to the controller as an `@Assisted` parameter.
	- In the screen, provide the current latest model via the supplier function whenever it is invoked via the `MobiusDelegate`.
	- In the controller, instead of using the Rx streams to fetch this data (which is now present in the `Model`), use the injected supplier function to get access to the current `Model` and read this data. This should only be done for existing flows which are not getting migrated.
7. Once the minimum amount of data required to trigger the method is moved into the `Model`, we can move the rendering/one-off UI logic.
	- If the method is a screen rendering method, move this rendering to the `UiRenderer`.
	- If the method is a one-off action, add an `Effect` and a corresponding handler in the `EffectHandler` and move the method invocation over. Trigger the `Effect` in `Update` to close the loop.
    - In tests, replace the anonymous class `EffectHandler#uiActions` with the mock of `Ui`.
8. Run tests again and verify that they pass.
	- If the tests fail, examine **why** they failed first before trying to change the tests. Be absolutely sure that the tests need to be changed before changing them. Usually, it will be because something was missed while the refactoring happened.
9. Once the tests pass, remove the commented out code from the controller and commit the changes.
	-  If all the flows have been migrated, proceed to the [clean-up](#clean-up).
	-  Otherwise, go back to #1.

#### Clean-up
Now that we have migrated all the code to Mobius, we can delete the older code.

1. Delete the controller from the tests and the `setupController` method.
2. Stop extending `UiActions` on the `Ui` interface.
3. Change the tests to verify one-off actions on the `UiActions` mock and replace the implementation passed to the `EffectHandler` with the mock.
4. Remove the controller from the screen.
5. Stop sharing the event stream in the screen.
6. Delete the controller class.
7. If the screen has a custom saved state that is being persisted, it might be prudent to move this saved state into the `Model`.
8. We can also delete the event stream that sets up the `ScreenCreated` events since that will be handled by the `Init` function.
9. Replace the `Controller` prefix in the older test classes with `Logic`.

### Sterilize
This is where we do the "final" refactoring, so to speak.

1. When we migrated the flows to Mobius, we may not have migrated them in a *Mobius-idiomatic* way. This is where we can attempt to do this.
2. If there is any duplication in the `Update` or `EffectHandler` sections, we can refactor them here.

## Maintaining the migrated screen
1. The older tests will not be deleted, but they should also only be maintained minimally.
2. When new features are added to the screen, write them in the Mobius style.
3. If a screen is being refactored and the older regression tests are getting in the way, add new regression tests in the Mobius style for the feature that is being refactored, delete the older regression tests, and proceed with the refactoring.
