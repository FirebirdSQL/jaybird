<!--
SPDX-FileCopyrightText: Copyright 2022-2023 Mark Rotteveel
SPDX-License-Identifier: LicenseRef-PDL-1.0
-->
# jdp-2022-03: Java 17 minimum version

## Status

- Published: 2023-01-03
- Implemented in: Jaybird 6

## Type

- Feature-Specification

## Context

At time of writing, 2022-10-09, Java 17 is the latest LTS of Java. Since Java 8,
a lot of new features have been added (and in some cases, removed or have been
deprecated for removal).

According to <https://en.wikipedia.org/wiki/Java_version_history>, various 
vendors offer support for Java 8 up to 2030, and for Java 11 up to 2027.

On the other hand, in the Java ecosystem various projects are moving towards
Java 17 as the baseline version (e.g. Spring 6 and Spring Boot 3).

Time between major Jaybird releases:

- Jaybird 2.1 - Jaybird 2.2: 6 years
- Jaybird 2.2 - Jaybird 3: 5 years
- Jaybird 3 - Jaybird 4: 3 years
- Jaybird 4 - Jaybird 5: 2.5 years (expected)

From a personal perspective, supporting a wide range of Java versions, while not
being able to use newer features, is less fun and a bit demotivating. 

## Decision

Starting with Jaybird 6, Java 17 is the minimum version.

Jaybird 5 will serve as a form of LTS for Java 8 and Java 11, with maintenance
releases at least until release of Jaybird 7 (and possibly longer).

## Consequences

Starting with Jaybird 6, support for version before Java 17 will no longer be
available. Jaybird users requiring support for older versions can continue to 
use Jaybird 5 or earlier.

## License Notice

The contents of this Documentation are subject to the Public Documentation
License Version 1.0 (the “License”); you may only use this Documentation if you
comply with the terms of this License. A copy of the License is available at
<https://firebirdsql.org/en/public-documentation-license/>.

The Original Documentation is "jdp-2022-03: Java 17 minimum version".
The Initial Writer of the Original Documentation is Mark Rotteveel,
Copyright © 2022-2023. All Rights Reserved. (Initial Writer contact(s):
mark (at) lawinegevaar (dot) nl).

<!--
Contributor(s): ______________________________________.
Portions created by ______ are Copyright © _________ [Insert year(s)]. All Rights Reserved.
(Contributor contact(s): ________________ [Insert hyperlink/alias]).
-->

The exact file history is recorded in our Git repository; see
<https://github.com/FirebirdSQL/jaybird>
