SDK Search (Chrome Extension)
=============================

Install from the [Chrome Web Store](https://chrome.google.com/webstore/detail/android-sdk-search/elihjfnjglabmkeonphlglkpjppchoco).

![](frontend/chrome-extension/src/main/store/screenshots/1.png)


Building
--------

### Local

Running `./gradlew :frontend:chrome-extension:installDebugDist` will create an "unpacked" extension
in `build/install/sdk-search-debug/`. This can be loaded into Chrome by visiting
`chrome://extensions` and clicking "Load unpacked extension...".

For convenience, this uses the 'ad2' omnibox keyword so that it can be used alongside the release
extension.


### Release

Running `./gradlew :frontend:chrome-extension:build` will create a zip file of the extension
in `build/distributions/`


Deploying
---------

After obtaining a zip of the extension from a release build, visit the
[Chrome Web Store developer dashboard](https://chrome.google.com/webstore/developer/dashboard) to
upload it.
