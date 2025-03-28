<!--
SPDX-FileCopyrightText: Copyright 2023 Mark Rotteveel
SPDX-License-Identifier: LicenseRef-PDL-1.0
-->
# jdp-2023-02: How to handle new Java features

## Status

- Published

## Type

- Project-Specification
- Living document

## Context

Jaybird recently switched to Java 17 as minimum version, and a lot of new 
language features and convenience methods have been introduced between Java 8 
and Java 17. We need to decide how we're going to handle the new features, and 
how we introduce them in the existing codebase.

## Decision

### Introduction of new features in the codebase

Use of new Java features in the existing codebase should not be done in 
a big-bang approach. There should be no wholesale editing of the entire project 
to introduce the use of a new feature.

Use of new features should be done piecemeal, during normal work (e.g. adding 
a new feature, fixing a bug, or refactoring code). New features may be 
introduced in the methods or classes touched by the change, and should generally 
be committed as part of the initial change. If the changes to introduce a new 
Java feature overwhelm the initial change, then it is recommended to commit 
those changes separately.

Discretion is advised. In general, only touch code in methods altered by 
the initial change, unless changing the entire class makes sense for consistency.

### How to use new (language) features

The general approach to using language features is documented in [jdp-2023-03: Jaybird style guide](https://github.com/FirebirdSQL/jaybird/blob/master/devdoc/jdp/jdp-2023-03-jaybird-style-guide.md).

This section might be updated later if there are specific approaches to be 
documented that don't fall under the style guide.

## License Notice

The contents of this Documentation are subject to the Public Documentation
License Version 1.0 (the “License”); you may only use this Documentation if you
comply with the terms of this License. A copy of the License is available at
<https://firebirdsql.org/en/public-documentation-license/>.

The Original Documentation is "jdp-2023-02: How to handle new Java features".
The Initial Writer of the Original Documentation is Mark Rotteveel,
Copyright © 2023. All Rights Reserved. (Initial Writer contact(s):
mark (at) lawinegevaar (dot) nl).

<!--
Contributor(s): ______________________________________.
Portions created by ______ are Copyright © _________ [Insert year(s)]. All Rights Reserved.
(Contributor contact(s): ________________ [Insert hyperlink/alias]).
-->

The exact file history is recorded in our Git repository; see
<https://github.com/FirebirdSQL/jaybird>
