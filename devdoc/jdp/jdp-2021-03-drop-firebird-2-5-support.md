<!--
SPDX-FileCopyrightText: Copyright 2021-2023 Mark Rotteveel
SPDX-License-Identifier: LicenseRef-PDL-1.0
-->
# jdp-2021-03: Drop Firebird 2.5 support

## Status

- Published: 2023-01-03
- Implemented in: Jaybird 6

## Type

- Feature-specification

## Context

With the release of Firebird 2.5.9 in June 2019, the Firebird 2.5 line has
become end-of-life.

## Decision

Jaybird 6 will drop formal support for Firebird 2.5.

## Consequences

Jaybird 6 will not be tested against Firebird 2.5, and may use features from
Firebird 3.0. Support for the Firebird 2.5 wire protocol (version 12) will not
be removed, so in general the driver will likely continue to work, but some
features, e.g. `DatabaseMetaData`, may fail if it uses syntax or features
introduced in Firebird 3.0.

In general, we will no longer fix bugs that only occur when Jaybird is used with
Firebird 2.5 or earlier.

## License Notice

The contents of this Documentation are subject to the Public Documentation
License Version 1.0 (the “License”); you may only use this Documentation if you
comply with the terms of this License. A copy of the License is available at
<https://firebirdsql.org/en/public-documentation-license/>.

The Original Documentation is "jdp-2021-03: Drop Firebird 2.5 support".
The Initial Writer of the Original Documentation is Mark Rotteveel,
Copyright © 2021-2023. All Rights Reserved. (Initial Writer contact(s):
mark (at) lawinegevaar (dot) nl).

<!--
Contributor(s): ______________________________________.
Portions created by ______ are Copyright © _________ [Insert year(s)]. All Rights Reserved.
(Contributor contact(s): ________________ [Insert hyperlink/alias]).
-->

The exact file history is recorded in our Git repository; see
<https://github.com/FirebirdSQL/jaybird>
