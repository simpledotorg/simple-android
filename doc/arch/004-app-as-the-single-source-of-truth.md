# ADR 004: treating the client app as the source of truth

## Status

Accepted

## Context

Unlike an online-only app that can depend on the server to create unique record IDs, Simple is designed to work completely offline. It needs to create
data locally and perform two-way syncing of data with the server whenever possible. This brings two kinds of challenges,

1. Resolving conflicts when merging data from the server with its local copy.
2. Generating IDs that are universally unique.

## Decision

Simple is primarily an app-only service, therefore the client app is considered as the source of truth for
all [Protected Health Information (PHI)](https://en.wikipedia.org/wiki/Protected_health_information). If a conflict arises when merging a record
received from the server with a local copy of the record with the same ID, the server copy is discarded if the local copy is dirty (i.e., pending a
sync with the server). Otherwise, the server copy is saved.

For ensuring that the IDs are unique, [UUIDs](https://developer.android.com/reference/java/util/UUID.html) (universally unique identifier) are used.
While the probability that a UUID will be duplicated is not zero, it is close enough to zero to be negligible. There are multiple versions of UUID,
but we’re using **v4** because it is the most convenient to use and doesn’t require any seed value.

```kotlin
UUID.randomUUID()
```

It would have been nice to let our database handle generation of IDs, but SQLite only supports generation of auto-incrementing integer IDs. Other
alternatives like using `Random.nextLong` were also discarded because their space isn’t large enough to guarantee near uniqueness.

Both the client app and the backend can create a UUID and use it to identify something with near certainty that the identifier does not duplicate one
that has already been, or will be, created to identify something else.

According to [Wikipedia](https://en.wikipedia.org/wiki/Universally_unique_identifier#Collisions),

> the number of random version-4 UUIDs which need to be generated in order to have a 50% probability of at least one collision is 2.71 quintillion.

## Consequences

1. When discarding a record received from the server in favor of its dirty local copy, any additional data in the server record will be lost.

2. Usage of UUIDs over primitives as the primary key for SQLite table has two marginal disadvantages: they require more storage space, and are slower
   to query in comparison to primitives.

