References Validator
====================

This Java app will validate that the source mappings for all classes in the Android SDK reference
point to valid files.

Build an executable with `./gradlew :references:validator:installDist`.

The validator can then be run by invoking the binary:
```
./references/validator/build/install/validator/bin/validator
```
This will take anywhere from 10 to 20 minutes depending on rate limiting. Correct any 404 errors by
updating the mappings in `ReferenceLists.kt`.

If you want to validate a subset of APIs (such as when fixing paths), you can supply a list of
package names to the script. For example,
```
./references/validator/build/install/validator/bin/validator android.test android.arch
```
