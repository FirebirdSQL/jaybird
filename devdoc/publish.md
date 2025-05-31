<!--
SPDX-FileCopyrightText: Copyright 2020-2025 Mark Rotteveel
SPDX-License-Identifier: LicenseRef-PDL-1.0
-->
Publishing
==========

To publish to Maven use

```
gradlew clean dist assemble publish -PcredentialsPassphrase=<credentials password>
```
Where `<credentials password>` is the password used to add the credentials (see
also below).

The `assemble` task is not strictly necessary, but will also generate the `dist`
zip and sign it.

Publishing to Maven Central (non-SNAPSHOT releases) requires the following
additional steps:

1. Promote the published artifacts to Central Portal through the SwaggerUI <https://ossrh-staging-api.central.sonatype.com/swagger-ui/>
2. An explicit close through <https://central.sonatype.com/publishing/deployments>.

To be able to deploy, you need the following:

a `<homedir>/.gradle/gradle.properties` with the following properties:

```
signing.keyId=<gpg key id>
signing.secretKeyRingFile=<path to your secring.gpg> 

centralUsername=<Central Portal usertoken name>
```

In addition, you need to set the following credentials

```
./gradlew addCredentials --key signing.password --value <your secret key password> -PcredentialsPassphrase=<credentials password> 
./gradlew addCredentials --key centralPassword --value <your Central Portal usertoken password> -PcredentialsPassphrase=<credentials password> 
```

See https://github.com/etiennestuder/gradle-credentials-plugin for details on
credentials.

## License Notice

The contents of this Documentation are subject to the Public Documentation
License Version 1.0 (the “License”); you may only use this Documentation if you
comply with the terms of this License. A copy of the License is available at
<https://firebirdsql.org/en/public-documentation-license/>.

The Original Documentation is "Publishing".
The Initial Writer of the Original Documentation is Mark Rotteveel,
Copyright © 2020-2025. All Rights Reserved. (Initial Writer contact(s):
mark (at) lawinegevaar (dot) nl).

<!--
Contributor(s): ______________________________________.
Portions created by ______ are Copyright © _________ [Insert year(s)]. All Rights Reserved.
(Contributor contact(s): ________________ [Insert hyperlink/alias]).
-->

The exact file history is recorded in our Git repository; see
<https://github.com/FirebirdSQL/jaybird>
