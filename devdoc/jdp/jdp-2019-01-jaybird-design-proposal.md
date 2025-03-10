<!--
SPDX-FileCopyrightText: Copyright 2019-2025 Mark Rotteveel
SPDX-License-Identifier: LicenseRef-PDL-1.0
-->
# jdp-2019-01: Jaybird Design Proposal

## Status

- Published
- Updated: 2025-02-26

## Type

- Project-Specification
- Living document

## Context

Jaybird is a long-running project, and its design decisions are currently 
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

The document should be in Asciidoc format (see also below) and stored in
`devdoc/jdp` with a filename consisting of the JDP number, followed by the
lowercase, dash separated descriptive title:

    jdp-yyyy-nn-descriptive-title.adoc
    
For example

    jdp-2019-01-jaybird-design-proposal.adoc

A JDP should consist of the following sections. The sections _Status_, _Type_ 
and _License Notice_ are required. The other sections should be considered 
the preferred structure. Feel free to use a different structure if it makes more
sense for a document.

JDPs before _jdp-2023-06_ were specified in Markdown, not Asciidoc. They may be
converted to Asciidoc if and when needed (e.g. large scale changes, or just when
we feel like it), or remain in Markdown.

At the start of the document, after the title and initial Asciidoc properties, the SPDX information of the author copyright and license should be declared:

<!-- REUSE-IgnoreStart -->

```asciidoc
= jdp-yyyy-NN: <title>

// SPDX-FileCopyrightText: Copyright <first-year>[-<last-year>] <author-name>
// SPDX-License-Identifier: LicenseRef-PDL-1.0
```

<!-- REUSE-IgnoreEnd -->

The license is always `LicenseRef-PDL-1.0` (for the Public Documentation License 1.0).

#### Status

The status of the document. Possible values:

- Draft
- Published: (date) : the date is optional for living documents
- Updated by: (jdp-reference)
- Replaced by: (jdp-reference)
- Implemented in: (first Jaybird version that implements this)
- Proposed for: (proposed Jaybird version to implement this) 
- Deferred indefinitely : not implemented nor proposed for a version; likely
the feature described will never be implemented
- Updated: (date) -- the last date a previously published document was updated

  This should be used only for textual corrections (i.e. typo fixes), for 
  (minor) changes which do not affect anything for application versions released
  after the original publish date, or for living documents.

The status section should also reference the JDP(s) it updates or replaces:

- Updates: (jdp-reference)
- Replaces: (jdp-reference)

A document can have multiple status values, but _Draft_ and _Published_ are 
mutually exclusive.

When a draft is not yet complete, it is advisable, but not required to not 
assign a JDP number yet, and put it in the directory `work-in-progress`. 

#### Type

The type of document. Possible values:

- Project-Specification : Specification for the project itself (e.g. governance,
  way of working, etc)
- Living document : indicates the document can undergo fundamental changes 
  without being replaced.
- Feature-Specification : Specification of features, architecture and other code
  related things
- Experimental : Feature is experimental and might be modified or dropped in
  point releases (only for use with _Feature-Specification_)
- .. (we'll likely find new types to add once we're using this)

A document can have multiple types. The types _Project-Specification_ and 
_Feature-Specification_ are mutually exclusive. The type _Experimental_ can only
occur with _Feature-Specification_. Modifications to _Experimental_ JDPs must 
be published as separate JDPs.

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

#### License Notice

Contains the Public Documentation License Notice, with the details of the author(s):

In Asciidoc format:

```asciidoc
[appendix]
== License Notice

The contents of this Documentation are subject to the Public Documentation License Version 1.0 (the “License”);
you may only use this Documentation if you comply with the terms of this License.
A copy of the License is available at https://firebirdsql.org/en/public-documentation-license/.

The Original Documentation is "`jdp-yyyy-NN: <title>`".
The Initial Writer of the Original Documentation is <author-name>, Copyright © <first-year>[-<last-year>].
All Rights Reserved.
(Initial Writer contact(s): <email address, hyperlink, etc>.

////
Contributor(s): ______________________________________.
Portions created by ______ are Copyright © _________ [Insert year(s)].
All Rights Reserved.
(Contributor contact(s): ________________ [Insert hyperlink/alias]).
////

The exact file history is recorded in our Git repository;
see https://github.com/FirebirdSQL/jaybird
```

The commented out part is a template for additional contributors.

## Consequences

Jaybird Design Proposals will help make the design decisions for Jaybird 
explicit. This can be used as a guide for Jaybird developers, and serve as
a form of documentation for users of Jaybird.

As we start with this 18 years after Jaybird started, we have a lot of 
undocumented historical decisions. It is unlikely we can reconstruct that, 
instead moving forward we will attempt to formalize if and when a JDP touches 
an aspect of Jaybird even if the JDP doesn't change that part.

## License Notice

The contents of this Documentation are subject to the Public Documentation
License Version 1.0 (the “License”); you may only use this Documentation if you
comply with the terms of this License. A copy of the License is available at
<https://firebirdsql.org/en/public-documentation-license/>.

The Original Documentation is "jdp-2019-01: Jaybird Design Proposal".
The Initial Writer of the Original Documentation is Mark Rotteveel,
Copyright © 2019-2025. All Rights Reserved. (Initial Writer contact(s):
mark (at) lawinegevaar (dot) nl).

<!--
Contributor(s): ______________________________________.
Portions created by ______ are Copyright © _________ [Insert year(s)]. All Rights Reserved.
(Contributor contact(s): ________________ [Insert hyperlink/alias]).
-->

The exact file history is recorded in our Git repository; see
<https://github.com/FirebirdSQL/jaybird>
 