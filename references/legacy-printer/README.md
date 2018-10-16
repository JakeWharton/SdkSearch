Legacy Printer
==============

Outputs the package and class list from the new DAC `/getsuggestions` endpoint in the format of the
old DAC's `lists.js` file.

This allows us to easily update https://github.com/romannurik/AndroidSDKSearchExtension until
`frontend/chrome-extension` is ready to use.

Build an executable with `./gradlew :references:legacy-printer:installDist`.

The printer can then be run by invoking the binary:
```
./references/legacy-printer/build/install/legacy-printer/bin/legacy-printer
```
The `lists.js`-style output will be written to stdout which you can then redirect to a file.
