Legacy Printer
==============

Outputs the package and class list from the new DAC `/getsuggestions` endpoint in the format of the
old DAC's `lists.js` file.

This allows us to easily update https://github.com/romannurik/AndroidSDKSearchExtension until
`frontend/chrome-extension` is ready to use.

Build a standalone jar by executing the `assemble` task.

Run the binary with `java -jar`:
```
java -jar build/libs/legacy-printer-all.jar
```
The `lists.js`-style output will be written to stdout which you can then redirect to a file.
