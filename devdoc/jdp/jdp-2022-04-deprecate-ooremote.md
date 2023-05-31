# jdp-2022-04: Deprecate OOREMOTE (OpenOffice/LibreOffice driver) for removal

## Status

- Published: 2022-12-07
- Implemented in: Jaybird 5 (deprecation)
- Implemented in: Jaybird 6 (removal)

## Type

- Feature-Specification

## Context

For years Jaybird has had the OOREMOTE protocol (JDBC URL `jdbc:firebird:oo` and
`jdbc:firebirdsql:oo`) for use with LibreOffice and OpenOffice to address 
differences in interpretation of the JDBC requirements, especially regarding use 
of `DatabaseMetaData`.

These days, OpenOffice is hardly used by anyone, and LibreOffice now has builtin
support for Firebird (either as _Firebird Embedded_ or Firebird database inside 
an `.odb` file, or _Firebird External_ or connecting to a Firebird server), 
removing the need to use a JDBC driver.

The OOREMOTE implementation is not tested, and the reasons for the specifics of
the implementation are unavailable, making evolution of the code hard.

## Decision

Given there is now an alternative with builtin support within LibreOffice 
itself, the OOREMOTE protocol is deprecated in Jaybird 5 and will be removed in
Jaybird 6.
