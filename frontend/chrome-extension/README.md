SDK Search (Chrome Extension)
=============================

![](frontend/chrome-extension/src/main/store/screenshots/1.png)


Building
--------

### Local

Running `./gradlew :frontend:chrome-extension:installDist` will create an "unpacked" extension
in `build/install/sdk-search/`. This can be loaded into Chrome by visiting `chrome://extensions`
and clicking "Load unpacked extension...".


### Release

Running `./gradlew :frontend:chrome-extension:distZip` will create a zip file of the extension
in `build/distributions/`


Deploying
---------

After obtaining a zip of the extension from a release build, visit the
[Chrome Web Store developer dashboard](https://chrome.google.com/webstore/developer/dashboard) to
upload it.
