# ADR 010: Dagger Qualifier Migration

## Status:

Accepted on 2020-08-05

## Context:

We have been using `@Named` annotation to provide dependencies that have same return type.

```kotlin
// Providing dependency
@Provides
@Named("full_date")
fun provideDateFormatterForFullDate(locale: Locale): DateTimeFormatter = DateTimeFormatter.ofPattern("d-MMM-yyyy", locale)

@Provides
@Named("date_for_user_input")
fun provideDateFormatterForUserInput(locale: Locale): DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", locale)

// Injecting dependency
@Inject
@Named("full_date")
private lateinit var dateFormatter: DateTimeFormatter
```

This is problematic because this is not a type-safe approach and prone to developer error.

It is possible to have a typo in `@Named` value when providing and injecting the dependency, we can find the issue after building the project when
Dagger graph is generated.

## Decision

In order to be more type safe when we are defining our dependencies we need to move to using `@Qualifier` from `javax.inject` package.

We can now define `Qualifier`s for different dependencies that have same return type.

```kotlin
@Qualifier
@Retention(AnnotationRetention.SOURCE)
annotation class FullDate
```

Then we can use that `Qualifier` while providing and injecting the dependencies.

```kotlin
@Provides
@FullDate
fun provideDateFormatterForFullDate(locale: Locale): DateTimeFormatter = DateTimeFormatter.ofPattern("d-MMM-yyyy", locale)

@field:[Inject FullDate]
lateinit var dateFormatter: DateTimeFormatter
```

This approach allows us to be more type-safe when providing and injecting our dependencies. Since `@Named` itself is a `@Qualifier`  they both behave
same way internally and there will be no additional overhead on build.

## Migrating Process

### New dependencies

When we are defining new dependencies that have same return type in the dagger graph. Instead of using `Named` we will create a `Qualifier` and use
that.

### Existing dependencies

For existing `Named` usages, we will leave them be until we need to change them or inject them somewhere new at which point we will convert
the `Named` usage to `Qualifier`.

## Consequences:

- Codebase will have both `Named` and `Qualifier` for a while.
