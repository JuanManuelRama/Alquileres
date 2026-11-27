# Control Alq

An Android app for managing rental properties and leases: track owners, properties, tenants, contracts, and monthly bills, with WhatsApp/SMS billing messages and filtering by contract status. Data is stored locally with Room; the UI is built with Jetpack Compose and Material 3. English and Spanish strings are included.

## Build and test

Open the project in Android Studio, or from the command line:

```sh
./gradlew assembleDebug
./gradlew test      
./gradlew connectedAndroidTest
```

Requires JDK 11+ and Android SDK 37. Dependencies are managed by Gradle version catalogs in `gradle/libs.versions.toml`.
