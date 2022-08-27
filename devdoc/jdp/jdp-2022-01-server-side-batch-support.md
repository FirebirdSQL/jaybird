# jdp-2021-04: Server-side Batch support

## Status

- Published: 2022-08-27
- Implemented in: Jaybird 5

## Type

- Feature-specification

## Context

Firebird 4.0 introduced support for batch updates in the wire protocol and
native API (in the OO API).

Batch updates were introduced in JDBC 2.1. Given Firebird did not support batch
updates, this was implemented by emulating batches.

### Relevant details Firebird batch updates

Information based on Firebird 4.0 implementation.

- Introduced in protocol v16, modified in v17 (changes not relevant for Jaybird).
- The protocol only supports batch updates with _elements_ (sets of parameters)
  for a single prepared statement (i.e. `PreparedStatement` equivalent). 
  Combining multiple different DML statements (i.e. `Statement` equivalent) is
  not supported.
- It is not possible to use `RETURNING` with batch updates.
- Update counts are sent in response `op_batch_cs` (`p_batch_updates`) when 
  requested using `TAG_RECORD_COUNTS`.
- Multiple errors are sent in response `op_batch_cs` when requested with 
  `TAG_MULTIERROR` (continues execution with the next element on error).
- Update counts signal the amount of rows updated for each element, or 
  `-2` (success no info), or `-1` (failed; **NOTE** JDBC uses `-3` for this)
- When not using multiple errors, the update counts include the `-1` (failed)
  for the first failed row (JDBC expects only update counts *before* the first 
  failed row)
- A batch can report upto 256 detailed errors in `p_batch_vectors` 
  (`TAG_DETAILED_ERRORS`, default maximum 64, set to 0 to report no detailed 
  errors). This is a combination of element number (0-based) and status vector.
- If there are more errors, these are reported in `p_batch_errors` which only
  identifies the failed element (0-based).
- A batch has a maximum size of 256MB, default 16MB, minimum sufficient for two
  rows. Setting buffer size to zero will use maximum batch size. Configured
  using `TAG_BUFFER_BYTES_SIZE`
- A number of batch operations (like `op_batch_create`, `op_batch_msg`, `op_batch_rls`)
  use deferred replies. The native implementation of fbclient also defers
  sending the request from client to server until execute, or - in v17 - when
  64 deferred packets are queued in the client.
- In the native API, this is only exposed through the OO API (the legacy API has 
  some bridging methods, but this still relies on the OO API for actual batch 
  usage)

## Decision

Jaybird will implement real batch updates in the pure-java wire protocol
implementation only, for now.

### Open options or questions

...

### Rejected options

- Add support in the native protocol.

  The native API is not a development priority, especially as this requires use
  of the OO API. If there is demand, this may be revisited in the future.
- Support sending blobs as part of the batch

  For now blobs are handled as-is, foregoing performance benefits. Supporting
  sending blobs as part of the batch requires large-scale refactoring. This may
  be revisited at a later time.
- Support multi-error ("continue one error") in JDBC.

  For now, we retain the existing "fail on first error" behaviour. If we revisit
  this, the emulated batch must also be adjusted to support this.
- Check if batch fits in buffer size, and if necessary split batch into using
  multiple server-side batch executes.

  We accept the `isc_batch_too_big` error thrown by the server when a too large 
  batch is used, as this only happens with batches of a few thousand rows (e.g.
  naive calculation for maximum row size allows for around 4000 rows).
- Support for `CallableStatement`.

  `FBCallableStatement` has a different implementation of batch updates, and we
  think the primary use case of batch updates is using prepared statements. At 
  this time we don't want to invest time to rewrite it to be able to use
  server-side batch updates.

  A workaround is to use `execute procedure` or `{call procedurename(..)}` with
  a prepared statement. This may be revisited at a later date if there is
  sufficient interest.

## Consequences

The GDS-ng `FbStatement` API adds methods to check if batch support is available,
create a batch, send rows to the server, execute the batch, and cancel (clear)
and release (close) the batch. Given server-side behaviour, creating, sending
rows and releasing will be semi-async.

In the JDBC implementation, a package private `Batch` interface is defined. 
Both the server-side as the "old" emulated behaviour will be implementations of
`Batch`, so there is a single general way of handling batches, with the details 
pushed into the `Batch` implementations.

The `Batch` interface allows _elements_ (sets of parameters) to be added to 
the batch, to execute the batch, clear the batch and close/release the batch.
The API is similar to the JDBC API for batch updates.

The _elements_ are represented by interface `BatchRowValue`, which has 
a `toRowValue()` method to convert to a row value that can be used for execution.
This exists specifically to allow finishing operations on the element, like 
flushing blobs. In practice, this flushing doesn't actually occur for normal 
prepared statements, but for consistent behaviour and to prevent bugs we do 
support this. At a later point, this could become an extension point to support
sending blobs as part of the batch, but that would require significant changes
in fields, `FBPreparedStatement`, `FbStatement`, `Batch` and `BatchRowValue`.

Given the lack of support for `RETURNING` with server-side batches, requesting
generated keys will fall back to the emulated batch.

The following connection properties will be added (to driver manager and data 
sources):

- `useServerBatch`, default: `true`, setting to `false` or connecting to 
  Firebird 3.0 or lower will fall back to emulated behaviour.
- `serverBatchBufferSize`, default: `0` (use server-side max), size in bytes of
  the batch buffer, negative value uses server-side default

Through the JDBC API, Jaybird will always request update counts, and will use
the server-side default number of detailed errors (64 as of Firebird 4.0).
