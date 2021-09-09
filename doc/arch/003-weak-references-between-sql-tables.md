# ADR 003: Weak references (no foreign keys) between SQLite tables

## Status

Accepted

## Context

Strong references between SQL tables using foreign keys are useful for maintaining data integrity. They introduce safeguards against orphaned records
by,

- making it impossible to create data records that do not fulfill relation.
- deleting referenced records in a cascading fashion when the root dependent record is deleted.

In the context of Simple, a good example would be the `Patient` and `BloodPressureMeasurement` tables. In real world, it’s impossible to record a
blood pressure for a non-existent patient. Likewise, maintaining a strong reference between these two tables in SQL would have ensured that blood
pressures do not get created either unless the associated patient already exists in the database.

Additionally, if the patient ever gets deleted, SQLite would have handled deletion of all their blood pressures automatically.

Unfortunately, strong references work only when the data storage is centralized. Because Simple’s data is distributed across many devices, it’s
impossible to guarantee the existence of all data records at the same time due to its offline by default nature. For instance, it’s easy to imagine a
scenario where the app receives some blood pressures from the server, but fails to receive their associated patients because of bad network
connectivity. This is expected behavior and we do not want the app to fail because of a foreign key integrity failure.

## Decision

SQL tables in Simple will not keep strong references using foreign keys unless they can be synced **together** in the same network call with the
server.

## Consequences

We are intentionally making a trade-off by letting go of data integrity on the database level. We’ll have to rely on the user interface to ensure
safety against the creation of orphaned records. For instance, the user interface should make it impossible to record a blood pressure if the patient
does not exist. 
