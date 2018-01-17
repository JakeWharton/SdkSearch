SDK Search (Android)
====================

Download from [Google Play](https://play.google.com/store/apps/details?id=com.jakewharton.sdksearch).
Consider joining the [alpha testers](https://play.google.com/apps/testing/com.jakewharton.sdksearch)
for early access to the next version.

<img src="src/main/play/en-US/listing/phoneScreenshots/1.png" height="300"/>


Building
--------

### Debug

Debug builds should Just Work™.

The one caveat is that you won't be able to use the "Update from CI" app shortcut since it requires
a valid Circle CI API token. You can obtain a token at https://circleci.com/account/api. Once you
have the token, place the following in `~/.gradle/gradle.properties`:
```
SDK_SEARCH_CIRCLE_CI_TOKEN=<token>
```
(replacing `<token>` with the actual token, obviously)

Crashes in debug builds are not uploaded unless they are built on CI. See the "Release" section
below for information on configuring the required API key for this.


### Release

**Keystore**

Release builds will use the debug signing key unless an `upload.keystore` file is present. This
allows local builds to work without any setup to test things like ProGuard.

When an `upload.keystore` is present, a keystore password and key password are required. Place the
following in `~/.gradle/gradle.properties`:

```
SDK_SEARCH_UPLOAD_STORE_PASSWORD=<password>
SDK_SEARCH_UPLOAD_KEY_PASSWORD=<password>
```
(replacing each `<password>` with the actual respective value, obviously)

**Bugsnag**

A Bugsnag project API key is required. Place the following in `~/.gradle/gradle.properties`:
```
SDK_SEARCH_BUGSNAG_API_KEY=<key>
```
(replacing `<key>` with the actual key, obviously)


Deploying
---------

All deploy builds require that the `upload.keystore` is present (see "Release" above)

### Alpha

 1. Ensure all tests pass:
 
        ./gradlew clean :frontend:android:check :frontend:android:connectedCheck

 2. Update the version information in `build.gradle`.
 
 3. Update the information in `src/main/play` to reflect changes in style, app description, or
    what's new in the release.

 4. Build the release APK
 
        ./gradlew :frontend:android:assembleRelease
 
 5. Install the release APK onto a phone or emulator.
 
 6. Validate crash reporting works:
 
        adb shell am start -n com.jakewharton.sdksearch/.ui.MainActivity -e crash true
    
    The app should crash and the exception should be logged to Bugsnag.

At this point you have a verified APK that's ready to release.

Uploading the alpha to the Google Play developer console is handled by a Gradle plugin. You must
have an `upload.p12` file and the following in your `~/.gradle/gradle.properties`:

```
SDK_SEARCH_UPLOAD_PLAY_EMAIL=<email>
```
(replacing `<email>` with the actual email, obviously)

More information on the `.p12` file and the email can be found at
https://github.com/Triple-T/gradle-play-publisher#authentication

Upload the new APK to the alpha channel:

    ./gradlew :frontend:android:publishRelease




### Production

Visit the Google Play developer console and promote the latest alpha to production.
