# Android Easy Camera

## Deployment

To deploy a new version of a library, bump `libraryVersion` in `gradle.properties`
and execute run:

`./gradlew :easycamera:clean :easycamera:build :easycamera:bintrayUpload -PbintrayUser=<username>
-PbintrayKey=<api_key> -PdryRun=false --no-daemon`