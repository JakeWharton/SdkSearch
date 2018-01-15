References Validator
====================

This Java app will validate that the source mappings for all classes in the Android SDK reference
point to valid files.

Run the validator with `./gradlew :references:validator:run`. This will take anywhere from 10 to 20
minutes depending on rate limiting. Correct any 404 errors by updating the mappings in
`ReferenceLists.kt`.
