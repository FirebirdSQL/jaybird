<!--
SPDX-FileCopyrightText: Copyright 2021 Mark Rotteveel
SPDX-License-Identifier: LicenseRef-PDL-1.0
-->
# jdp-2021-02 Stream blobs by default

## Status

- Published: 2021-10-16
- Implemented in: Jaybird 5

## Type

- Feature-Specification

## Context

Firebird has two ways of storing blobs, segmented and streamed. When a blob is
segmented, it is stored in multiple length-prefixed segments, the length of each
segment is determined by the actual size of segments sent by the client (and
not, as some may think, by the `SEGMENT SIZE` attribute of the blob column
definition, which is only informational, e.g. for clients).

A stream blob is stored as a single 'stream' of bytes. The client still sends
and receives blob content in segments, but on storage there are no individual
size-prefixed segments. Contrary to segmented blobs, stream blobs support the
full range of seek options, absolute from head or tail, and relative to current
position.

The type of storage of a blob is determined by the client, specifying
`isc_bpb_type` with value `isc_bpb_type_segmented` or `isc_bpb_type_stream` in
the blob parameter buffer.

In theory, segmented blobs could have some benefits with for example UTF-8
encoded data, in that segments could be written in a way that multi-byte
characters don't straddle two segments. However, due to the way Jaybird writes
(and reads) blobs, this doesn't happen (characters can straddle two segments,
and on read characters straddling two segments are not a problem).

## Decision

As there is no downside to using stream blobs, and stream blobs offer additional
features, Jaybird will switch to using stream blobs by default.

## Consequences

The default value for connection property `useStreamBlobs` changes from `false`
to `true`.

## License Notice

The contents of this Documentation are subject to the Public Documentation
License Version 1.0 (the “License”); you may only use this Documentation if you
comply with the terms of this License. A copy of the License is available at
<https://firebirdsql.org/en/public-documentation-license/>.

The Original Documentation is "jdp-2021-02 Stream blobs by default".
The Initial Writer of the Original Documentation is Mark Rotteveel,
Copyright © 2021. All Rights Reserved. (Initial Writer contact(s):
mark (at) lawinegevaar (dot) nl).

<!--
Contributor(s): ______________________________________.
Portions created by ______ are Copyright © _________ [Insert year(s)]. All Rights Reserved.
(Contributor contact(s): ________________ [Insert hyperlink/alias]).
-->

The exact file history is recorded in our Git repository; see
<https://github.com/FirebirdSQL/jaybird>
