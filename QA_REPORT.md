# VizoEdit source validation report — 0.6.0

## Checks performed in the authoring environment

- Compiled the pure Kotlin editor/domain files with the locally installed Kotlin compiler (`kotlinc`): settings, color looks, codec settings, export profile, keyframe interpolation, transform model, transitions model, canvas/audio settings, timeline data model, and trim range. Result: passed.
- Added JUnit tests for editor settings, look presets, overlay settings, codec/bitrate settings, keyframe interpolation, transform model, transition timing, export-profile estimates, timeline operations, and trim history.
- Reviewed source wiring so color recipes, transforms/crop/canvas, text/emoji overlay, source-audio gain/fades, codec requests, and bitrate requests feed into the Media3 export path.
- Updated project persistence to store the added settings, using safe defaults for earlier project documents that omit those fields.
- Reviewed project files and GitHub Actions configuration.

## Checks not performed

- A complete Android Gradle build and execution of Gradle/JUnit tasks were not possible in this authoring environment because Android SDK/Gradle dependencies are unavailable here and network downloads are blocked.
- No APK was installed or tested on a physical Android device or emulator.
- No sample video was rendered, so codec support, visual effect quality, output duration, memory use, and audio fades must be verified in GitHub Actions and on devices.

## Release gate

Do not publish a release until `Android CI` passes on GitHub and a set of real test videos successfully imports, previews, exports, saves to Gallery, and opens in an external player on multiple Android devices. H.265 is hardware-dependent and should be tested separately.

## 0.5.0 additions
- Added pure Kotlin models and tests for audio clip settings, SRT parsing/writing, overlay layers, mask geometry and transition configuration.
- SRT codec and validation logic can be unit-tested without media assets.
- Not verified in this environment: full Gradle build, Android packaging, on-device playback/export, real-time transition rendering, audio DSP, masks/blend modes, reverse playback/export, stabilization, optical flow, motion tracking, or all-device codec support.
- Important: configuration models are not a substitute for renderer integration. UI must not claim a setting affects exported media until the render pipeline applies it.


## 0.6.0 source review
- Added Android multi-document picker workflow to gather video URIs and read their durations with `MediaMetadataRetriever`.
- Added user-visible clip queue controls for ordering/removal.
- Connected queued clips to Media3 `Composition` export; optional background audio is configured as a looping audio-only sequence with a `GainProcessor`.
- Re-ran the pure Kotlin domain compilation after changes; this script does not compile Android/Compose/Media3 APIs.
- Still unverified: full `assembleDebug`, Android ActivityResult/Compose compilation, Media3 composition export on codecs/devices, audio mixing quality, permissions edge cases and real-device UX. GitHub Actions is the next full-build gate.
## Build error fixes prepared (2026-10-18)

- Media3 dependencies now use a single aligned version (`1.8.0`), which provides `DefaultGainProvider`, `GainProcessor`, and `StaticOverlaySettings` used by the source.
- Corrected Kotlin SRT regex escaping by using a raw string literal.
- Updated Android SDK setup to set `packages: platform-tools` explicitly so setup does not attempt to install the obsolete `tools` SDK package.
- The pure Kotlin validation script passes after the edits. The complete Android Gradle build has not been run in this environment; use GitHub Actions to confirm compilation and packaging.
