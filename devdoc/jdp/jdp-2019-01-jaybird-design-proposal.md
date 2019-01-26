# jdp-2019-01: Jaybird Design Proposal

## Status

- Draft

## Type

- Project-Specification
- Living document

## Context

Jaybird is a long running project, and its design decisions are currently 
either implicit or hidden in commits, tracker tickets, code comments, 
discussions on - for example - Firebird-java, or - even worse - just in the mind 
of the implementer (and likely long since forgotten).

This can be problematic because those decisions are not easy to find, 
reconstruct, or remember. This can result in the following problems:

- Unintentional breaking changes
- Intentional breaking changes without proper rationale or considering the 
  impact
- Paralysis (fear of touching things, not knowing why they are like they are)
- Doubt/second-guessing when revisiting something years later

## Decision

Changes should be documented in a _Jaybird Design Proposal_ (or JDP). This is 
similar to Java's JEP or Python's PEP.

This includes, but is not limited to:

- Architectural changes
- Breaking changes
- New features

Ideally, changes should start with writing a JDP, that can then be discussed 
before implementation starts (or at minimum sent to Firebird-java for comments). 
During implementation, the JDP can be improved and revised.

Minor changes do not need to be documented in a JDP.

### Document structure

The JDP is identified by a number that consists of a 4 digit year, followed by
two digits within that year: `jdp-yyyy-nn`. If we ever produce more than 99 JDPs
within a year (unlikely), this will be increased to three digits. 

The document title should consist of the JDP number followed by a descriptive 
name:

    jdp-yyyy-nn: <descriptive title>
    
For example

    jdp-2019-01: Jaybird Design Proposal

The document should be in markdown format, and stored in `devdoc/jdp` with a
filename consisting of the JDP number, followed by the lowercased, dash 
separated descriptive title:

    jdp-yyyy-nn-descriptive-title.md
    
For example

    jdp-2019-01-jaybird-design-proposal.md

A JDP should consist of the following sections. The sections _Status_ and _Type_
are required. The other sections should be considered the preferred structure. 
Feel free to use a different structure if it makes more sense for a document.

#### Status

The status of the document. Possible values:

- Draft
- Published: (date) : the date is optional for living documents
- Updated by: (jdp-reference)
- Replaced by: (jdp-reference)
- Implemented in: (first Jaybird version that implements this)
- Proposed for: (proposed Jaybird version to implement this) 

The status section should also reference the JDP(s) it updates or replaces:

- Updates: (jdp-reference)
- Replaces: (jdp-reference)

A document can have multiple status values, but _Draft_ and _Published_ are 
mutually exclusive.

#### Type

The type of document. Possible values:

- Project-Specification : Specification for the project itself (eg governance,
  way of working, etc)
- Living document : indicates the document can undergo fundamental changes 
  without being replaced.
- Feature-Specification : Specification of features, architecture and other code
  related things
- .. (we'll likely find new types to add once we're using this)

A document can have multiple types. The types _Project-Specification_ and 
_Feature-Specification_ are mutually exclusive.

The _Living document_ type should generally only be used for 
_Project-Specification_ documents like this document, and not for features.

#### Context

Describes the background and context of the JDP. For example, the reasons for
proposing the changes, or 'external force' that are relevant to the decision.

#### Decision

Documents the decision itself in sufficient detail. For features, it should
describe a rough outline of the API (the more detail, the better), and describe
technical considerations for the implementation.

Preferably, the reasons for the decisions should be documented, and - where 
possible - discarded choices or alternatives should be documented.

If any follow-up features or possible future improvement were uncovered, these
should be listed as well.

#### Consequences

Documents consequences of the decision. This includes identifying expected
breaking changes caused by the decision.

## Consequences

Jaybird Design Proposals will help make the design decisions for Jaybird 
explicit. This can be used as a guide for Jaybird developers, and serve as
a form of documentation for users of Jaybird.

As we start with this 18 years after Jaybird started, we have a lot of 
undocumented historical decisions. It is unlikely we can reconstruct that, 
instead moving forward we will attempt to formalize if and when a JDP touches 
an aspect of Jaybird even if the JDP doesn't change that part.
 