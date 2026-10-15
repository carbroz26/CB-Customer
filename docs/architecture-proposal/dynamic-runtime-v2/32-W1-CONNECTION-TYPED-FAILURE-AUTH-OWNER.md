# 32 — W1 Connection Exception and Bearer Ownership Gate

**Status:** One production connection mapping and one focused MockEngine test committed; **new JVM tests not yet executed**, W1 open. Authoritative scope: frozen 20, detailed 17 (as corrected by 19/20), logging/KDoc and cleanup 23, latest source audits 26–31. Single branch only `architecture/dynamic-runtime-v2-discussion-freeze`.

## Exact production change
- `network/src/commonMain/kotlin/com/carbroz/cbpartner/network/http/RequestHandler.kt`: catch cross-platform Ktor `io.ktor.util.network.UnresolvedAddressException` **after** timeout/cancellation and before generic Throwable, return existing `NetworkException.Connection("Unable to resolve network address", cause)`. Never copy hostname/exception message into public message. Existing Unknown remains default for **other** unproven I/O failure categories; no guessed broad IOException classification.
- Update existing RequestHandler KDoc: cancellation propagates; timeout vs DNS error typed.
- `network/src/commonTest/kotlin/com/carbroz/cbpartner/network/http/RequestHandlerTest.kt`: MockEngine throws actual Ktor UnresolvedAddressException; assert Connection, null status, safe message. It tests a concrete exception family, **not** real DNS resolution on Android/Web/iOS.
- No new class, manager, dependency, DI registration, Loggers, import-only dead code or serializer introduced.

## Connection exception scope
Ktor API documents UnresolvedAddressException as a common error for unresolved network address across JVM/native/web (see Ktor API UnresolvedAddressException). Its JVM actual is `java.nio.channels.UnresolvedAddressException`. A refused TCP socket, TLS error, EOF and offline browser failures may use different engine-specific causes; these are **not** classified by this one branch. No broad IOException catch without verifying desired taxonomic policy against actual engines.

## Authentication cross-boundary ownership
- `RequestActionHandler` resolves action `authentication` to default NONE and constructs DynamicRequest; `DynamicRepositoryImpl` and `DynamicRemoteDataSource` forward it.
- RequestHandler supplies Bearer when a provider exists, otherwise rejects request. Production `NetworkModule` initializes it with only HttpClient and RuntimeMetadataProvider; App's `startKoin` provides no credential source.
- No named auth/token/secure/session Kotlin files appeared in current repo tree; **name scan is not proof of absence**. Need inspect auth response/bootstrap DTO, retained state, session lifetime and platform secure storage before deciding exact credential provider contract. Auth integration intersects V2 W4 session and W7 platform wiring. Do not store bearer in RuntimeMetadata or arbitrary DynamicValue state. No token manager or extra HTTP client created to pretend W1 is complete.
- Preserving NONE authentication of Bootstrap and existing public screens is required.

## Hygiene, KDoc, safe logging
- The one production import `UnresolvedAddressException` is used; test import used. Existing NetworkException.Connection reused; no dependencies.
- Class KDoc amended. No new logs in this narrow catch: error output has safe classification and stable message, while Kermit configuration and privacy-safe event logging are still pending.
- To close W1, verify compilation + tests, actual I/O failure mapping on relevant engines, error taxonomy UI handling, accepted token owner/wiring, KDoc logging and full source-hygiene gate.

## User verification (Windows PowerShell)
```powershell
cd D:\CarBroz\CB-Partner
git status --short
git fetch origin --prune
git merge --ff-only origin/architecture/dynamic-runtime-v2-discussion-freeze
.\gradlew.bat :network:jvmTest :data:jvmTest :feature:dynamic:jvmTest
git diff --check
```
