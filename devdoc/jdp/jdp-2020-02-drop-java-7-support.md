<!--
SPDX-FileCopyrightText: Copyright 2020-2022 Mark Rotteveel
SPDX-License-Identifier: LicenseRef-PDL-1.0
-->
# jdp-2020-02: Drop Java 7 support

## Status

- Published: 2020-03-14
- Implemented in: Jaybird 5

## Type

- Feature-Specification

## Context

Java 7 has been end-of-public updates since April 2015. A lot of modern testing
libraries like JUnit 5 and related libraries, require a minimum of Java 8. In
addition, some of our testing libraries require bytecode manipulation libraries
that, for testing on Java 11 and higher, require versions that no longer work on
Java 7.

Moving to Java 8 as a minimum will allows us to switch to modern testing
libraries, and potentially allow us to modernize the build in other ways.

## Decision

Starting with Jaybird 5, Java 7 will no longer be supported.

## Consequences

Starting with Jaybird 5, Java 7 support will no longer be available. Jaybird
users requiring Java 7 can continue to use Jaybird 4 or earlier.

## License Notice

The contents of this Documentation are subject to the Public Documentation
License Version 1.0 (the “License”); you may only use this Documentation if you
comply with the terms of this License. A copy of the License is available at
<https://firebirdsql.org/en/public-documentation-license/>.

The Original Documentation is "jdp-2020-02: Drop Java 7 support".
The Initial Writer of the Original Documentation is Mark Rotteveel,
Copyright © 2020-2022. All Rights Reserved. (Initial Writer contact(s):
mark (at) lawinegevaar (dot) nl).

<!--
Contributor(s): ______________________________________.
Portions created by ______ are Copyright © _________ [Insert year(s)]. All Rights Reserved.
(Contributor contact(s): ________________ [Insert hyperlink/alias]).
-->

The exact file history is recorded in our Git repository; see
<https://github.com/FirebirdSQL/jaybird>
