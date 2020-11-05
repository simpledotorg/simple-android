# Recipes
This document shows how we are handling some commonly used use-cases like feature flags, Dagger `Qualifier`s, etc.,

## Feature Flags
We define features using enums, and provide a default value.

```kotlin
enum class Feature(
    val enabled: Boolean,
    val remoteConfigKey: String = ""
) {
  SecureCall(false, "secure_call"),
  Telemedicine(true, "telemedicine_enabled")
}
```

We check if a feature is enabled or not, using `Features` class.  Since this class is part of Dagger graph we can inject it.

```kotlin
fun call() {
	if (features.isEnabled(SecureCall))) {
		// Handle code
	}
}
```

Overriding this value in tests is very simple.

```kotlin
@Test
fun `the feature is overriden in this test`() {
  Features.overrides[Feature.SecureCall] = true
}
```

You can read more about this usage [here](https://github.com/simpledotorg/simple-android/issues/1364)
