# jdp-2019-05: Database Metadata Implementation

## Status

- Published: 2022-12-07
- Implemented in: Jaybird 5

## Type

- Feature-Specification
- Living document (for the time being)

## Context

The `java.sql.DatabaseMetaData` implementation, `org.firebirdsql.jdbc.FBDatabaseMetaData`,
is becoming unwieldy (at time of writing 3885 lines). This makes it hard to
understand, something which is made worse by version-dependent implementations.

In Jaybird 3, `org.firebirdsql.jdbc.FirebirdVersionMetaData` was introduced to
hold version-dependent metadata (although right now it only handles
`getSqlKeywords`). We expect that this enum by itself is not enough to address
the current problem, but would just move it. 

## Decision

The `FBDatabaseMetaData` implementation needs to be split up. The exact form of
this split will need to be determined by experimentation.

The following proposal should be taken as a suggestion, and be revised when it
is clear that the proposed structure isn't workable.

### Simple metadata methods

Simple metadata methods will remain in `FBDatabaseMetaData`. A small test shows
that moving those few methods that are version-dependent will only yield more 
code with little further benefit.

### Result set producing metadata methods

Result-set-producing metadata methods will be moved to separate implementation
classes, and be accessed through a static factory method (passing in the 
`FBDatabaseMetaData` object, the `FBConnection`, or possibly a new 
database metadata mediator object specific for metadata queries).

The static factory method is only responsible for returning the right metadata
object. The metadata object provides a method that matches the signature of the
`java.sql.DatabaseMetaData` method it implements.

The metadata object should be considered transient: it will be created for the
specific metadata method invocation and abandoned afterwards. 

If a result-set-producing metadata method is version dependent, the primary
class will be abstract, with the version-specific implementation done in nested
classes extending the primary class. The static factory method is responsible
for selecting the right implementation. This should not be handled through
`FirebirdVersionMetaData`.

The above should take into consideration the actual differences and amount of
duplication. For example, `DatabaseMetaData.getTypeInfo` is version-specific,
but it is probably better to use a single implementation with version checks to
account for the actual differences (new types, new precision, etc)..

Where possible, version-specific implementations will try and reuse code
between versions.

For purposes of code organization, these implementation classes will be put in a
separate package, `org.firebirdsql.jdbc.metadata`.

### OOREMOTE protocol

For the OOREMOTE protocol, we currently use a separate `DatabaseMetaData` 
implementation (`OODatabaseMetaData`). Instead of replacing/refactoring this
implementation, the decision was made to deprecate the protocol for removal in
Jaybird 6 (see jdp-2022-04).

### Testing

The individual metadata implementation classes will be tested through the 
`java.sql.DatabaseMetaData` API. This will ensure both the implementation and
the API are covered.

The individual implementation classes do not need their own test classes unless
it becomes apparent it is better for test coverage or testability.

### Roadmap

Given the amount of metadata to (re)implement, and the level of uncertainty over
the design, the proposal is to implement this gradually between Jaybird 4 and 
Jaybird 5 (and maybe Jaybird 6).

For Jaybird 4, the implementation of `getFunctions` and `getFunctionColumns` is
used as a testbed for the result set producing metadata methods. In addition,
one or two simple metadata methods will be moved to `FirebirdVersionMetaData`.

For Jaybird 5, the remaining result set producing metadata methods and version
dependent simple metadata methods will be moved. This should not be done
big-bang, but gradually and any new insights should be taken into account.

## Consequences

Refactoring the database metadata implementation will reduce the complexity and
amount of code per class. This will make it easier to focus on the specific
aspects of a metadata method, and make it easier to isolate version specific
differences.

However, in the wider picture, this change will introduce more classes,
increasing complexity for the entire project. We think this trade-off is
acceptable.

Jaybird 4 will serve as a testbed for this change, with completion expected for
Jaybird 5.
