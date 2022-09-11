# jdp-2023-02: Replace synchronized with ReentrantLock

## Status

- Draft
- Proposed for: Jaybird 5

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
`synchronized` on `getSynchronizationObject()` will be replaced with 
a `ReentrantLock`.

Other usages of `synchronized` will be evaluated separately, if necessary.

## Consequences

The interface `org.firebirdsql.jdbc.Synchronizable` will be replaced with 
an interface `org.firebirdsql.jaybird.concurrent.Lockable` which offers a single
method, `LockClosable withLock()`. This method will take out a lock, and return
a `Closable` variant which will unlock on `close()`, so it can be used in
a try-with-resources.

In that way, current usages of

```java
synchronized (getSynchronizationObject()) {
    // ...
}
```

can be replaced with

```java
synchronized (withLock()) {
    // ...
}
```

The `Lock` (`ReentrantLock`) itself will not be exposed (though this might be
revisited in the future).

Classes and interfaces in `org.firebirdsql.jdbc` currently implementing 
`Synchronizable` will not switch to implementing `Lockable` to remove this from
the public JDBC API, it will only be accessible on internal/semi-internal APIs.

All these interface, methods, etc., are considered internal API of Jaybird.
