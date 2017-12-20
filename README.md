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
