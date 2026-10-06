# QA report: Gastos Personales (Android), 2026-10-06

Status: **DONE_WITH_CONCERNS** (static and unit checks done; on-device probes blocked, see "Blocked").

## Boundaries
- Target: `com.worthit.gastospersonales` (debug build) on device `RF8M92XDTCN`, plus Gradle checks.
- Surfaces: functional (Gradle: lint, unit tests, release build) and UI journeys through `adb`. No browser.
- Writes: this report folder only. No source changes were made in this run.
- Tier: Standard. No git repository exists, so no commits were possible or made.
- Not touched: `com.umss.registrogasto` (user data) and `com.worthit.registrogasto` (another app).

## Results

| Contract | Result | Evidence |
|---|---|---|
| Unit tests | PASS, 40 tests, 0 failures | `lint-and-tests.log`, Gradle test results |
| Lint (`lintDebug`) | PASS, 0 errors, 22 warnings | `lint-results-debug.txt` |
| Release build with R8 | PASS, `.aab` 3.0 MB; the R8 build was exercised on the phone earlier in this session (save, report, detail, share image, edit, language) | previous session steps |
| Project identity | PASS, `applicationId` and `namespace` are both `com.worthit.gastospersonales`; the built APK reports the same package and launcher activity; no `com.umss` left outside `build/` | `aapt2 dump badging` |
| UI journeys on device | BLOCKED | see below |

### Lint warnings worth acting on (none are errors)
1. `MissingQuantity` in `values-es/strings.xml` (`report_spents_count`): Spanish needs a `many` quantity for large numbers (low).
2. `UnusedResources`: style `Theme.Gastopersonal` in `values/themes.xml` is dead (low).
3. `RedundantLabel` on the activity in `AndroidManifest.xml` (cosmetic).
4. `SetTextI18n` in `SpentAdapter.kt` and `SpentImageRenderer.kt`: "date · category" is built by concatenation (low).
5. Dependency and AGP updates available (not QA findings).

## Blocked
**On-device journeys were not run.** A first launch after `pm clear` crashed with
`NoClassDefFoundError: Failed resolution of: Landroidx/startup/R$string`
(`crash-first-launch.txt`). Root cause is not the app:
- The installed package shows `firstInstallTime=19:05:48` and `lastUpdateTime=19:07:18`, and this run never installed anything (last local build 19:04, not installed). Someone else installed the app on the same phone while the probe was launching it; Android raises exactly this error when the APK is replaced under a starting process.
- The user then reported Android Studio's error `Activity class {com.umss.registrogasto/com.worthit.gastospersonales.MainActivity} does not exist`. That is the old application id with the new code package: the state the project had after the IDE's partial rename (namespace new, applicationId old). The files on disk are now consistent, so the IDE needs a Gradle sync.
- The first two probe screenshots (`screenshots/A-empty-save.png`, `B-blank-desc.png`) are **void**: the app was not on screen, and the taps landed in the app drawer and opened another app (Localizador). Nothing was changed there as far as the screens show.

Device ownership is therefore unclear (shared with the user's IDE runs), so probes stay stopped until the user confirms the phone is free.

## Candidate defects (from code and earlier observation, NOT yet replayed in this run)
Not counted as verified findings; each needs replay on the device before repair.
1. **Validation errors appear under the wrong field** (medium): amount, coin and category errors are all shown under "Descripción". Seen earlier in the session ("El monto debe ser un número" under Description).
2. **Large amounts show scientific notation** (low to medium): amounts of 10,000,000 or more print like `1.0E7 BOB` in the list and detail, because the amount is built with `Double.toString`. The edit prefill also converts whole numbers with `toLong()`, which overflows above 9.2e18.
3. List shows `30.0 BOB`, the report shows `30 BOB` (cosmetic inconsistency).

## Regression tests added
None in this run (no defect was reproduced and fixed).

## Next
1. User: sync Gradle in Android Studio, run again; uninstall the old `com.umss.registrogasto` when its test data is no longer needed.
2. When the phone is confirmed free: replay candidates 1 and 2, fix them with a regression test each, re-run the journeys (create, validate, edit, delete, report, share) and re-score.
