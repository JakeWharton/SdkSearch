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


### Release

TODO



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
