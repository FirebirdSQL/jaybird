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

Publishing to Maven Central (non-SNAPSHOT releases) requires an explicit close 
and release through <https://oss.sonatype.org/>.

To be able to deploy, you need the following:

a `<homedir>/.gradle/gradle.properties` with the following properties:

```
signing.keyId=<gpg key id>
signing.secretKeyRingFile=<path to your secring.gpg> 

ossrhUsername=<sonatype OSSRH usertoken name>
```

In addition, you need to set the following credentials

```
./gradlew addCredentials --key signing.password --value <your secret key password> -PcredentialsPassphrase=<credentials password> 
./gradlew addCredentials --key ossrhPassword --value <your sonatyp OSSRH password> -PcredentialsPassphrase=<credentials password> 
```

See https://github.com/etiennestuder/gradle-credentials-plugin for details on
credentials.
