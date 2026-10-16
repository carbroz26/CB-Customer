# 46 — W3-B Required Image URL and Nested Content Validation

**State:** Kotlin changes and regression tests committed; owner local JVM/Web validation PENDING. Branch `architecture/dynamic-runtime-v2-discussion-freeze`.

## Source contracts and fixed scope
- Frozen doc 20 §1/§4: `ImageElement` needs a required URL unless explicitly made optional. Existing legacy name-only `IconElement` and `IconContent` must remain readable as unavailable assets, without a glyph-name registry.
- Element contents are optional lists attached via `leading`/`trailing`; each declared entry must be a valid node. `DividerContent` and `IconContent` remain the only concrete content types, with existing `DynamicSpacing`.
- Changes only to existing `ElementMapper.kt`, `ElementContentMapper.kt`, `DynamicResponseMapperTest.kt`.

## Exact changes
1. `ElementMapper.mapImage`: a null, empty or blank `properties.url` now raises `IllegalArgumentException` through `requireNotNull`/nonblank check, instead of silently turning it into `""`. A valid URL maps unchanged. URL origin/policy validation remains separately owned and is **not** invented here.
2. `ElementContentMapper.mapList`: absent list = empty; declared list must be JSON array; each item must be JSON object. Invalid container/items throw `IllegalArgumentException` instead of disappearing. Nested map still validates concrete type, maps icon/divider, spacing and actions unchanged.
3. Tests cover missing/empty/blank Image URL, untouched name-only legacy Icon URL fallback, malformed nested content list/nonobject entries, and valid Divider spacing mapping.

## Decisions and nonchanges
- No new framework, factory, interface, DTO tree or mapper; six category hierarchy remains stable.
- No changes to `IconElement`, `IconContent`, `DynamicAssetImage`, renderer, Store or backend. Valid JSON behavior unchanged.
- No claim that missing source URLs on Image are optional; future backend wire contract would need owner review to introduce optional image assets.
- Existing invalid `properties` and `actions` nested object handling outside `leading`/`trailing` remains outside W3-B scope; do not pretend W3-B universally validates all JSON.

## Verification requested
```powershell
cd D:\CarBroz\CB-Partner
git status --short
git fetch origin --prune
git merge --ff-only origin/architecture/dynamic-runtime-v2-discussion-freeze
.\gradlew.bat :data:jvmTest --tests "*DynamicResponseMapperTest*"
.\gradlew.bat :domain:jvmTest :data:jvmTest :feature:dynamic:jvmTest :webApp:wasmJsBrowserDevelopmentWebpack
git diff --check
```
No JVM/Wasm test passed claim from GitHub-only editing. Android/iOS runtime deferred.
