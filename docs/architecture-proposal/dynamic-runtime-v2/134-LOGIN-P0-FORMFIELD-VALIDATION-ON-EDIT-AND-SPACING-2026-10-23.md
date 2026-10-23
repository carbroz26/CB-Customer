# 134 — P0 FormFieldSection validation reset and error-to-button spacing correction

**Date:** 2026-10-23  
**Frontend branch:** `architecture/dynamic-runtime-v2-discussion-freeze`  
**Owner evidence:** Actual Dynamic Login screenshot submitted 2026-10-23, showing red message touching Continue button and red input/error state persisting after all digits deleted.  
**Supersedes:** Only the **edit-lifecycle expectation** and **no-extra-height assumption** in historical frozen document 132 and validation implementation document 133. Their FormFieldSection SRP ownership and all other contracts remain frozen.  
**Status (owner-reported Windows execution, 2026-10-23):** Source, tests and JSON committed; **Gradle P0 verification PASSED. Browser visual regression and acceptance still pending.**

## Approved reference: exact VisualLoginPreview source

`VisualLoginPreview.kt` sets `attempted = true` on Continue and `attempted = false` on **every phone input change**. Rendering condition is `attempted && !phoneValid`. Thus 12-digit invalid → first backspace at 11 already clears red state; it does **not** remain red until 10. This **later owner clarification supersedes** the prior 12→11 retains-error rule from v7/Document 131. Submitting a still-invalid 11 or 8-digit value will show errors again. Editing, including deleting to blank or typing from blank, clears the prior submitted error on the next input change. Never treat the blank field as valid on submit.

Visual reference uses `Spacer(height=24.dp)` *after* error rendering. A fixed always-present error slot alone leaves no gap when error text fills the slot.

## Generic typed implementation, no screen-specific checks

`DynamicValidationDto.clearOnEdit: Boolean = false` and corresponding Domain and ElementMapper field are backed by optional input `validation.clearOnEdit` in JSON. Default `false` retains historical revalidation semantics for all other inputs. **Login JSON v9 sets `clearOnEdit:true` explicitly.** This is an authored validation-display policy, not a phone-number special-case.

`DynamicFormValidator.clearsErrorOnEdit(screen,binding)` resolves the policy from existing typed input elements. Validator's reusable `validate(screen,...,suppressedBinding)` excludes only the currently edited binding when author-directed; all other field errors and fallback evaluation continue to behave normally. `DynamicStore.updateBinding` continues owning transient state; it delegates policy lookup and validation, has no new Store, no view-specific flags, and persists the resulting validation error map through the existing entry state. On subsequent Continue submit, all fields are validated without suppression.

## Error slot and spacing

Login v9's `FormFieldSection.properties.padding.bottom:24` adds backend-authored space *after* the always-reserved `validationPresentation.messageStyle.reserveHeight:24` row. This preserves a stable form height while the error appears/disappears; normal/error heights must match. The phone field still uses authored 8dp label gap and 56dp control surface with red border and editable-only pink background. **This adds 24dp to the overall card in all states versus v8**, a deliberate and necessary parity correction. No global default was added.

## Scope and files

- `data/.../model/validation/DynamicValidationDto.kt`: optional typed boolean policy.
- `domain/.../model/dynamic/validation/DynamicValidation.kt`: corresponding Domain value.
- `data/.../mapper/ElementMapper.kt`: map field.
- `feature/dynamic/.../runtime/DynamicFormValidator.kt`: centralized input traversal and suppression of edited binding.
- `feature/dynamic/.../store/DynamicStore.kt`: author-driven error reset on edit; retain existing revalidation for other fields.
- `feature/dynamic/.../store/DynamicStoreTest.kt`, `feature/dynamic/.../runtime/DynamicFormValidatorTest.kt` and `data/.../mapper/DynamicValidationMessageContractTest.kt`: regression coverage.
- New `partner-login-country-picker-parity-v9.json` and byte-identical active Web preview JSON; **v8 retained immutable**.
- **Unchanged:** FormFieldSection renderer, StackGroup ownership, Country Picker JSON/selection/fallback, VisualLoginPreview, backend, assets, hero, button, actions, OTP, navigation and all P1 areas.

## Acceptance tests

1. Initial: no errors, reserved field slot and authored 24dp button gap.
2. Empty → Continue: "Mobile number is required.", red outline, editable area pink; no card jump.
3. Start typing after submitted empty error: instantly clear validation UI even if partial value is still invalid.
4. 8 digits → Continue: length message; type 9th digit: normal UI; Continue again: length message.
5. 12 digits → Continue: length message; backspace to 11 **clears on first edit**; backspace to valid 10 remains normal.
6. 12 digits → Continue → delete all: no stale red tint/border/message at blank; blank → Continue once more: required error returns.
7. Default missing clearOnEdit: historical revalidate behavior still works in other fields/tests; errors for other keys not hidden.
8. Normal/focused/invalid field border precedence and fixed slot/card height; Country Picker, hero, legal text unaffected.
9. Verify `:data:jvmTest :feature:dynamic:jvmTest :feature:dynamic:compileAndroidMain :webApp:wasmJsBrowserDevelopmentWebpack` and matched-viewport screenshots. **Do not claim completion until tests run and owner confirms UI.**

Gray disabled/error Continue styling and red exclamation indicator present in isolated reference have separate unresolved contract/design ownership. They were intentionally **not** implemented in this targeted error-reset/spacing fix.

## Execution evidence — 2026-10-23

The owner pulled commit `257c396bcd155eee7e38ac52f166da363dfe2cf0` and executed:

```powershell
.\gradlew.bat :data:jvmTest :feature:dynamic:jvmTest :feature:dynamic:compileAndroidMain :webApp:wasmJsBrowserDevelopmentWebpack
```

Observed: **BUILD SUCCESSFUL in 4m 26s; 145 actionable tasks (44 executed, 101 up-to-date).** Four requested gates passed: `data:jvmTest`, `feature:dynamic:jvmTest`, `feature:dynamic:compileAndroidMain`, and `webApp:wasmJsBrowserDevelopmentWebpack`.

Non-blocking warnings remain: `FormFieldSection.kt:59` always-true condition; WebLoginPreview experimental Wasm interop opt-in; Windows iOS simulator unavailable; Android host-test config warning; transitive `ws` selection; Webpack expression dependency warning. These warnings have not been modified or declared fixed.

**Not yet proven:** Actual browser layout spacing, error reset after editing an invalid/empty number, stable form height, country picker, and approved VisualLoginPreview parity. Do not mark P0 acceptance frozen until screenshots or direct runtime checks support these claims. **No P1 source cleanup performed.**
