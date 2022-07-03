# jdp-2021-04: Real scrollable cursor support

## Status

- Published: 2022-07-03
- Implemented in: Jaybird 5

## Type

- Feature-specification

## Context

Firebird 3.0 introduced scrollable cursors in the internal API, PSQL, and for 
Embedded. Support was yet not added to the wire protocol at that time. Support
for scrollable cursors is added to the wire protocol in Firebird 5.0.

Scrollable cursors were introduced in JDBC 2.1, and Jaybird 4 and earlier
emulate support by retrieving the entire result set in memory.

### Details Firebird scrollable cursor support

- The protocol is extended on execute by including an extra int32,
  `p_sqldata_cursor_flags`. Currently, only one flag is defined, the first bit,
  which is set for scrollable, and not set for forward-only.
- A new operation, `op_fetch_scroll`, which compared to the 'normal' `op_fetch`
  has two additional int32 fields, `p_sqldata_fetch_op` and `p_sqldata_fetch_pos`
- `p_sqldata_fetch_op` is the fetch operation, with possible values:
  - `0` - next (same as `op_fetch`)
  - `1` - prior
  - `2` - first
  - `3` - last
  - `4` - absolute
  - `5` - relative
- `p_sqldata_fetch_pos` only has meaning for _absolute_ and _relative_, specifying
  either the absolute position to fetch (negative values are taken from the end, 
  with -1 the last row, -2 the second to last, etc.), or the relative position 
  (positive forward, negative backward)
- The field `p_sqldata_messages` is ignored for operations other than _next_ and 
  _prior_, one row will be returned.
- A scrollable cursor is fully materialized on the server.
- Scrollable cursors are insensitive. That is, any changes after execution are
  not visible. This includes positional updates or deletes (i.e. 
  `UPDATE/DELETE ... WHERE CURRENT OF ...`).
- The total number of rows of the cursor can be fetched with `op_info_cursor`
  (op-code 113) and info-item `INF_RECORD_COUNT` (code 10), where -1 is returned
  for non-scrollable cursors. This can only be retrieved after at least one
  fetch has been done.
- It is not possible to fetch scrollable through the legacy native API. This is
  only implemented in the native OO API (so far).

### Relevant details of Jaybird 4.0 and older result sets

- Updates are written back to the current row of the (cached) fetcher.
- Deletions are reflected in the result set by removing the row from 
  the (cached) fetcher.
- Inserts are added before the current row of the (cached) fetcher.

## Decision

Jaybird will add support for server-side scrollable cursors in the pure Java
implementation only. Given some differences in behaviour, it will need to
be enabled explicitly.

### Open options or questions

- Add option on statement or connection to explicitly enable scrollable cursors 
  for a statement, even if not globally enabled? Options: 
  - With a custom `resultSetType`
  - Property on the statement

### Rejected options

- Make server-side scrollable cursors the default.

  Although attractive from a "it-will-get-used" point-of-view, the differences in
  behaviour for updatable result sets are sufficient to warrant a conservative 
  approach and make it opt-in. This may be revisited in the future.
- Modify behaviour of emulated scrollable cursors to be the same (for updatable
  cursors).

  This is out of scope for this JDP, and should be considered separately in a 
  future JDP.
- Make `scrollableCursors` a `boolean` option.

  Making it a string option allows future expansion (e.g. adding an option 
  `server-read-only` to enable server-side scrolling only for non-updatable
  result sets), without introducing backwards compatibility issues.

## Consequences

Jaybird supports server-side scrollable cursor behind a configuration property.
The default behaviour continues to use emulated scrollable cursors.

Jaybird adds a connection property `scrollableCursor` with values `emulated` 
(default) and `server` (values are case-insensitive), in JDBC properties
and data sources. If `server` is specified, but the server does not support
server-side scrollable cursors, behaviour automatically falls back to `emulated`.

When holdable cursors are requested, server-side scrollable cursors are not 
used, as Jaybird doesn't use commit retain in this situation, so we need to rely
on a locally cached result set (i.e. `emulated` behaviour).

When server-side scrollable and updatable cursors are used, there are some
differences in behaviour compared to the emulated scrollable cursors:

- New rows are inserted at the end of the cursor
- Deleted rows are visible with an all-null marker row
- Result sets report `true` for `rowUpdated()`, `rowDeleted()` and 
`rowInserted()` for rows updated, deleted or inserted through the result set.

  This is not yet reflected in `updatesAreDetected()`, `deletesAreDetected()`
and `insertsAreDetected()` of `DatabaseMetaData`. This will be corrected when we
retrofit the new behaviour for _emulated_ (see below).

These differences may be resolved in a future JDP or Jaybird version, likely by 
changing the behaviour of emulated scrollable cursors.

When using server-side scrollable cursors in combination with a non-zero value
for the `maxRows` property of `Statement`, and the result set is after-last and
the server-side cursor has rows remaining, the server-side cursor is positioned
at `maxRows + 1`. This may result in gotchas / edge-cases like being able to
perform an `update` or `delete` with `where current of <cursor-name>` even though
the result set itself is not on a row.

Support will only be available in the pure Java implementation, and only when
connecting to Firebird 5.0 or higher.

See [Decision](#decision) and [Rejected options](#rejected-options).
