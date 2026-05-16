<!--
SPDX-FileCopyrightText: Copyright 2020-2026 Mark Rotteveel
SPDX-License-Identifier: LicenseRef-PDL-1.0
-->
Publishing
==========

To publish to Maven use

```
./gradlew clean assemble publish -PcentralPassword=<value>
```

This command will prompt for you GPG key password if it's not already cached in
your current session.

The `assemble` task is not strictly necessary, but will also generate the `dist`
zip and sign it.

Publishing to Maven Central (non-SNAPSHOT releases) requires the following
additional steps:

1. Promote the published artifacts to Central Portal through the SwaggerUI <https://ossrh-staging-api.central.sonatype.com/swagger-ui/>
2. An explicit close through <https://central.sonatype.com/publishing/deployments>.

To be able to deploy, you need the following:

a `<homedir>/.gradle/gradle.properties` with the following properties:

```
signing.gnupg.keyName=<short keyid>

centralUsername=<Central Portal usertoken name>
# Only needed if you don't want to specify -PcentralPassword=... on commandline
centralPassword=<your Central Portal usertoken password>
```

(It's possible `signing.gnupg.keyName` also accepts long key-ids, we haven't checked.)

Make sure the file is only readable and writable by you (chmod 600). If the
password contains backslashes, make sure to escape them by doubling. If the
password contains characters not in ISO 8859-1, make sure to use a Java Unicode
escape.

See also [Gradle: The Signing Plugin](https://docs.gradle.org/current/userguide/signing_plugin.html).

## License Notice

The contents of this Documentation are subject to the Public Documentation
License Version 1.0 (the “License”); you may only use this Documentation if you
comply with the terms of this License. A copy of the License is available at
<https://firebirdsql.org/en/public-documentation-license/>.

The Original Documentation is "Publishing".
The Initial Writer of the Original Documentation is Mark Rotteveel,
Copyright © 2020-2026. All Rights Reserved. (Initial Writer contact(s):
mark (at) lawinegevaar (dot) nl).

<!--
Contributor(s): ______________________________________.
Portions created by ______ are Copyright © _________ [Insert year(s)]. All Rights Reserved.
(Contributor contact(s): ________________ [Insert hyperlink/alias]).
-->

The exact file history is recorded in our Git repository; see
<https://github.com/FirebirdSQL/jaybird>
