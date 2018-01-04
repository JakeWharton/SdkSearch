SDK Search
==========

A quick tile which searches the contents of the Android SDK.


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

A Bugsnag project API key is required. Place the following in `~/.gradle/gradle.properties`:
```
SDK_SEARCH_BUGSNAG_API_KEY=<key>
```
(replacing `<key>` with the actual key, obviously)


Deploying
---------

 1. Ensure all source mappings are working.

    * Run `gw :references:validator:run`

    * Correct any 404s in `ReferenceLists.kt`

 2. Create a clean release build: `./gradlew clean assembleRelease`

 3. Install it onto a device: `./gradlew installRelease`

 4. Run `adb shell am start -n com.jakewharton.sdksearch/.ui.MainActivity -e crash true` and ensure
    that the app crashed and that the exception was reported to Bugsnag.

 5. Upload to Google Play.




License
=======

    Copyright 2016 Jake Wharton

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
