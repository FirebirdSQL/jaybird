<!--
SPDX-FileCopyrightText: Copyright 2023-2026 Mark Rotteveel
SPDX-License-Identifier: LicenseRef-PDL-1.0
-->
# jdp-2023-03: Jaybird style guide

## Status

- Published

## Type

- Project-Specification
- Living document

## Context

A project that is developed by - potentially - multiple developers and 
contributors requires a shared understanding of the code style, and certain 
_"Do's and Don't's"_.

This document serves as the _Style Guide_ of Jaybird.

This is a _Living Document_, which means it will be updated as new style issues 
or implicit assumptions on style are uncovered, or questions or ambiguities 
arise.

## Code Formatting

Code formatting is governed by `/devdoc/formatting/intellij-jaybird-style.xml`.
Make sure to enable formatter markers under Settings > Editor > Code Style > Formatter 
with markers `@formatter:off` and `@formatter:on`.

For Eclipse, there is `/devdoc/formatting/eclipse-jaybird-formatter.xml`, which 
was exported from IntelliJ. In case of differences, the IntelliJ style takes 
precedence.

If you're not using IntelliJ or Eclipse, let us know what you're using, and 
we'll try to create a suitable format definition for your IDE of choice, or 
otherwise document the expected style in plain English.

Our approach to code formatting is _"deviate when it makes sense"_. If 
the default formatting reduces readability of a piece of code, use formatter 
markers (see above) to disable formatting and apply your own formatting. 
However, we ask contributors to do this sparingly. The main developers have 
the final say about style.

Some code formatting style is documented explicitly in the sections below:

### Line Length

#### Java

The Java line length is max. 120 characters.

#### Javadoc

The same maximum line length applies to Javadoc.

(And otherwise, the IntelliJ rules for Javadoc don't allow separate 
configuration of Javadoc comment length.)

#### Markdown

The Markdown line length is 80 characters. In general, you should terminate 
lines at or before 80 characters. Exceptions are made for links and long 
inline code-formatted text like fully-qualified classnames.

When breaking a line, it is preferred to keep article and noun together, that
means that even if the article (e.g. _"the"_) would still fit on the line, you 
break *before* the article, so article and noun are on the new line.

#### AsciiDoc

For AsciiDoc, we don't apply a maximum line length for normal text. Instead, 
a "sentence-per-line" (or "line-per-sentence", or "ventilated prose") style is 
used. This means

1. No line breaks inside a sentence
2. A new sentence starts on a new line
3. For the purpose of style, text following a semicolon (`;`) is also considered 
   a new sentence
4. For code blocks, keep line length around max. 70 characters
5. Exceptions may be made for rule 1 if the sentence contains a footnote

## Miscellaneous

### Use of `var`

The `var` type keyword is allowed, but only for the following cases:

1. The assignment expression starts with `new` (e.g. `var someVariable = new SomeType(...)`)
2. The assignment expression is a simple factory method that clearly reveals 
   the type (e.g. `var someVariable = SomeType.of(...)`)
3. The assignment expression is a well-known JDBC method (e.g. 
   `var connection = DriverManager.getConnection(..)` and 
   `var pstmt = connection.prepareStatement(..)`) (this can be considered 
   a specialization of rule 2)
4. String literal assignment (e.g. `var someString = "literal"`)

For consistency, the preference is to always use `var` for above cases when 
writing *new* code or refactoring *existing* code. See also [jdp-2023-02](https://github.com/FirebirdSQL/jaybird/blob/master/devdoc/jdp/jdp-2023-02-how-to-handle-new-java-features.md)
All other cases should use an explicit type.

For test code, additional cases are allowed:

5. The assignment expression clearly expresses its actual type in the method
   name of a well-known class or library (e.g. `var columnValue = ResultSet.getString(1)`
   or `var someObjectString = object.toString()`)
6. The assignment expression clearly expresses its actual type through a _cast_
   (e.g. `var dbmd = (FirebirdDatabaseMetaData) connection.getMetaData()`) or
   `unwrap` (e.g. `var fbConnection = connection.unwrap(FirebirdConnection.class)`)

## Methods

In general, we follow common Java method naming rules. Here, we document some
deviations.

### Naming of Private Methods Split From a Larger Method

When splitting of a private method with parts of the implementation of a larger
method, it is preferred to give it a good name. However, sometimes that leads to
obnoxiously long names that do very little to clarify purpose (or possibly, we
lack the imagination to come up with a good name).

In that case, we allow use of `<method-name><number>` as the method name. For
example, `flush0()` as part of the implementation of `flush()`. However, this
comes with the following limitations:

1. The split-off method must be private.
2. Only `originalMethod(..)` (any overload) calls `originalMethod0(...)`. If
   a differently named method calls it, the method should receive a real name,
   e.g. `flushNoLock()`.
3. Methods with a numeric must may not be overloaded: each receives their own
   suffix number.
4. There should only be at most two such methods (i.e. `originalMethod0(...)`, 
   `originalMethod1(...)`). If a third needs to be added, it is time to think
   harder on names.
5. This naming must not be applied to methods that already have a numeric suffix.
   So, no `flush00()`, or `flush0_0()`, etc.

## License Notice

The contents of this Documentation are subject to the Public Documentation
License Version 1.0 (the “License”); you may only use this Documentation if you
comply with the terms of this License. A copy of the License is available at
<https://firebirdsql.org/en/public-documentation-license/>.

The Original Documentation is "jdp-2023-03: Jaybird style guide".
The Initial Writer of the Original Documentation is Mark Rotteveel,
Copyright © 2023-2026. All Rights Reserved. (Initial Writer contact(s):
mark (at) lawinegevaar (dot) nl).

<!--
Contributor(s): ______________________________________.
Portions created by ______ are Copyright © _________ [Insert year(s)]. All Rights Reserved.
(Contributor contact(s): ________________ [Insert hyperlink/alias]).
-->

The exact file history is recorded in our Git repository; see
<https://github.com/FirebirdSQL/jaybird>
