# jdp-2023-02: Replace synchronized with ReentrantLock

## Status

- Published: 2022-09-12
- Implemented in: Jaybird 5
- Updated: 2024-03-04

## Type

- Feature-specification

## Context

Jaybird currently uses a lot of synchronized blocks for thread-safety. Java 19 
introduces virtual threads with Project Loom (as a preview feature). Virtual 
threads and synchronized blocks currently don't work well together (or more 
correctly, seriously reduce the benefits/usability of virtual threads due to 
thread pinning). See https://openjdk.org/jeps/425 for details. Although JEP 425 
says _"In a future release, we may be able to remove the first limitation above 
(pinning inside `synchronized`)."_, this is not a solved problem, and may never 
surface (see https://mail.openjdk.org/pipermail/jdk-dev/2022-June/006721.html).

As a result, replacing synchronized blocks with reentrant locks (and maybe at 
the same time, look at reducing use of locks) makes sense to allow Jaybird to 
make use of virtual thread without pinning (that is, for the pure-java protocol, 
as calling native methods will still cause pinning).

## Decision

The usages of `org.firebirdsql.jdbc.Synchronizable`, specifically use of 
`synchronized` on `getSynchronizationObject()`, and other forms of connection
mutual exclusion (for example. GDS API doesn't use `Synchronizable`, but does 
apply a similar pattern, e.g. `FbAttachment.getSynchronizationObject()`) will be 
replaced with a `ReentrantLock`.

Other usages of `synchronized` will be evaluated separately, if necessary.

### Rejected Options

- Defining a `void withLock(ExceptionRunnable)` or similar to use a lambda
  to define the action taken under lock.

  This would require more methods (e.g. for varieties of exceptions, simply
  runnable versus returning a value, etc.). In evaluation, the solution
  with a closeable, allowing use of try-with-resources, looked cleaner.

  Other arguments against might be modification of local variables, but we 
  didn't check if this actually occurs in the codebase.
- Define a separate interface with the `withLock()` and `isLockedByCurrentThread()`
  methods in the GDS-ng API.

  With the chosen solution the methods are defined only where they are needed,
  reducing access-level where possible. The exposure through `FbAttachment`
  and `FbStatement` is for simplicity of implementation.

## Consequences

The interface `org.firebirdsql.jdbc.Synchronizable` will be removed, and 
methods `LockClosable withLock()` and `boolean isLockedByCurrentThread()` are 
introduced were appropriate and needed.`

The `withLock()` method will take out a lock, and return a `AutoClosable` 
variant which will unlock on `close()`, so it can be used in a 
try-with-resources.

In that way, current usages of

```java
synchronized (getSynchronizationObject()) {
    // ...
}
```

can be replaced with

```java
try (LockCloseable ignored = withLock()) {
    // ...
}
```

The `isLockedByCurrentThread()` returns true if the lock is held by
the current thread.

The `Lock` (`ReentrantLock`) itself will not be exposed (though this might be
revisited in the future).

Classes and interfaces in `org.firebirdsql.jdbc` currently implementing 
`Synchronizable` will - if needed - implement a `protected ClosableLock withLocK()`
wrapping the `withLock()` of the enclosed attachment, statement or other object
exposing `withLock()`. The intent is to remove locking from the public JDBC API, 
it will only be accessible on internal/semi-internal APIs.

All these interface, methods, etc., are considered internal API of Jaybird, and
may change in point releases.
