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
