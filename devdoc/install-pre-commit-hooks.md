<!--
SPDX-FileCopyrightText: Copyright 2025 Mark Rotteveel
SPDX-License-Identifier: LicenseRef-PDL-1.0
-->
# Install pre-commit hooks

As a contributor to the project, you can use pre-commit hooks to perform some
verifications before committing your work. This document describes how to
install them.

The use of these pre-commit hooks is not required, but will allow you to check
things that will otherwise fail later when the GitHub Actions are run.

## Installation instructions

1. Install a recent version of Python 3.
2. Install pre-commit using pip (see also <https://pre-commit.com/#install>):
   ```
   pip install pre-commit  
   ```
3. From the root of the Jaybird repository, run:
   ```
   pre-commit install
   ```

Now the pre-commit hooks are installed and will run before commit.

## Available pre-commit hooks

Jaybird currently defines the following pre-commit hooks:

### reuse

Checks if appropriate copyright and license information is present, verifies
compliance with the REUSE specification.

If you want to be able to run `reuse` manually, follow the installation instructions on <https://github.com/fsfe/reuse-tool?tab=readme-ov-file#install>.

A manual compliance check can then be run with:

```
reuse lint
```

## License Notice

The contents of this Documentation are subject to the Public Documentation
License Version 1.0 (the “License”); you may only use this Documentation if you
comply with the terms of this License. A copy of the License is available at
<https://firebirdsql.org/en/public-documentation-license/>.

The Original Documentation is "Install pre-commit hooks".
The Initial Writer of the Original Documentation is Mark Rotteveel,
Copyright © 2025. All Rights Reserved. (Initial Writer contact(s):
mark (at) lawinegevaar (dot) nl).

<!--
Contributor(s): ______________________________________.
Portions created by ______ are Copyright © _________ [Insert year(s)]. All Rights Reserved.
(Contributor contact(s): ________________ [Insert hyperlink/alias]).
-->

The exact file history is recorded in our Git repository; see
<https://github.com/FirebirdSQL/jaybird>