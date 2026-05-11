# still pact

this repo is part of the still app family. the pact is enforced in code, manifests, build files, and release notes.

## promises

- no internet permission.
- no firebase, no google play services, no analytics.
- plaintext export forever where the primitive owns user data.
- one app per primitive.
- honest permissions: every declared permission must be documented and load-bearing.

## verify

```bash
./tools/verify-still-pact.sh
JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew --no-daemon :app:assembleDebug :app:testDebugUnitTest --stacktrace
```
